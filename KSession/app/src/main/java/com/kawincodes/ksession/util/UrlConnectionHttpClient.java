package com.kawincodes.ksession.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** {@link HttpClient} backed by {@link HttpURLConnection}. */
public final class UrlConnectionHttpClient implements HttpClient {

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(request.url).openConnection();
        try {
            c.setRequestMethod(request.method);
            c.setConnectTimeout(request.connectTimeoutMillis);
            c.setReadTimeout(request.readTimeoutMillis);
            c.setInstanceFollowRedirects(true);
            c.setUseCaches(false);
            for (Map.Entry<String, String> h : request.headers.entrySet()) {
                c.setRequestProperty(h.getKey(), h.getValue());
            }
            if (request.body != null) {
                c.setDoOutput(true);
                c.setFixedLengthStreamingMode(request.body.length);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(request.body);
                }
            } else if ("POST".equals(request.method) || "PUT".equals(request.method)) {
                c.setDoOutput(true);
                c.setFixedLengthStreamingMode(0);
                c.getOutputStream().close();
            }
            int code = c.getResponseCode();
            InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
            byte[] body = in == null ? new byte[0] : readLimited(in, request.maxResponseBytes);
            Map<String, String> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> e : c.getHeaderFields().entrySet()) {
                if (e.getKey() != null && e.getValue() != null && !e.getValue().isEmpty()) {
                    headers.put(e.getKey(), e.getValue().get(0));
                }
            }
            return new HttpResponse(code, body, headers);
        } finally {
            c.disconnect();
        }
    }

    @Override
    public void download(String url, OutputStream sink, ProgressListener progress) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setConnectTimeout(20_000);
            c.setReadTimeout(60_000);
            c.setInstanceFollowRedirects(true);
            int code = c.getResponseCode();
            if (code < 200 || code >= 300) throw new IOException("HTTP " + code + " for " + url);
            long total = c.getContentLengthLong();
            long done = 0;
            byte[] buf = new byte[64 * 1024];
            try (InputStream in = c.getInputStream()) {
                int n;
                while ((n = in.read(buf)) > 0) {
                    if (progress != null && progress.isCancelled()) {
                        throw new InterruptedIOException("Download cancelled");
                    }
                    sink.write(buf, 0, n);
                    done += n;
                    if (progress != null) progress.onProgress(done, total);
                }
            }
        } finally {
            c.disconnect();
        }
    }

    private static byte[] readLimited(InputStream in, long max) throws IOException {
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16 * 1024];
            long total = 0;
            int n;
            while ((n = is.read(buf)) > 0) {
                total += n;
                if (total > max) throw new IOException("Response exceeds " + max + " bytes");
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}
