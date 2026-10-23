package com.kawincodes.ksession.ui;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Single activity: edge-to-edge, bottom navigation (Chat, Radar, Tunnels, Cloud) hosting
 * ChatFragment, RadarFragment, TunnelsFragment and CloudFragment; app lock gate; notification
 * permission request. Hides the bottom bar while the keyboard is open.
 */
public class MainActivity extends AppCompatActivity implements Navigator {
    /** Optional extra: nav id to open. */
    public static final String EXTRA_TAB = "tab";

    @Override
    public void selectTab(int navId) {
        // TODO shell
    }
}
