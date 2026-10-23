package com.kawincodes.ksession.core.cloud.model;

/** How the app is signed in to Cloudflare. */
public enum AuthMode {
    NONE,
    /** Browser login (cloudflared's cert.pem flow): token scoped to one account and zone. */
    BROWSER_CERT,
    /** User-supplied API token. */
    API_TOKEN
}
