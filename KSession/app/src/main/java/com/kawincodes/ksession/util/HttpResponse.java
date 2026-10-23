package com.kawincodes.ksession.util;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** HTTP response with a fully buffered body. Header names are case-insensitive. */
public final class HttpResponse {
    public final int code;
    public final byte[] body;
    public final Map<String, String> headers;

    public HttpResponse(int code, byte[] body, Map<String, String> headers) {
        this.code = code;
        this.body = body == null ? new byte[0] : body;
        Map<String, String> h = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) h.putAll(headers);
        this.headers = Collections.unmodifiableMap(h);
    }

    public String bodyString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    public String header(String name) {
        return headers.get(name);
    }
}
