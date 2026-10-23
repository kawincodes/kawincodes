package com.kawincodes.ksession.ui;

/** Implemented by MainActivity so fragments can switch bottom-navigation tabs. */
public interface Navigator {
    /** One of R.id.nav_chat, R.id.nav_radar, R.id.nav_tunnels, R.id.nav_cloud. */
    void selectTab(int navId);
}
