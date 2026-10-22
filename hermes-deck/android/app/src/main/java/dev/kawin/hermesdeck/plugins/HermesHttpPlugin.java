package dev.kawin.hermesdeck.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Talks to the Hermes API server from native code, so requests are not subject to WebView CORS
 * and responses can be streamed (SSE) chunk by chunk. Cleartext is limited to loopback by
 * network_security_config.xml (SSH tunnels and on-device Termux); everything else must be HTTPS.
 */
@CapacitorPlugin(name = "HermesHttp")
public class HermesHttpPlugin extends Plugin {

    private static final int MAX_BODY_BYTES = 4 * 1024 * 1024;

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, HttpURLConnection> open = new ConcurrentHashMap<>();
    private final Set<String> cancelled = ConcurrentHashMap.newKeySet();

    @PluginMethod
    public void request(PluginCall call) {
        pool.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = openConnection(call, call.getInt("timeoutMs", 20000));
                int status = conn.getResponseCode();
                JSObject result = new JSObject();
                result.put("status", status);
                result.put("body", readAll(status >= 400 ? conn.getErrorStream() : conn.getInputStream()));
                call.resolve(result);
            } catch (Exception e) {
                call.reject(describe(e));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    @PluginMethod
    public void stream(PluginCall call) {
        String streamId = call.getString("streamId");
        if (streamId == null) {
            call.reject("streamId is required");
            return;
        }
        pool.execute(() -> {
            HttpURLConnection conn = null;
            try {
                // Hermes sends an SSE keepalive every 10s, so a 90s read timeout only trips on a dead link.
                conn = openConnection(call, call.getInt("timeoutMs", 90000));
                open.put(streamId, conn);
                int status = conn.getResponseCode();
                JSObject result = new JSObject();
                result.put("status", status);
                if (status >= 400) {
                    result.put("body", readAll(conn.getErrorStream()));
                    call.resolve(result);
                    return;
                }
                char[] buf = new char[2048];
                try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    int n;
                    while ((n = reader.read(buf)) != -1) {
                        if (n == 0) continue;
                        JSObject event = new JSObject();
                        event.put("streamId", streamId);
                        event.put("data", new String(buf, 0, n));
                        notifyListeners("chunk", event);
                    }
                }
                result.put("body", "");
                call.resolve(result);
            } catch (Exception e) {
                if (cancelled.remove(streamId)) {
                    // Cancelled by the user: report a clean end instead of an error.
                    JSObject result = new JSObject();
                    result.put("status", 499);
                    result.put("body", "");
                    call.resolve(result);
                } else {
                    call.reject(describe(e));
                }
            } finally {
                open.remove(streamId);
                cancelled.remove(streamId);
                if (conn != null) conn.disconnect();
            }
        });
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        String streamId = call.getString("streamId");
        if (streamId != null) {
            HttpURLConnection conn = open.get(streamId);
            if (conn != null) {
                cancelled.add(streamId);
                pool.execute(conn::disconnect);
            }
        }
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        for (HttpURLConnection conn : open.values()) conn.disconnect();
        open.clear();
        pool.shutdownNow();
    }

    private HttpURLConnection openConnection(PluginCall call, int timeoutMs) throws Exception {
        String url = call.getString("url");
        if (url == null || !(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new IllegalArgumentException("Only http(s) URLs are supported");
        }
        String method = call.getString("method", "GET");
        String body = call.getString("body");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(timeoutMs);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false); // never forward the bearer token to another origin
        JSObject headers = call.getObject("headers", new JSObject());
        JSONObject json = headers == null ? new JSONObject() : headers;
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            conn.setRequestProperty(key, json.getString(key));
        }
        if (body != null) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
        }
        return conn;
    }

    private static String readAll(InputStream in) throws Exception {
        if (in == null) return "";
        try (InputStream stream = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = stream.read(buf)) != -1) {
                out.write(buf, 0, n);
                if (out.size() > MAX_BODY_BYTES) break;
            }
            return out.toString("UTF-8");
        }
    }

    private static String describe(Exception e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (msg.contains("Cleartext HTTP traffic")) {
            return "Plain http:// is only allowed to this device (127.0.0.1). Use https:// for remote agents, or connect over SSH.";
        }
        return msg;
    }
}
