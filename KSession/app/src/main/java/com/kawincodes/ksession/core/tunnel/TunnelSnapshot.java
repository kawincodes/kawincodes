package com.kawincodes.ksession.core.tunnel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable view of a tunnel's runtime state, published by {@code TunnelController}. */
public final class TunnelSnapshot {
    public final String profileId;
    public final String name;
    public final TunnelType type;
    public final TunnelStatus status;
    public final List<String> publicUrls;
    public final List<EdgeConnection> connections;
    public final TunnelMetrics metrics;
    /** When the current run started (0 when stopped). */
    public final long startedAtMillis;
    /** When the tunnel first became ONLINE in the current run (0 if never). */
    public final long onlineSinceMillis;
    /** Human readable reason for ERROR/DEGRADED, or null. */
    public final String lastError;
    /** Automatic restarts in the current session. */
    public final int restartCount;
    /** Connections cloudflared is expected to hold (ha-connections). */
    public final int expectedConnections;

    private TunnelSnapshot(Builder b) {
        profileId = b.profileId;
        name = b.name;
        type = b.type;
        status = b.status;
        publicUrls = Collections.unmodifiableList(new ArrayList<>(b.publicUrls));
        connections = Collections.unmodifiableList(new ArrayList<>(b.connections));
        metrics = b.metrics == null ? TunnelMetrics.EMPTY : b.metrics;
        startedAtMillis = b.startedAtMillis;
        onlineSinceMillis = b.onlineSinceMillis;
        lastError = b.lastError;
        restartCount = b.restartCount;
        expectedConnections = b.expectedConnections;
    }

    /** A STOPPED snapshot for a profile. */
    public static TunnelSnapshot stopped(TunnelProfile p) {
        return new Builder(p.getId(), p.getName(), p.getType()).publicUrls(p.publicUrls()).build();
    }

    /** First public URL, or null. */
    public String primaryUrl() {
        return publicUrls.isEmpty() ? null : publicUrls.get(0);
    }

    public Builder toBuilder() {
        return new Builder(profileId, name, type).status(status).publicUrls(publicUrls)
                .connections(connections).metrics(metrics).startedAt(startedAtMillis)
                .onlineSince(onlineSinceMillis).lastError(lastError).restartCount(restartCount)
                .expectedConnections(expectedConnections);
    }

    public static final class Builder {
        private final String profileId;
        private final String name;
        private final TunnelType type;
        private TunnelStatus status = TunnelStatus.STOPPED;
        private List<String> publicUrls = new ArrayList<>();
        private List<EdgeConnection> connections = new ArrayList<>();
        private TunnelMetrics metrics = TunnelMetrics.EMPTY;
        private long startedAtMillis;
        private long onlineSinceMillis;
        private String lastError;
        private int restartCount;
        private int expectedConnections;

        public Builder(String profileId, String name, TunnelType type) {
            this.profileId = profileId;
            this.name = name;
            this.type = type;
        }

        public Builder status(TunnelStatus s) { status = s; return this; }
        public Builder publicUrls(List<String> urls) { publicUrls = new ArrayList<>(urls); return this; }
        public Builder connections(List<EdgeConnection> c) { connections = new ArrayList<>(c); return this; }
        public Builder metrics(TunnelMetrics m) { metrics = m; return this; }
        public Builder startedAt(long t) { startedAtMillis = t; return this; }
        public Builder onlineSince(long t) { onlineSinceMillis = t; return this; }
        public Builder lastError(String e) { lastError = e; return this; }
        public Builder restartCount(int n) { restartCount = n; return this; }
        public Builder expectedConnections(int n) { expectedConnections = n; return this; }

        public TunnelSnapshot build() {
            return new TunnelSnapshot(this);
        }
    }
}
