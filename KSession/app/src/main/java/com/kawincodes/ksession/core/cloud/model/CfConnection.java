package com.kawincodes.ksession.core.cloud.model;

/** An active connector connection reported by the API. */
public final class CfConnection {
    public final String id;
    /** Data center, e.g. "BOM". */
    public final String coloName;
    public final String originIp;
    public final String openedAt;
    public final String clientVersion;

    public CfConnection(String id, String coloName, String originIp, String openedAt, String clientVersion) {
        this.id = id;
        this.coloName = coloName;
        this.originIp = originIp;
        this.openedAt = openedAt;
        this.clientVersion = clientVersion;
    }
}
