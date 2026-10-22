package dev.kawin.hermesdeck.plugins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Receives the result bundle Termux sends back through the PendingIntent passed with RUN_COMMAND. */
public class TermuxResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(TermuxPlugin.EXTRA_EXECUTION_ID, -1);
        TermuxPlugin.deliver(id, intent.getBundleExtra("result"));
    }
}
