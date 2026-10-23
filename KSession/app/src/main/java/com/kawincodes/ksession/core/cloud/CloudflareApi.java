package com.kawincodes.ksession.core.cloud;

import com.kawincodes.ksession.util.HttpClient;

/**
 * Blocking Cloudflare API v4 client (call from background threads). Methods throw
 * {@code CloudflareApiException} for API errors and IOException for transport errors.
 * See docs/ARCHITECTURE.md §1.1 for the endpoints.
 */
public final class CloudflareApi {
    public static final String BASE_URL = "https://api.cloudflare.com/client/v4";

    private final HttpClient http;
    private final String token;

    public CloudflareApi(HttpClient http, String token) {
        this.http = http;
        this.token = token;
    }
    // TODO cloud: verifyToken, listAccounts, listZones, getZone, findZoneFor, listTunnels,
    // createTunnel, getTunnelToken, listConnections, cleanupConnections, deleteTunnel, routeDns
}
