package com.kawincodes.ksession.ui.common;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kawincodes.ksession.R;

import java.util.Locale;

/** Small UI helpers shared by all screens. */
public final class Ui {
    private Ui() {
    }

    public static int dp(Context c, float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics()));
    }

    /** Copies text; marks it sensitive when {@code sensitive} (tokens). Shows a toast below Android 13. */
    public static void copy(Context c, String label, String text, boolean sensitive) {
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        ClipData clip = ClipData.newPlainText(label, text);
        if (sensitive && Build.VERSION.SDK_INT >= 24) {
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean("android.content.extra.IS_SENSITIVE", true);
            clip.getDescription().setExtras(extras);
        }
        cm.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT < 33) {
            Toast.makeText(c, R.string.copied, Toast.LENGTH_SHORT).show();
        }
    }

    /** Opens a URL in a dark Custom Tab, falling back to any browser. */
    public static void openUrl(Context c, String url) {
        try {
            int bg = ContextCompat.getColor(c, R.color.ks_surface);
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(bg).setNavigationBarColor(bg).build())
                    .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                    .setShowTitle(true)
                    .build();
            if (!(c instanceof android.app.Activity)) intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.launchUrl(c, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (!(c instanceof android.app.Activity)) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(c, R.string.no_browser, Toast.LENGTH_LONG).show();
            }
        }
    }

    /** System share sheet for plain text. */
    public static void share(Context c, String subject, String text) {
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, subject).putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(send, subject);
        if (!(c instanceof android.app.Activity)) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(chooser);
    }

    public static void snack(View anchor, CharSequence msg) {
        Snackbar.make(anchor, msg, Snackbar.LENGTH_LONG).show();
    }

    /** "2h 05m", "4m 12s", "38s". */
    public static String duration(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format(Locale.US, "%dh %02dm", h, m);
        if (m > 0) return String.format(Locale.US, "%dm %02ds", m, sec);
        return sec + "s";
    }

    /** "1.2k", "3.4M". */
    public static String compactNumber(long n) {
        if (n < 1000) return Long.toString(n);
        if (n < 1_000_000) return String.format(Locale.US, "%.1fk", n / 1000.0);
        return String.format(Locale.US, "%.1fM", n / 1_000_000.0);
    }

    /** "37.6 MB". */
    public static String bytes(long b) {
        if (b < 1024) return b + " B";
        if (b < 1024 * 1024) return String.format(Locale.US, "%.1f KB", b / 1024.0);
        return String.format(Locale.US, "%.1f MB", b / (1024.0 * 1024));
    }
}
