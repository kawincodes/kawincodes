package dev.kawin.hermesdeck.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SSH client for deploying and managing Hermes Agent on a remote server.
 *
 * Host keys are pinned: {@link #probeHostKey} reads the server key without authenticating so the
 * user can verify it, and {@link #connect} refuses to authenticate unless the server presents
 * exactly that key.
 */
@CapacitorPlugin(name = "Ssh")
public class SshPlugin extends Plugin {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChannelExec> execs = new ConcurrentHashMap<>();

    @PluginMethod
    public void probeHostKey(PluginCall call) {
        String host = call.getString("host");
        int port = call.getInt("port", 22);
        if (host == null || host.trim().isEmpty()) {
            call.reject("Host is required");
            return;
        }
        pool.execute(() -> {
            try {
                String[] key = SshSupport.probe(host.trim(), port);
                JSObject result = new JSObject();
                result.put("type", key[0]);
                result.put("fingerprint", key[1]);
                call.resolve(result);
            } catch (Exception e) {
                call.reject(SshSupport.friendly(e));
            }
        });
    }

    @PluginMethod
    public void connect(PluginCall call) {
        String host = call.getString("host");
        int port = call.getInt("port", 22);
        String username = call.getString("username");
        String expected = call.getString("expectedFingerprint");
        if (host == null || username == null || expected == null || expected.isEmpty()) {
            call.reject("host, username and expectedFingerprint are required");
            return;
        }
        pool.execute(() -> {
            try {
                Session session = SshSupport.open(
                    host.trim(),
                    port,
                    username.trim(),
                    call.getString("password"),
                    call.getString("privateKey"),
                    call.getString("passphrase"),
                    expected
                );
                String id = UUID.randomUUID().toString();
                sessions.put(id, session);
                JSObject result = new JSObject();
                result.put("sessionId", id);
                call.resolve(result);
            } catch (Exception e) {
                call.reject(SshSupport.friendly(e));
            }
        });
    }

    @PluginMethod
    public void exec(PluginCall call) {
        String sessionId = call.getString("sessionId");
        String execId = call.getString("execId");
        String command = call.getString("command");
        Session session = sessionId == null ? null : sessions.get(sessionId);
        if (session == null || !session.isConnected()) {
            call.reject("SSH session is not connected");
            return;
        }
        if (execId == null || command == null) {
            call.reject("execId and command are required");
            return;
        }
        pool.execute(() -> {
            try {
                int code = SshSupport.exec(session, command, call.getString("stdin"), (stream, chunk) -> {
                    JSObject event = new JSObject();
                    event.put("execId", execId);
                    event.put("stream", stream);
                    event.put("chunk", chunk);
                    notifyListeners("output", event);
                }, pool, channel -> execs.put(execId, channel));
                JSObject result = new JSObject();
                result.put("exitCode", code);
                call.resolve(result);
            } catch (Exception e) {
                call.reject(SshSupport.friendly(e));
            } finally {
                execs.remove(execId);
            }
        });
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        String execId = call.getString("execId");
        ChannelExec channel = execId == null ? null : execs.remove(execId);
        if (channel != null) {
            try {
                channel.sendSignal("TERM");
            } catch (Exception ignored) {
                // Many servers ignore signal requests; disconnecting below still stops the stream.
            }
            channel.disconnect();
        }
        call.resolve();
    }

    @PluginMethod
    public void forward(PluginCall call) {
        String sessionId = call.getString("sessionId");
        int remotePort = call.getInt("remotePort", 8642);
        Session session = sessionId == null ? null : sessions.get(sessionId);
        if (session == null || !session.isConnected()) {
            call.reject("SSH session is not connected");
            return;
        }
        pool.execute(() -> {
            try {
                // Bind to loopback only: the tunnel must not be reachable from the local network.
                int localPort = session.setPortForwardingL("127.0.0.1", 0, "127.0.0.1", remotePort);
                JSObject result = new JSObject();
                result.put("localPort", localPort);
                call.resolve(result);
            } catch (Exception e) {
                call.reject(SshSupport.friendly(e));
            }
        });
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        String sessionId = call.getString("sessionId");
        Session session = sessionId == null ? null : sessions.remove(sessionId);
        if (session != null) session.disconnect();
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        for (Session session : sessions.values()) session.disconnect();
        sessions.clear();
        pool.shutdownNow();
    }
}
