package com.kawincodes.ksession.core.cloud;

import com.kawincodes.ksession.AppGraph;
import com.kawincodes.ksession.core.cloud.model.CfTunnel;
import com.kawincodes.ksession.core.tunnel.IngressRule;
import com.kawincodes.ksession.core.tunnel.TunnelProfile;
import com.kawincodes.ksession.util.Callback;

import java.util.List;

/**
 * Creates, imports and deletes tunnel profiles, talking to Cloudflare when needed and storing
 * secrets in CredentialStore. Callbacks run on the main thread.
 */
public final class TunnelProvisioner {

    private final AppGraph graph;

    public TunnelProvisioner(AppGraph graph) {
        this.graph = graph;
    }

    /**
     * Saves a QUICK profile (no network). {@code target} may be a port ("3000"), host:port or URL;
     * it is normalised to an origin URL (localhost becomes 127.0.0.1). Name null = unique "quick".
     */
    public TunnelProfile createQuick(String name, String target) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Validates a connector token, stores it and saves a TOKEN profile. */
    public void addToken(String name, String token, Callback<TunnelProfile> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /**
     * Creates a named tunnel in the active account with a fresh 32-byte secret, routes every rule
     * hostname (DNS CNAME to <id>.cfargotunnel.com), stores credentials and saves a MANAGED profile.
     * Requires sign-in.
     */
    public void createManaged(String name, List<IngressRule> rules, Callback<TunnelProfile> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Fetches the connector token of an existing remote tunnel and saves a TOKEN profile. */
    public void importRemote(CfTunnel tunnel, Callback<TunnelProfile> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Routes a new hostname to a MANAGED tunnel and adds the rule (restart to apply). */
    public void addHostname(TunnelProfile profile, IngressRule rule, Callback<TunnelProfile> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Converts user input ("3000", "localhost:5173", "https://10.0.0.2:8443") into an origin URL. */
    public static String normalizeOrigin(String target) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /**
     * Stops and deletes a profile and its secrets; optionally deletes the tunnel on Cloudflare
     * (MANAGED/TOKEN, requires sign-in; cleans up connections first).
     */
    public void delete(TunnelProfile profile, boolean alsoDeleteOnCloudflare, Callback<Void> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }
}
