package com.kawincodes.ksession.core.tunnel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A saved tunnel. Persisted as JSON by {@code TunnelRepository}. Secrets (connector token,
 * tunnel credentials) are never stored here; they live in {@code CredentialStore} under
 * {@link #tokenKey()} / {@link #credentialsKey()}.
 */
public final class TunnelProfile {
    public static final String DEFAULT_LOCAL_SERVICE = "http://127.0.0.1:8080";

    private String id;
    private String name;
    private TunnelType type;
    /** QUICK: the origin served at the random URL. Ignored for TOKEN (ingress is remote). */
    private String localService;
    /** MANAGED: hostname rules; the catch-all 404 is appended when the config is written. */
    private List<IngressRule> ingress = new ArrayList<>();
    /** Cloudflare tunnel UUID (TOKEN/MANAGED). */
    private String cfTunnelId;
    /** Cloudflare account tag the tunnel belongs to (TOKEN/MANAGED). */
    private String accountTag;
    /** Tunnel name on Cloudflare, when known. */
    private String remoteName;
    /** Zone used to route MANAGED hostnames. */
    private String zoneId;
    private boolean autoStart;
    /** "auto" | "quic" | "http2", or null to follow Settings. */
    private String protocolOverride;
    private long createdAt;
    /** Last public URL seen (quick tunnels get a new one each run). */
    private String lastPublicUrl;
    private String notes;

    public TunnelProfile() {
    }

    public static TunnelProfile create(String name, TunnelType type) {
        TunnelProfile p = new TunnelProfile();
        p.id = UUID.randomUUID().toString();
        p.name = name;
        p.type = type;
        p.createdAt = System.currentTimeMillis();
        if (type == TunnelType.QUICK) p.localService = DEFAULT_LOCAL_SERVICE;
        return p;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TunnelType getType() { return type; }
    public void setType(TunnelType type) { this.type = type; }
    public String getLocalService() { return localService; }
    public void setLocalService(String localService) { this.localService = localService; }
    public List<IngressRule> getIngress() { return Collections.unmodifiableList(ingress); }
    public void setIngress(List<IngressRule> rules) { this.ingress = new ArrayList<>(rules); }
    public String getCfTunnelId() { return cfTunnelId; }
    public void setCfTunnelId(String cfTunnelId) { this.cfTunnelId = cfTunnelId; }
    public String getAccountTag() { return accountTag; }
    public void setAccountTag(String accountTag) { this.accountTag = accountTag; }
    public String getRemoteName() { return remoteName; }
    public void setRemoteName(String remoteName) { this.remoteName = remoteName; }
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    public String getProtocolOverride() { return protocolOverride; }
    public void setProtocolOverride(String protocolOverride) { this.protocolOverride = protocolOverride; }
    public long getCreatedAt() { return createdAt; }
    public String getLastPublicUrl() { return lastPublicUrl; }
    public void setLastPublicUrl(String lastPublicUrl) { this.lastPublicUrl = lastPublicUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** CredentialStore key of the connector token (TOKEN). */
    public String tokenKey() {
        return "tunnel." + id + ".token";
    }

    /** CredentialStore key of the credentials JSON (MANAGED). */
    public String credentialsKey() {
        return "tunnel." + id + ".credentials";
    }

    /** Public URLs this tunnel serves (https://hostname per MANAGED rule, or the last quick URL). */
    public List<String> publicUrls() {
        List<String> urls = new ArrayList<>();
        if (type == TunnelType.MANAGED) {
            for (IngressRule r : ingress) if (r.hostname != null) urls.add("https://" + r.hostname);
        } else if (lastPublicUrl != null) {
            urls.add(lastPublicUrl);
        }
        return urls;
    }

    public TunnelProfile copy() {
        return fromJson(toJson());
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("name", name);
            o.put("type", type.name());
            o.putOpt("localService", localService);
            JSONArray rules = new JSONArray();
            for (IngressRule r : ingress) rules.put(r.toJson());
            o.put("ingress", rules);
            o.putOpt("cfTunnelId", cfTunnelId);
            o.putOpt("accountTag", accountTag);
            o.putOpt("remoteName", remoteName);
            o.putOpt("zoneId", zoneId);
            o.put("autoStart", autoStart);
            o.putOpt("protocolOverride", protocolOverride);
            o.put("createdAt", createdAt);
            o.putOpt("lastPublicUrl", lastPublicUrl);
            o.putOpt("notes", notes);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        return o;
    }

    public static TunnelProfile fromJson(JSONObject o) {
        TunnelProfile p = new TunnelProfile();
        p.id = o.optString("id", UUID.randomUUID().toString());
        p.name = o.optString("name", "tunnel");
        p.type = TunnelType.valueOf(o.optString("type", TunnelType.QUICK.name()));
        p.localService = o.optString("localService", null);
        JSONArray rules = o.optJSONArray("ingress");
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                JSONObject r = rules.optJSONObject(i);
                if (r != null) p.ingress.add(IngressRule.fromJson(r));
            }
        }
        p.cfTunnelId = o.optString("cfTunnelId", null);
        p.accountTag = o.optString("accountTag", null);
        p.remoteName = o.optString("remoteName", null);
        p.zoneId = o.optString("zoneId", null);
        p.autoStart = o.optBoolean("autoStart", false);
        p.protocolOverride = o.optString("protocolOverride", null);
        p.createdAt = o.optLong("createdAt", 0);
        p.lastPublicUrl = o.optString("lastPublicUrl", null);
        p.notes = o.optString("notes", null);
        return p;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
