package com.kawincodes.ksession.core.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** User preferences backed by {@link SharedPreferences}. Safe to call from any thread. */
public final class Settings {

    /** Transport between cloudflared and the Cloudflare edge. */
    public enum Protocol {
        AUTO("auto"), QUIC("quic"), HTTP2("http2");

        public final String flag;

        Protocol(String flag) {
            this.flag = flag;
        }

        public static Protocol fromFlag(String flag) {
            for (Protocol p : values()) if (p.flag.equalsIgnoreCase(flag)) return p;
            return AUTO;
        }
    }

    public static final String KEY_PROTOCOL = "protocol";
    public static final String KEY_HA_CONNECTIONS = "ha_connections";
    public static final String KEY_AUTO_RESTART = "auto_restart";
    public static final String KEY_KEEP_AWAKE = "keep_awake";
    public static final String KEY_START_ON_BOOT = "start_on_boot";
    public static final String KEY_PREFER_INSTALLED_ENGINE = "prefer_installed_engine";
    public static final String KEY_APP_LOCK = "app_lock";
    public static final String KEY_POST_QUANTUM = "post_quantum";
    public static final String KEY_LOG_LEVEL = "log_level";
    public static final String KEY_POCKET_PORT = "pocket_port";
    public static final String KEY_NOTIFY_EVENTS = "notify_events";
    public static final String KEY_RUNNING_IDS = "running_ids";
    public static final String KEY_ONBOARDED = "onboarded";

    private final SharedPreferences prefs;

    public Settings(Context context) {
        prefs = context.getSharedPreferences("ksession_settings", Context.MODE_PRIVATE);
    }

    public SharedPreferences prefs() {
        return prefs;
    }

    public Protocol protocol() {
        return Protocol.fromFlag(prefs.getString(KEY_PROTOCOL, Protocol.AUTO.flag));
    }

    public void setProtocol(Protocol p) {
        prefs.edit().putString(KEY_PROTOCOL, p.flag).apply();
    }

    /** Connections per tunnel (1-4). cloudflared defaults to 4; 2 is kinder to mobile radios. */
    public int haConnections() {
        return clamp(prefs.getInt(KEY_HA_CONNECTIONS, 2), 1, 4);
    }

    public void setHaConnections(int n) {
        prefs.edit().putInt(KEY_HA_CONNECTIONS, clamp(n, 1, 4)).apply();
    }

    public boolean autoRestart() {
        return prefs.getBoolean(KEY_AUTO_RESTART, true);
    }

    public void setAutoRestart(boolean v) {
        prefs.edit().putBoolean(KEY_AUTO_RESTART, v).apply();
    }

    /** Hold a partial wake lock and Wi-Fi lock while tunnels run. */
    public boolean keepAwake() {
        return prefs.getBoolean(KEY_KEEP_AWAKE, false);
    }

    public void setKeepAwake(boolean v) {
        prefs.edit().putBoolean(KEY_KEEP_AWAKE, v).apply();
    }

    public boolean startOnBoot() {
        return prefs.getBoolean(KEY_START_ON_BOOT, false);
    }

    public void setStartOnBoot(boolean v) {
        prefs.edit().putBoolean(KEY_START_ON_BOOT, v).apply();
    }

    public boolean preferInstalledEngine() {
        return prefs.getBoolean(KEY_PREFER_INSTALLED_ENGINE, true);
    }

    public void setPreferInstalledEngine(boolean v) {
        prefs.edit().putBoolean(KEY_PREFER_INSTALLED_ENGINE, v).apply();
    }

    public boolean appLock() {
        return prefs.getBoolean(KEY_APP_LOCK, false);
    }

    public void setAppLock(boolean v) {
        prefs.edit().putBoolean(KEY_APP_LOCK, v).apply();
    }

    public boolean postQuantum() {
        return prefs.getBoolean(KEY_POST_QUANTUM, false);
    }

    public void setPostQuantum(boolean v) {
        prefs.edit().putBoolean(KEY_POST_QUANTUM, v).apply();
    }

    /** cloudflared --loglevel: debug, info, warn, error. */
    public String logLevel() {
        return prefs.getString(KEY_LOG_LEVEL, "info");
    }

    public void setLogLevel(String level) {
        prefs.edit().putString(KEY_LOG_LEVEL, level).apply();
    }

    public int pocketServerPort() {
        return clamp(prefs.getInt(KEY_POCKET_PORT, 8080), 1024, 65535);
    }

    public void setPocketServerPort(int port) {
        prefs.edit().putInt(KEY_POCKET_PORT, clamp(port, 1024, 65535)).apply();
    }

    public boolean notifyEvents() {
        return prefs.getBoolean(KEY_NOTIFY_EVENTS, true);
    }

    public void setNotifyEvents(boolean v) {
        prefs.edit().putBoolean(KEY_NOTIFY_EVENTS, v).apply();
    }

    public boolean onboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public void setOnboarded(boolean v) {
        prefs.edit().putBoolean(KEY_ONBOARDED, v).apply();
    }

    /** Profiles that were running, used to restore tunnels after the process is killed. */
    public Set<String> runningProfileIds() {
        return Collections.unmodifiableSet(new HashSet<>(prefs.getStringSet(KEY_RUNNING_IDS, Collections.emptySet())));
    }

    public void setRunningProfileIds(Set<String> ids) {
        prefs.edit().putStringSet(KEY_RUNNING_IDS, new HashSet<>(ids)).apply();
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
