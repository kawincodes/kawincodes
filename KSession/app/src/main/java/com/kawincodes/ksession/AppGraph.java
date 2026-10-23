package com.kawincodes.ksession;

import android.content.Context;

import com.kawincodes.ksession.chat.ChatEngine;
import com.kawincodes.ksession.core.cloud.AccountManager;
import com.kawincodes.ksession.core.cloud.TunnelProvisioner;
import com.kawincodes.ksession.core.engine.EngineManager;
import com.kawincodes.ksession.core.security.CredentialStore;
import com.kawincodes.ksession.core.server.PocketServer;
import com.kawincodes.ksession.core.settings.Settings;
import com.kawincodes.ksession.core.tunnel.TunnelController;
import com.kawincodes.ksession.core.tunnel.TunnelRepository;
import com.kawincodes.ksession.util.AppExecutors;
import com.kawincodes.ksession.util.HttpClient;
import com.kawincodes.ksession.util.UrlConnectionHttpClient;

/**
 * Hand-rolled dependency graph. Every service is a lazily created, app-scoped singleton that
 * receives this graph in its constructor and looks up its own dependencies on demand.
 */
public final class AppGraph {

    private final Context context;
    private final AppExecutors executors = new AppExecutors();

    private Settings settings;
    private CredentialStore credentials;
    private HttpClient http;
    private TunnelRepository tunnels;
    private TunnelController controller;
    private EngineManager engine;
    private AccountManager accounts;
    private TunnelProvisioner provisioner;
    private PocketServer pocketServer;
    private ChatEngine chat;

    public AppGraph(Context context) {
        this.context = context.getApplicationContext();
    }

    public Context context() {
        return context;
    }

    public AppExecutors executors() {
        return executors;
    }

    public synchronized Settings settings() {
        if (settings == null) settings = new Settings(context);
        return settings;
    }

    public synchronized CredentialStore credentials() {
        if (credentials == null) credentials = new CredentialStore(context);
        return credentials;
    }

    public synchronized HttpClient http() {
        if (http == null) http = new UrlConnectionHttpClient();
        return http;
    }

    public synchronized TunnelRepository tunnels() {
        if (tunnels == null) tunnels = new TunnelRepository(this);
        return tunnels;
    }

    public synchronized TunnelController controller() {
        if (controller == null) controller = new TunnelController(this);
        return controller;
    }

    public synchronized EngineManager engine() {
        if (engine == null) engine = new EngineManager(this);
        return engine;
    }

    public synchronized AccountManager accounts() {
        if (accounts == null) accounts = new AccountManager(this);
        return accounts;
    }

    public synchronized TunnelProvisioner provisioner() {
        if (provisioner == null) provisioner = new TunnelProvisioner(this);
        return provisioner;
    }

    public synchronized PocketServer pocketServer() {
        if (pocketServer == null) pocketServer = new PocketServer(this);
        return pocketServer;
    }

    public synchronized ChatEngine chat() {
        if (chat == null) chat = new ChatEngine(this);
        return chat;
    }
}
