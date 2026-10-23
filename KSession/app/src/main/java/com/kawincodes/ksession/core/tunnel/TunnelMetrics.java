package com.kawincodes.ksession.core.tunnel;

/** Counters scraped from cloudflared's Prometheus endpoint. */
public final class TunnelMetrics {
    public static final TunnelMetrics EMPTY = new TunnelMetrics(0, 0, 0, 0, 0);

    public final long totalRequests;
    public final long requestErrors;
    public final long concurrentRequests;
    public final int haConnections;
    public final long scrapedAtMillis;

    public TunnelMetrics(long totalRequests, long requestErrors, long concurrentRequests,
                         int haConnections, long scrapedAtMillis) {
        this.totalRequests = totalRequests;
        this.requestErrors = requestErrors;
        this.concurrentRequests = concurrentRequests;
        this.haConnections = haConnections;
        this.scrapedAtMillis = scrapedAtMillis;
    }
}
