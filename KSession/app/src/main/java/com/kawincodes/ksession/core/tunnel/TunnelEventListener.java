package com.kawincodes.ksession.core.tunnel;

/** Notified on the main thread about tunnel lifecycle events. */
public interface TunnelEventListener {

    /** Status changed (e.g. CONNECTING -> ONLINE). {@code previous} is never null. */
    void onStatusChanged(TunnelSnapshot previous, TunnelSnapshot current);

    /** A new public URL is known (quick tunnels: once provisioned). */
    default void onPublicUrl(TunnelSnapshot snapshot, String url) {
    }

    /** A connection to a data center was registered. */
    default void onConnectionRegistered(TunnelSnapshot snapshot, EdgeConnection connection) {
    }
}
