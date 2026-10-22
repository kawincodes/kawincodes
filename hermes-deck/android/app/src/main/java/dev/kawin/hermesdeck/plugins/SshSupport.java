package dev.kawin.hermesdeck.plugins;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import org.bouncycastle.util.encoders.Base64;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Android-independent SSH logic behind {@link SshPlugin}, so it can be tested on the JVM against a
 * real OpenSSH server (see SshSupportTest).
 */
final class SshSupport {

    static {
        // Android has no JCE EdDSA/XDH, so route Ed25519 and Curve25519 through Bouncy Castle.
        JSch.setConfig("ssh-ed25519", "com.jcraft.jsch.bc.SignatureEd25519");
        JSch.setConfig("ssh-ed448", "com.jcraft.jsch.bc.SignatureEd448");
        JSch.setConfig("keypairgen.eddsa", "com.jcraft.jsch.bc.KeyPairGenEdDSA");
        JSch.setConfig("xdh", "com.jcraft.jsch.bc.XDH");
    }

    static final int CONNECT_TIMEOUT_MS = 20000;

    private SshSupport() {
    }

    interface OutputSink {
        void write(String stream, String chunk);
    }

    /** Reads the server host key without authenticating. Returns {type, fingerprint}. */
    static String[] probe(String host, int port) throws JSchException {
        PinnedHostKeys repo = new PinnedHostKeys(null);
        JSch jsch = new JSch();
        jsch.setHostKeyRepository(repo);
        Session session = null;
        JSchException failure = null;
        try {
            session = jsch.getSession("hermes-deck-probe", host, port);
            session.setConfig("StrictHostKeyChecking", "yes");
            session.connect(CONNECT_TIMEOUT_MS);
        } catch (JSchException e) {
            // Expected: the repository rejects the unknown key right after key exchange.
            failure = e;
        } finally {
            if (session != null) session.disconnect();
        }
        if (repo.seenFingerprint == null) {
            throw failure != null ? failure : new JSchException("Could not read the server host key");
        }
        return new String[] { repo.seenType, repo.seenFingerprint };
    }

