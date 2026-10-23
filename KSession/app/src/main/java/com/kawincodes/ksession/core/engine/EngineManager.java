package com.kawincodes.ksession.core.engine;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;
import com.kawincodes.ksession.util.Callback;

/**
 * Locates, verifies and (on the sideload flavor) installs the cloudflared engine.
 * Candidates: {@code nativeLibraryDir/libcloudflared.so} (BUNDLED) and
 * {@code filesDir/engine/cloudflared} (INSTALLED). See docs/ARCHITECTURE.md §1.3.
 */
public final class EngineManager {

    /** Download progress, delivered on the main thread. */
    public interface ProgressListener {
        void onProgress(long bytes, long total);
    }

    private final AppGraph graph;
    private final MutableLiveData<EngineInfo> state = new MutableLiveData<>(EngineInfo.NONE);

    public EngineManager(AppGraph graph) {
        this.graph = graph;
    }

    /** Current engine; updated after {@link #refresh}, install and uninstall. */
    public LiveData<EngineInfo> state() {
        return state;
    }

    /** Last known engine info (never null). */
    public EngineInfo current() {
        EngineInfo v = state.getValue();
        return v == null ? EngineInfo.NONE : v;
    }

    /**
     * Detects the engine (runs {@code --version} on each candidate, preferring INSTALLED when
     * Settings.preferInstalledEngine() and it runs). Blocking variant for background threads.
     */
    public EngineInfo detectBlocking() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Async {@link #detectBlocking()}; {@code cb} may be null. */
    public void refresh(@Nullable Callback<EngineInfo> cb) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /**
     * True when a downloaded binary can be executed: BuildConfig.RUNTIME_ENGINE_INSTALL, or the
     * app's targetSdk is below 29, or the device runs Android 9 or older.
     */
    public boolean canInstallAtRuntime() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Latest cloudflared release tag from GitHub, e.g. "2026.10.3". */
    public void checkLatestVersion(Callback<String> cb) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /**
     * Downloads, verifies (pinned checksum for the pinned version, otherwise the GitHub asset
     * digest), installs and test-runs cloudflared. {@code version} null means latest.
     */
    public void install(@Nullable String version, @Nullable ProgressListener progress, Callback<EngineInfo> cb) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Cancels a running {@link #install}. */
    public void cancelInstall() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** True while {@link #install} is running. */
    public boolean isInstalling() {
        return false;
    }

    /** Deletes the downloaded engine (the bundled one stays) and re-detects. */
    public void uninstallDownloaded(@Nullable Callback<EngineInfo> cb) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Device ABI used to pick the binary, e.g. "arm64-v8a". */
    public String deviceAbi() {
        throw new UnsupportedOperationException("TODO runtime");
    }
}
