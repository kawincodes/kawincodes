package com.kawincodes.ksession.core.engine;

/** Where the active cloudflared binary comes from. */
public enum EngineSource {
    /** Packed in the APK as lib/<abi>/libcloudflared.so and extracted to nativeLibraryDir. */
    BUNDLED,
    /** Downloaded inside the app to filesDir/engine/cloudflared. */
    INSTALLED,
    /** No runnable engine found. */
    NONE
}
