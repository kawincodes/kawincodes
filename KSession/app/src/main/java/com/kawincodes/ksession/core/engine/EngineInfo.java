package com.kawincodes.ksession.core.engine;

/** Snapshot of the cloudflared engine the app will use. */
public final class EngineInfo {
    public static final EngineInfo NONE = new EngineInfo(EngineSource.NONE, null, null, null, false, null, null);

    public final EngineSource source;
    /** Absolute path of the binary, or null. */
    public final String path;
    /** Version reported by {@code --version}, e.g. "2026.10.3", or null if unknown. */
    public final String version;
    /** Device ABI the binary was chosen for, e.g. "arm64-v8a". */
    public final String abi;
    /** True when {@code --version} executed successfully. */
    public final boolean runnable;
    /** SHA-256 of the binary when known. */
    public final String sha256;
    /** Why the engine is not runnable, or null. */
    public final String error;

    public EngineInfo(EngineSource source, String path, String version, String abi, boolean runnable,
                      String sha256, String error) {
        this.source = source;
        this.path = path;
        this.version = version;
        this.abi = abi;
        this.runnable = runnable;
        this.sha256 = sha256;
        this.error = error;
    }

    public boolean isReady() {
        return runnable && path != null;
    }
}
