package com.kawincodes.ksession.core.cloud.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** What cloudflared needs to run a named tunnel: account tag, tunnel ID and secret. */
public final class TunnelCredentials {
    public final String accountTag;
    public final String tunnelId;
    /** Base64 tunnel secret (at least 32 bytes decoded). */
    public final String tunnelSecret;
    /** Optional endpoint ("fed"), usually null. */
    public final String endpoint;

    public TunnelCredentials(String accountTag, String tunnelId, String tunnelSecret, String endpoint) {
        this.accountTag = accountTag;
        this.tunnelId = tunnelId;
        this.tunnelSecret = tunnelSecret;
        this.endpoint = endpoint;
    }

    /** cloudflared credentials-file JSON: {"AccountTag","TunnelSecret","TunnelID"[,"Endpoint"]}. */
    public String toCredentialsFileJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("AccountTag", accountTag);
            o.put("TunnelSecret", tunnelSecret);
            o.put("TunnelID", tunnelId);
            if (endpoint != null && !endpoint.isEmpty()) o.put("Endpoint", endpoint);
            return o.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public static TunnelCredentials fromCredentialsFileJson(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        return new TunnelCredentials(o.getString("AccountTag"), o.getString("TunnelID"),
                o.getString("TunnelSecret"), o.optString("Endpoint", null));
    }

    /** Connector token: base64(JSON {"a","t","s"[,"e"]}), as accepted by {@code cloudflared tunnel run --token}. */
    public String toToken() {
        try {
            JSONObject o = new JSONObject();
            o.put("a", accountTag);
            o.put("t", tunnelId);
            o.put("s", tunnelSecret);
            if (endpoint != null && !endpoint.isEmpty()) o.put("e", endpoint);
            return Base64.getEncoder().encodeToString(o.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }
}
