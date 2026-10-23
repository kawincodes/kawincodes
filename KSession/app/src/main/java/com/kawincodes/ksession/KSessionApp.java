package com.kawincodes.ksession;

import android.app.Application;
import android.content.Context;

import com.kawincodes.ksession.service.NotificationHelper;

/** Application entry point. Owns the {@link AppGraph} of app-scoped services. */
public class KSessionApp extends Application {

    private AppGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        graph = new AppGraph(this);
        NotificationHelper.createChannels(this);
    }

    public AppGraph graph() {
        return graph;
    }

    /** Returns the app graph from any context. */
    public static AppGraph graph(Context context) {
        return ((KSessionApp) context.getApplicationContext()).graph();
    }
}
