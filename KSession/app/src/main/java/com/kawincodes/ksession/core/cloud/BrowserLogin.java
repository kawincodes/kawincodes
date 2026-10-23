package com.kawincodes.ksession.core.cloud;

/**
 * Native implementation of {@code cloudflared tunnel login} (see docs/ARCHITECTURE.md §1.1):
 * build the authorize URL, then long-poll login.cloudflareaccess.org for the cert PEM.
 */
public final class BrowserLogin {

    public static final String LOGIN_URL = "https://dash.cloudflare.com/argotunnel";
    public static final String CALLBACK_STORE = "https://login.cloudflareaccess.org/";

    /** One login attempt. */
    public static final class Session {
        /** base64url(32 random bytes) with padding. */
        public final String key;
        /** URL the user opens in a browser / Custom Tab. */
        public final String authorizeUrl;
        /** URL polled for the certificate. */
        public final String pollUrl;

        public Session(String key, String authorizeUrl, String pollUrl) {
            this.key = key;
            this.authorizeUrl = authorizeUrl;
            this.pollUrl = pollUrl;
        }
    }

    private BrowserLogin() {
    }

    /** Creates a new session with a fresh random key. */
    public static Session newSession() {
        throw new UnsupportedOperationException("TODO cloud");
    }
}
