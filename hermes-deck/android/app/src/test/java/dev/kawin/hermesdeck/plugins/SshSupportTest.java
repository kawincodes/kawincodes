package dev.kawin.hermesdeck.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs the SSH code the app uses against a throwaway OpenSSH server on 127.0.0.1, covering the
 * Bouncy Castle Ed25519/X25519 path, host key pinning, stdin EOF handling and port forwarding.
 * Skipped when sshd is not installed.
 */
public class SshSupportTest {

    private static final String[] SSHD_PATHS = { "/usr/sbin/sshd", "/usr/local/sbin/sshd" };
    private static Process sshd;
    private static Path dir;
    private static int port;
    private static String hostFingerprint;
    private static String user;
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    @BeforeClass
    public static void startServer() throws Exception {
        String sshdPath = Arrays.stream(SSHD_PATHS).filter(p -> new File(p).canExecute()).findFirst().orElse(null);
        assumeTrue("sshd not installed", sshdPath != null);
        user = System.getProperty("user.name");
        dir = Files.createTempDirectory("hermes-deck-sshd");
        run("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", dir.resolve("host_ed25519").toString());
        run("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-C", "plain", "-f", dir.resolve("client").toString());
        run("ssh-keygen", "-q", "-t", "ed25519", "-N", "s3cret", "-C", "enc", "-f", dir.resolve("client_enc").toString());
        Files.write(dir.resolve("authorized_keys"), (read("client.pub") + read("client_enc.pub")).getBytes(StandardCharsets.UTF_8));
        hostFingerprint = run("ssh-keygen", "-lf", dir.resolve("host_ed25519.pub").toString()).split(" ")[1];

        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        String config = String.join("\n",
            "Port " + port,
            "ListenAddress 127.0.0.1",
            "HostKey " + dir.resolve("host_ed25519"),
            "AuthorizedKeysFile " + dir.resolve("authorized_keys"),
            "PasswordAuthentication no",
            "KbdInteractiveAuthentication no",
            "PubkeyAuthentication yes",
            "PermitRootLogin prohibit-password",
            "StrictModes no",
            "UsePAM no",
            "AllowTcpForwarding yes",
            "PidFile " + dir.resolve("sshd.pid"),
            "");
        Files.write(dir.resolve("sshd_config"), config.getBytes(StandardCharsets.UTF_8));
        if ("root".equals(user)) new File("/run/sshd").mkdirs(); // privilege separation directory
        sshd = new ProcessBuilder(sshdPath, "-D", "-e", "-f", dir.resolve("sshd_config").toString())
            .redirectErrorStream(true)
            .redirectOutput(dir.resolve("sshd.log").toFile())
            .start();
        for (int i = 0; i < 100; i++) {
            try (Socket ignored = new Socket("127.0.0.1", port)) {
                return;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
        fail("sshd did not start: " + read("sshd.log"));
    }

    @AfterClass
    public static void stopServer() {
        if (sshd != null) sshd.destroy();
        pool.shutdownNow();
    }

    @Test
    public void probeReportsTheSameFingerprintAsSshKeygen() throws Exception {
        String[] key = SshSupport.probe("127.0.0.1", port);
        assertEquals("ssh-ed25519", key[0]);
        assertEquals(hostFingerprint, key[1]);
    }

    @Test
    public void execStreamsOutputSendsStdinEofAndReturnsExitCode() throws Exception {
        Session session = open(read("client"), null, hostFingerprint);
        try {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            int code = SshSupport.exec(session, "cat; echo; echo oops >&2; exit 3", "hello",
                (stream, chunk) -> ("stdout".equals(stream) ? out : err).append(chunk), pool, null);
            assertEquals(3, code);
            assertEquals("hello\n", out.toString());
            assertEquals("oops\n", err.toString());
        } finally {
            session.disconnect();
        }
    }

    @Test
    public void passphraseProtectedOpenSshKeyWorks() throws Exception {
        Session session = open(read("client_enc"), "s3cret", hostFingerprint);
        try {
            StringBuilder out = new StringBuilder();
            assertEquals(0, SshSupport.exec(session, "id -un", null, (s, c) -> out.append(c), pool, null));
            assertEquals(user, out.toString().trim());
        } finally {
            session.disconnect();
        }
    }

    @Test
    public void wrongPinnedFingerprintIsRejectedBeforeAuthentication() throws Exception {
        String before = read("sshd.log");
        try {
            open(read("client"), null, "SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            fail("connection should have been refused");
        } catch (JSchException e) {
            assertTrue(SshSupport.friendly(e), SshSupport.friendly(e).startsWith("HOST KEY MISMATCH"));
        }
        Thread.sleep(200);
        String added = read("sshd.log").substring(before.length());
        assertTrue("client must not authenticate: " + added, !added.contains("Accepted publickey"));
    }

    @Test
    public void localForwardReachesAServiceOnTheServerLoopback() throws Exception {
        try (ServerSocket service = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
            pool.submit(() -> {
                try (Socket s = service.accept(); OutputStream os = s.getOutputStream()) {
                    os.write("pong".getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });
            Session session = open(read("client"), null, hostFingerprint);
            try {
                int local = session.setPortForwardingL("127.0.0.1", 0, "127.0.0.1", service.getLocalPort());
                try (Socket s = new Socket("127.0.0.1", local); InputStream in = s.getInputStream()) {
                    byte[] buf = new byte[4];
                    int n = 0;
                    while (n < 4) {
                        int r = in.read(buf, n, 4 - n);
                        if (r < 0) break;
                        n += r;
                    }
                    assertEquals("pong", new String(buf, 0, n, StandardCharsets.UTF_8));
                }
            } finally {
                session.disconnect();
            }
        }
    }

    @Test
    public void fingerprintMatchesOpenSshFormat() throws Exception {
        // SHA-256 of an empty blob, base64 without padding.
        assertEquals("SHA256:47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU", SshSupport.fingerprint(new byte[0]));
        assertEquals("ssh-ed25519", SshSupport.keyType(new byte[] { 0, 0, 0, 11, 's', 's', 'h', '-', 'e', 'd', '2', '5', '5', '1', '9' }));
    }

    private static Session open(String key, String passphrase, String fingerprint) throws JSchException {
        return SshSupport.open("127.0.0.1", port, user, null, key, passphrase, fingerprint);
    }

    private static String read(String name) throws Exception {
        return new String(Files.readAllBytes(dir.resolve(name)), StandardCharsets.UTF_8);
    }

    private static String run(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (p.waitFor() != 0) throw new IllegalStateException(String.join(" ", cmd) + ": " + out);
        return out;
    }
}
