package com.kawincodes.ksession.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kawincodes.ksession.R;

/** Dialog that shows a URL as a QR code with copy/share/open actions. */
public final class QrDialog {
    private QrDialog() {
    }

    public static void show(Context c, String title, String url) {
        View v = LayoutInflater.from(c).inflate(R.layout.dialog_qr, null, false);
        ImageView img = v.findViewById(R.id.qr_image);
        TextView text = v.findViewById(R.id.qr_url);
        int size = Ui.dp(c, 240);
        Bitmap bmp = QrCodes.render(url, size, 0xFF05070D, 0xFFFFFFFF);
        img.setImageBitmap(bmp);
        text.setText(url);
        new MaterialAlertDialogBuilder(c)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(R.string.action_copy, (d, w) -> Ui.copy(c, title, url, false))
                .setNeutralButton(R.string.action_share, (d, w) -> Ui.share(c, title, url))
                .setNegativeButton(R.string.action_open, (d, w) -> Ui.openUrl(c, url))
                .show();
    }
}
