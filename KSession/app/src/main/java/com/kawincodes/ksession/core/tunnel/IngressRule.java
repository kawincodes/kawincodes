package com.kawincodes.ksession.core.tunnel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/** One cloudflared ingress rule: requests for {@code hostname} (and optional path) go to {@code service}. */
public final class IngressRule {
    /** Public hostname, e.g. "app.example.com"; null for a catch-all rule. */
    public final String hostname;
    /** Origin, e.g. "http://127.0.0.1:8080", "tcp://127.0.0.1:22", "http_status:404". */
    public final String service;
    /** Optional path regex. */
    public final String path;
    /** Skip TLS verification for https origins with self-signed certificates. */
    public final boolean noTlsVerify;

    public IngressRule(String hostname, String service, String path, boolean noTlsVerify) {
        this.hostname = emptyToNull(hostname);
        this.service = Objects.requireNonNull(service, "service");
        this.path = emptyToNull(path);
        this.noTlsVerify = noTlsVerify;
    }

    public IngressRule(String hostname, String service) {
        this(hostname, service, null, false);
    }

    public boolean isCatchAll() {
        return hostname == null && path == null;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            if (hostname != null) o.put("hostname", hostname);
            o.put("service", service);
            if (path != null) o.put("path", path);
            if (noTlsVerify) o.put("noTlsVerify", true);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        return o;
    }

    public static IngressRule fromJson(JSONObject o) {
        return new IngressRule(o.optString("hostname", null), o.optString("service", "http_status:404"),
                o.optString("path", null), o.optBoolean("noTlsVerify", false));
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IngressRule)) return false;
        IngressRule r = (IngressRule) o;
        return noTlsVerify == r.noTlsVerify && Objects.equals(hostname, r.hostname)
                && service.equals(r.service) && Objects.equals(path, r.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, service, path, noTlsVerify);
    }

    @Override
    public String toString() {
        return (hostname == null ? "*" : hostname) + (path == null ? "" : path) + " -> " + service;
    }
}