    /** Opens an authenticated session that only proceeds if the host key matches {@code expectedFingerprint}. */
    static Session open(String host, int port, String username, String password, String privateKey, String passphrase, String expectedFingerprint)
        throws JSchException {
        if (expectedFingerprint == null || expectedFingerprint.isEmpty()) {
            throw new JSchException("A pinned host key fingerprint is required");
        }
        JSch jsch = new JSch();
        jsch.setHostKeyRepository(new PinnedHostKeys(expectedFingerprint));
        if (privateKey != null && !privateKey.trim().isEmpty()) {
            byte[] pass = passphrase == null || passphrase.isEmpty() ? null : passphrase.getBytes(StandardCharsets.UTF_8);
            jsch.addIdentity("hermes-deck", privateKey.trim().getBytes(StandardCharsets.UTF_8), null, pass);
        }
        Session session = jsch.getSession(username, host, port);
        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }
        session.setUserInfo(new PasswordInfo(password));
        session.setConfig("StrictHostKeyChecking", "yes");
        session.setServerAliveInterval(15000);
        session.setServerAliveCountMax(4);
        session.connect(CONNECT_TIMEOUT_MS);
        return session;
    }

    /**
     * Runs {@code command}, writes {@code stdin} then EOF, streams stdout/stderr to {@code sink} and
     * returns the exit status. {@code onOpen} receives the channel so callers can cancel it.
     */
    static int exec(Session session, String command, String stdin, OutputSink sink, ExecutorService pool, Consumer<ChannelExec> onOpen)
        throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        try {
            channel.setCommand(command);
            InputStream out = channel.getInputStream();
            InputStream err = channel.getErrStream();
            OutputStream in = channel.getOutputStream();
            channel.connect(CONNECT_TIMEOUT_MS);
            if (onOpen != null) onOpen.accept(channel);
            if (stdin != null && !stdin.isEmpty()) {
                in.write(stdin.getBytes(StandardCharsets.UTF_8));
                in.flush();
            }
            in.close(); // sends EOF so the remote command does not wait for more input
            Future<?> errPump = pool.submit(() -> pump(err, "stderr", sink));
            pump(out, "stdout", sink);
            errPump.get();
            while (!channel.isClosed()) {
                Thread.sleep(25);
            }
            return channel.getExitStatus();
        } finally {
            channel.disconnect();
        }
    }

    private static void pump(InputStream stream, String name, OutputSink sink) {
        char[] buf = new char[4096];
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            int n;
            while ((n = reader.read(buf)) != -1) {
                if (n > 0) sink.write(name, new String(buf, 0, n));
            }
        } catch (Exception ignored) {
            // Stream closed by cancel/disconnect.
        }
    }

    static String friendly(Exception e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (msg.contains("Auth fail") || msg.contains("Auth cancel")) return "Authentication failed: check the username, password or private key.";
        if (msg.contains("HostKey has been changed")) return "HOST KEY MISMATCH: the server key differs from the pinned fingerprint. Refusing to connect.";
        if (msg.contains("UnknownHostException")) return "Unknown host: " + msg;
        if (msg.contains("timeout") || msg.contains("timed out")) return "Connection timed out: " + msg;
        if (msg.contains("invalid privatekey")) return "The private key could not be read (wrong format or passphrase).";
        return msg;
    }

    /** Converts a raw SSH public key blob into `SHA256:<base64>` like `ssh-keygen -lf`. */
    static String fingerprint(byte[] key) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key);
        String b64 = Base64.toBase64String(digest);
        int end = b64.length();
        while (end > 0 && b64.charAt(end - 1) == '=') end--;
        return "SHA256:" + b64.substring(0, end);
    }

    static String keyType(byte[] key) {
        if (key.length < 4) return "unknown";
        int len = ((key[0] & 0xff) << 24) | ((key[1] & 0xff) << 16) | ((key[2] & 0xff) << 8) | (key[3] & 0xff);
        if (len <= 0 || len > key.length - 4) return "unknown";
        return new String(key, 4, len, StandardCharsets.US_ASCII);
    }

    /** Accepts exactly one fingerprint; with none pinned it records the presented key and rejects. */
    static final class PinnedHostKeys implements HostKeyRepository {
        private final String expected;
        volatile String seenFingerprint;
        volatile String seenType;

        PinnedHostKeys(String expected) {
            this.expected = expected;
        }

        @Override
        public int check(String host, byte[] key) {
            try {
                seenFingerprint = fingerprint(key);
                seenType = keyType(key);
            } catch (Exception e) {
                return CHANGED;
            }
            if (expected == null) return NOT_INCLUDED;
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), seenFingerprint.getBytes(StandardCharsets.US_ASCII)) ? OK : CHANGED;
        }

        @Override
        public void add(HostKey hostkey, UserInfo ui) {
        }

        @Override
        public void remove(String host, String type) {
        }

        @Override
        public void remove(String host, String type, byte[] key) {
        }

        @Override
        public String getKnownHostsRepositoryID() {
            return "hermes-deck-pinned";
        }

        @Override
        public HostKey[] getHostKey() {
            return new HostKey[0];
        }

        @Override
        public HostKey[] getHostKey(String host, String type) {
            return new HostKey[0];
        }
    }

    /** Answers keyboard-interactive password prompts (PAM) with the saved password; never prompts the user. */
    static final class PasswordInfo implements UserInfo, UIKeyboardInteractive {
        private final String password;

        PasswordInfo(String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String message) {
            return password != null && !password.isEmpty();
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            if (password == null || password.isEmpty()) return null;
            String[] answers = new String[prompt.length];
            for (int i = 0; i < prompt.length; i++) answers[i] = password;
            return answers;
        }
    }
}
