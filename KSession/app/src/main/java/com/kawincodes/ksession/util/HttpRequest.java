package com.kawincodes.ksession.util;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable-ish HTTP request description consumed by {@link HttpClient}. */
public final class HttpRequest {
    public final String method;
    public final String url;
    public final Map<String, String> headers = new LinkedHashMap<>();
    public byte[] body;
    public int connectTimeoutMillis = 15_000;
    public int readTimeoutMillis = 30_000;
    /** Responses larger than this are rejected (protects against hostile servers). */
    public long maxResponseBytes = 4L * 1024 * 1024;

    public HttpRequest(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public static HttpRequest get(String url) {
        return new HttpRequest("GET", url);
    }

    public static HttpRequest post(String url) {
        return new HttpRequest("POST", url);
    }

    public HttpRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpRequest jsonBody(String json) {
        body = json.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "application/json");
        return this;
    }

    public HttpRequest timeouts(int connectMillis, int readMillis) {
        connectTimeoutMillis = connectMillis;
        readTimeoutMillis = readMillis;
        return this;
    }
}
