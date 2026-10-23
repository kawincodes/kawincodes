package com.kawincodes.ksession.core.cloud.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A Cloudflare Tunnel as returned by /accounts/{id}/cfd_tunnel. */
public final class CfTunnel {
    public final String id;
    public final String name;
    /** "healthy", "degraded", "down" or "inactive". */
    public final String status;
    public final String createdAt;
    /** True when ingress is managed in the dashboard ("config_src": "cloudflare"). */
    public final boolean remotelyManaged;
    public final List<CfConnection> connections;

    public CfTunnel(String id, String name, String status, String createdAt, boolean remotelyManaged,
                    List<CfConnection> connections) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.remotelyManaged = remotelyManaged;
        this.connections = Collections.unmodifiableList(
                connections == null ? new ArrayList<>() : new ArrayList<>(connections));
    }

    @Override
    public String toString() {
        return name;
    }
}
