package com.kawincodes.ksession.core.tunnel;

import java.util.Locale;

/** A registered connection between cloudflared and a Cloudflare data center. */
public final class EdgeConnection {
    public final int connIndex;
    /** Data center code from the log, e.g. "bom01" or "sin12". */
    public final String location;
    public final String ip;
    public final String protocol;
    public final String connectionId;
    public final long connectedAtMillis;

    public EdgeConnection(int connIndex, String location, String ip, String protocol,
                          String connectionId, long connectedAtMillis) {
        this.connIndex = connIndex;
        this.location = location == null ? "" : location;
        this.ip = ip == null ? "" : ip;
        this.protocol = protocol == null ? "" : protocol;
        this.connectionId = connectionId == null ? "" : connectionId;
        this.connectedAtMillis = connectedAtMillis;
    }

    /** IATA-style airport code of the data center, e.g. "BOM" for "bom01". */
    public String airportCode() {
        StringBuilder sb = new StringBuilder();
        for (char c : location.toCharArray()) {
            if (Character.isLetter(c)) sb.append(c);
            else break;
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}
