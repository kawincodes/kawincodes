package com.kawincodes.ksession.ui.common;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.kawincodes.ksession.R;
import com.kawincodes.ksession.core.tunnel.TunnelStatus;
import com.kawincodes.ksession.core.tunnel.TunnelType;

/** Shared colors, labels and icons for tunnel status and type. */
public final class StatusStyle {
    private StatusStyle() {
    }

    @ColorInt
    public static int color(Context c, TunnelStatus s) {
        int res;
        switch (s) {
            case ONLINE: res = R.color.status_online; break;
            case PREPARING:
            case CONNECTING: res = R.color.status_connecting; break;
            case DEGRADED: res = R.color.status_degraded; break;
            case STOPPING: res = R.color.status_stopping; break;
            case ERROR: res = R.color.status_error; break;
            default: res = R.color.status_stopped; break;
        }
        return ContextCompat.getColor(c, res);
    }

    @StringRes
    public static int label(TunnelStatus s) {
        switch (s) {
            case PREPARING: return R.string.status_preparing;
            case CONNECTING: return R.string.status_connecting;
            case ONLINE: return R.string.status_online;
            case DEGRADED: return R.string.status_degraded;
            case STOPPING: return R.string.status_stopping;
            case ERROR: return R.string.status_error;
            default: return R.string.status_stopped;
        }
    }

    @DrawableRes
    public static int icon(TunnelStatus s) {
        switch (s) {
            case ONLINE: return R.drawable.ic_radio_tower;
            case PREPARING:
            case CONNECTING: return R.drawable.ic_satellite_dish;
            case DEGRADED: return R.drawable.ic_triangle_alert;
            case ERROR: return R.drawable.ic_circle_alert;
            case STOPPING: return R.drawable.ic_power;
            default: return R.drawable.ic_cloud_off;
        }
    }

    @StringRes
    public static int typeLabel(TunnelType t) {
        switch (t) {
            case QUICK: return R.string.type_quick;
            case TOKEN: return R.string.type_token;
            default: return R.string.type_managed;
        }
    }

    @DrawableRes
    public static int typeIcon(TunnelType t) {
        switch (t) {
            case QUICK: return R.drawable.ic_zap;
            case TOKEN: return R.drawable.ic_key_round;
            default: return R.drawable.ic_waypoints;
        }
    }
}
