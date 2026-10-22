package dev.kawin.hermesdeck.plugins;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs Hermes Agent on this phone through Termux's RUN_COMMAND service.
 *
 * Termux only accepts these intents when the user has set {@code allow-external-apps=true}
 * in ~/.termux/termux.properties and granted the RUN_COMMAND permission to this app.
 */
@CapacitorPlugin(
    name = "Termux",
    permissions = { @Permission(alias = TermuxPlugin.PERMISSION_ALIAS, strings = { TermuxPlugin.PERMISSION }) }
)
public class TermuxPlugin extends Plugin {

    static final String PERMISSION_ALIAS = "runCommand";
    static final String PERMISSION = "com.termux.permission.RUN_COMMAND";
    private static final String PACKAGE = "com.termux";
    private static final String SERVICE = "com.termux.app.RunCommandService";
    private static final String ACTION = "com.termux.RUN_COMMAND";
    private static final String BASH = "/data/data/com.termux/files/usr/bin/bash";
    private static final String HOME = "/data/data/com.termux/files/home";

    static final String EXTRA_EXECUTION_ID = "dev.kawin.hermesdeck.TERMUX_EXECUTION_ID";

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1000);
    private static final Map<Integer, PluginCall> PENDING = new ConcurrentHashMap<>();

    @PluginMethod
    public void status(PluginCall call) {
        call.resolve(statusObject());
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        if (!isInstalled()) {
            call.resolve(statusObject());
            return;
        }
        if (getPermissionState(PERMISSION_ALIAS) == PermissionState.GRANTED) {
            call.resolve(statusObject());
        } else {
            requestPermissionForAlias(PERMISSION_ALIAS, call, "permissionCallback");
        }
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        call.resolve(statusObject());
    }

    @PluginMethod
    public void run(PluginCall call) {
        String script = call.getString("script");
        if (!ready(call) || script == null) {
            if (script == null) call.reject("script is required");
            return;
        }
        int id = NEXT_ID.incrementAndGet();
        Intent resultIntent = new Intent(getContext(), TermuxResultReceiver.class);
        resultIntent.putExtra(EXTRA_EXECUTION_ID, id);
        int flags = PendingIntent.FLAG_ONE_SHOT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);
        PendingIntent pending = PendingIntent.getBroadcast(getContext(), id, resultIntent, flags);

        Intent intent = baseIntent();
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[] { "-s" });
        intent.putExtra("com.termux.RUN_COMMAND_STDIN", script);
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
        intent.putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", pending);
        PENDING.put(id, call);
        try {
            startService(intent);
        } catch (Exception e) {
            PENDING.remove(id);
            call.reject(describe(e));
            return;
        }
        int timeout = call.getInt("timeoutMs", 120000);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PluginCall waiting = PENDING.remove(id);
            if (waiting != null) waiting.reject("Termux did not answer. Check that allow-external-apps=true is set in ~/.termux/termux.properties.");
        }, timeout);
    }

    @PluginMethod
    public void runInTerminal(PluginCall call) {
        String script = call.getString("script");
        if (!ready(call) || script == null) {
            if (script == null) call.reject("script is required");
            return;
        }
        Intent intent = baseIntent();
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[] { "-c", script });
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
        intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0"); // new session, bring Termux to front
        try {
            startService(intent);
            Intent launch = getContext().getPackageManager().getLaunchIntentForPackage(PACKAGE);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(launch);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject(describe(e));
        }
    }

    /** Called by {@link TermuxResultReceiver} with Termux's result bundle. */
    static void deliver(int id, Bundle result) {
        PluginCall call = PENDING.remove(id);
        if (call == null) return;
        JSObject out = new JSObject();
        out.put("stdout", result == null ? "" : result.getString("stdout", ""));
        out.put("stderr", result == null ? "" : result.getString("stderr", ""));
        out.put("exitCode", result == null ? -1 : result.getInt("exitCode", -1));
        out.put("errmsg", result == null ? "No result from Termux" : result.getString("errmsg", ""));
        call.resolve(out);
    }

    private boolean ready(PluginCall call) {
        if (!isInstalled()) {
            call.reject("Termux is not installed. Install it from F-Droid or GitHub (the Play Store build is outdated).");
            return false;
        }
        if (getPermissionState(PERMISSION_ALIAS) != PermissionState.GRANTED) {
            call.reject("Grant the \"Run commands in Termux environment\" permission first.");
            return false;
        }
        return true;
    }

    private Intent baseIntent() {
        Intent intent = new Intent(ACTION);
        intent.setClassName(PACKAGE, SERVICE);
        intent.putExtra("com.termux.RUN_COMMAND_PATH", BASH);
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", HOME);
        return intent;
    }

    private void startService(Intent intent) {
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private boolean isInstalled() {
        try {
            getContext().getPackageManager().getPackageInfo(PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private JSObject statusObject() {
        JSObject out = new JSObject();
        boolean installed = isInstalled();
        out.put("installed", installed);
        out.put("permissionGranted", installed && getPermissionState(PERMISSION_ALIAS) == PermissionState.GRANTED);
        return out;
    }

    private static String describe(Exception e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (e instanceof SecurityException) {
            return "Termux refused the command. Enable allow-external-apps in ~/.termux/termux.properties. (" + msg + ")";
        }
        return msg;
    }
}
