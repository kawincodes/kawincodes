package com.kawincodes.ksession.service;

import android.content.Context;

/** Notification channels and builders for tunnels. */
public final class NotificationHelper {
    /** Ongoing foreground notification (low importance). */
    public static final String CHANNEL_TUNNELS = "tunnels";
    /** Tunnel online/offline/error alerts. */
    public static final String CHANNEL_EVENTS = "events";
    public static final int FOREGROUND_ID = 7001;

    private NotificationHelper() {
    }

    /** Creates channels; called from Application.onCreate. */
    public static void createChannels(Context context) {
        // TODO runtime
    }
}
