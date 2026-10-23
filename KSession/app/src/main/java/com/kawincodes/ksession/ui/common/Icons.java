package com.kawincodes.ksession.ui.common;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.kawincodes.ksession.R;

import java.util.HashMap;
import java.util.Map;

/** Maps persisted icon keys (Lucide names, e.g. "plug-zap") to drawables. */
public final class Icons {
    private static final Map<String, Integer> MAP = new HashMap<>();

    static {
        MAP.put("activity", R.drawable.ic_activity);
        MAP.put("arrow-up-right", R.drawable.ic_arrow_up_right);
        MAP.put("bot", R.drawable.ic_bot);
        MAP.put("check", R.drawable.ic_check);
        MAP.put("circle-alert", R.drawable.ic_circle_alert);
        MAP.put("circle-check", R.drawable.ic_circle_check);
        MAP.put("circle-stop", R.drawable.ic_circle_stop);
        MAP.put("clipboard-paste", R.drawable.ic_clipboard_paste);
        MAP.put("clock", R.drawable.ic_clock);
        MAP.put("cloud", R.drawable.ic_cloud);
        MAP.put("cloud-cog", R.drawable.ic_cloud_cog);
        MAP.put("cloud-download", R.drawable.ic_cloud_download);
        MAP.put("cloud-off", R.drawable.ic_cloud_off);
        MAP.put("copy", R.drawable.ic_copy);
        MAP.put("cpu", R.drawable.ic_cpu);
        MAP.put("download", R.drawable.ic_download);
        MAP.put("earth", R.drawable.ic_earth);
        MAP.put("eraser", R.drawable.ic_eraser);
        MAP.put("external-link", R.drawable.ic_external_link);
        MAP.put("file", R.drawable.ic_file);
        MAP.put("fingerprint", R.drawable.ic_fingerprint);
        MAP.put("folder-open", R.drawable.ic_folder_open);
        MAP.put("gauge", R.drawable.ic_gauge);
        MAP.put("globe", R.drawable.ic_globe);
        MAP.put("hard-drive", R.drawable.ic_hard_drive);
        MAP.put("history", R.drawable.ic_history);
        MAP.put("info", R.drawable.ic_info);
        MAP.put("key-round", R.drawable.ic_key_round);
        MAP.put("layers", R.drawable.ic_layers);
        MAP.put("link", R.drawable.ic_link);
        MAP.put("list", R.drawable.ic_list);
        MAP.put("lock", R.drawable.ic_lock);
        MAP.put("log-in", R.drawable.ic_log_in);
        MAP.put("log-out", R.drawable.ic_log_out);
        MAP.put("message-circle", R.drawable.ic_message_circle);
        MAP.put("network", R.drawable.ic_network);
        MAP.put("package", R.drawable.ic_package);
        MAP.put("package-check", R.drawable.ic_package_check);
        MAP.put("pause", R.drawable.ic_pause);
        MAP.put("play", R.drawable.ic_play);
        MAP.put("plug", R.drawable.ic_plug);
        MAP.put("plug-zap", R.drawable.ic_plug_zap);
        MAP.put("plus", R.drawable.ic_plus);
        MAP.put("power", R.drawable.ic_power);
        MAP.put("qr-code", R.drawable.ic_qr_code);
        MAP.put("radar", R.drawable.ic_radar);
        MAP.put("radio-tower", R.drawable.ic_radio_tower);
        MAP.put("refresh-cw", R.drawable.ic_refresh_cw);
        MAP.put("rocket", R.drawable.ic_rocket);
        MAP.put("route", R.drawable.ic_route);
        MAP.put("satellite-dish", R.drawable.ic_satellite_dish);
        MAP.put("scroll-text", R.drawable.ic_scroll_text);
        MAP.put("search", R.drawable.ic_search);
        MAP.put("send", R.drawable.ic_send);
        MAP.put("server", R.drawable.ic_server);
        MAP.put("settings", R.drawable.ic_settings);
        MAP.put("share-2", R.drawable.ic_share_2);
        MAP.put("shield-alert", R.drawable.ic_shield_alert);
        MAP.put("shield-check", R.drawable.ic_shield_check);
        MAP.put("smartphone", R.drawable.ic_smartphone);
        MAP.put("sparkles", R.drawable.ic_sparkles);
        MAP.put("square", R.drawable.ic_square);
        MAP.put("terminal", R.drawable.ic_terminal);
        MAP.put("timer", R.drawable.ic_timer);
        MAP.put("trash-2", R.drawable.ic_trash_2);
        MAP.put("triangle-alert", R.drawable.ic_triangle_alert);
        MAP.put("upload", R.drawable.ic_upload);
        MAP.put("user", R.drawable.ic_user);
        MAP.put("waypoints", R.drawable.ic_waypoints);
        MAP.put("wifi", R.drawable.ic_wifi);
        MAP.put("wifi-off", R.drawable.ic_wifi_off);
        MAP.put("x", R.drawable.ic_x);
        MAP.put("zap", R.drawable.ic_zap);
    }

    private Icons() {
    }

    /** Drawable for a Lucide key; falls back to {@code sparkles}. */
    @DrawableRes
    public static int forKey(@Nullable String key) {
        Integer res = key == null ? null : MAP.get(key);
        return res == null ? R.drawable.ic_sparkles : res;
    }
}
