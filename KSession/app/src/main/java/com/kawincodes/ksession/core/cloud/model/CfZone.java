package com.kawincodes.ksession.core.cloud.model;

/** A Cloudflare zone (domain). */
public final class CfZone {
    public final String id;
    public final String name;
    /** "active", "pending", ... */
    public final String status;
    public final String accountId;

    public CfZone(String id, String name, String status, String accountId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.accountId = accountId;
    }

    /** True if {@code hostname} is this zone or a subdomain of it. */
    public boolean covers(String hostname) {
        if (hostname == null || name == null) return false;
        String h = hostname.toLowerCase(java.util.Locale.ROOT);
        String z = name.toLowerCase(java.util.Locale.ROOT);
        return h.equals(z) || h.endsWith("." + z);
    }

    @Override
    public String toString() {
        return name;
    }
}
