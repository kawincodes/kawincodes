package com.kawincodes.ksession.core.tunnel;

/** Runtime state of a tunnel. */
public enum TunnelStatus {
    /** Not running. */
    STOPPED,
    /** Checking the engine, provisioning (quick tunnels), resolving the edge, writing config. */
    PREPARING,
    /** cloudflared is running but no edge connection is registered yet. */
    CONNECTING,
    /** At least one edge connection is registered and none are missing. */
    ONLINE,
    /** Some connections dropped (fewer than expected) or all dropped while cloudflared retries. */
    DEGRADED,
    /** Graceful shutdown in progress. */
    STOPPING,
    /** Failed to start or exited unexpectedly. May be restarted automatically. */
    ERROR;

    /** True while a process is (or is about to be) running. */
    public boolean isActive() {
        return this == PREPARING || this == CONNECTING || this == ONLINE || this == DEGRADED || this == STOPPING;
    }

    /** True when the tunnel can serve traffic. */
    public boolean isLive() {
        return this == ONLINE || this == DEGRADED;
    }
}
