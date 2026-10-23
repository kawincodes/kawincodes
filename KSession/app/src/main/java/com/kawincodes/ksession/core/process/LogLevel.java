package com.kawincodes.ksession.core.process;

/** cloudflared log levels (DBG, INF, WRN, ERR, FTL). */
public enum LogLevel {
    DEBUG, INFO, WARN, ERROR, FATAL, UNKNOWN;

    public static LogLevel fromCode(String code) {
        if (code == null) return UNKNOWN;
        switch (code) {
            case "DBG": return DEBUG;
            case "INF": return INFO;
            case "WRN": return WARN;
            case "ERR": return ERROR;
            case "FTL": return FATAL;
            default: return UNKNOWN;
        }
    }
}
