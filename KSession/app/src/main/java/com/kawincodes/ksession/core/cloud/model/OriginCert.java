package com.kawincodes.ksession.core.cloud.model;

/** Contents of cloudflared's cert.pem ("ARGO TUNNEL TOKEN" block) obtained by browser login. */
public final class OriginCert {
    public final String zoneId;
    public final String accountId;
    public final String apiToken;
    /** "fed" for FedRAMP, otherwise null. */
    public final String endpoint;
    /** The original PEM text. */
    public final String pem;

    public OriginCert(String zoneId, String accountId, String apiToken, String endpoint, String pem) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.apiToken = apiToken;
        this.endpoint = endpoint;
        this.pem = pem;
    }
}
