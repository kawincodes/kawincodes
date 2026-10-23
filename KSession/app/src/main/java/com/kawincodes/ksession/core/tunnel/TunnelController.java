package com.kawincodes.ksession.core.tunnel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Runs tunnels: prepares config/credentials, launches cloudflared, parses its logs into
 * {@link TunnelSnapshot}s, scrapes metrics, restarts on failure and keeps {@code TunnelService}
 * in the foreground while anything runs. All public methods are safe from the main thread.
 */
public final class TunnelController {

    private final AppGraph graph;
    private final MutableLiveData<Map<String, TunnelSnapshot>> snapshots =
            new MutableLiveData<>(Collections.emptyMap());

    public TunnelController(AppGraph graph) {
        this.graph = graph;
    }

    /** Snapshots of every profile keyed by profile id (stopped profiles included). */
    public LiveData<Map<String, TunnelSnapshot>> snapshots() {
        return snapshots;
    }

    /** Snapshot for one profile; a STOPPED snapshot when idle or unknown (never null). */
    public TunnelSnapshot snapshot(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Snapshots whose status {@link TunnelStatus#isActive()}. */
    public List<TunnelSnapshot> activeSnapshots() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public int activeCount() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public boolean isRunning(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Starts the profile (no-op if already active). Failures surface as ERROR snapshots. */
    public void start(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Graceful stop (SIGTERM, then kill after a grace period). */
    public void stop(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void stopAll() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void restart(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Log buffer for a profile (created on demand, never null). */
    public LogBuffer logs(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void addEventListener(TunnelEventListener l) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void removeEventListener(TunnelEventListener l) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Restarts tunnels that were running before the process died (Settings.runningProfileIds). */
    public void restoreAfterProcessDeath() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Starts every profile with autoStart (used at boot and by the quick settings tile). */
    public void startAutoStartProfiles() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Called by the repository/provisioner after a profile is deleted: stops it and drops its state. */
    public void forget(String profileId) {
        throw new UnsupportedOperationException("TODO runtime");
    }
}
