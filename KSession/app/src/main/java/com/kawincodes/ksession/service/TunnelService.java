package com.kawincodes.ksession.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Foreground service (type specialUse) that keeps the process alive while tunnels run. It owns no
 * tunnel logic: TunnelController calls {@link #ensureRunning(Context)} when a tunnel starts and
 * the service stops itself when no tunnel is active.
 */
public class TunnelService extends Service {
    public static final String ACTION_STOP_ALL = "com.kawincodes.ksession.action.STOP_ALL";

    /** Starts the service in the foreground if it is not running. */
    public static void ensureRunning(Context context) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
