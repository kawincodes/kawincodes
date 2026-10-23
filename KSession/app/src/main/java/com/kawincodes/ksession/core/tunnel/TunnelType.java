package com.kawincodes.ksession.core.tunnel;

/** How a tunnel is provisioned and run. */
public enum TunnelType {
    /** Account-less TryCloudflare tunnel; a new random *.trycloudflare.com URL on every start. */
    QUICK,
    /** Connector token (pasted from the dashboard or fetched by API); ingress is managed remotely. */
    TOKEN,
    /** Named tunnel created by KSession; ingress rules live in the profile (local config). */
    MANAGED
}
