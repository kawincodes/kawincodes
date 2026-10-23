package com.kawincodes.ksession.ui.tunnels;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/** Bottom sheet to create a Quick, Token or Managed tunnel, or import one from Cloudflare. */
public class CreateTunnelSheet extends BottomSheetDialogFragment {
    public static final String TAG = "create_tunnel";

    /** @param presetType "QUICK", "TOKEN", "MANAGED" or null. */
    public static void open(FragmentManager fm, @Nullable String presetType) {
        // TODO screens
    }
}
