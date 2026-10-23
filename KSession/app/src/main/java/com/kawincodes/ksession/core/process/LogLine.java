package com.kawincodes.ksession.core.process;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One parsed cloudflared log line, e.g. {@code 2026-10-23T04:44:56Z INF Registered tunnel connection connIndex=0 ...}. */
public final class LogLine {
    /** Device time the line was read. */
    public final long receivedAtMillis;
    /** Timestamp text from cloudflared ("2026-10-23T04:44:56Z"), or null for unstructured lines. */
    public final String time;
    public final LogLevel level;
    /** Message without the key=value fields. */
    public final String message;
    /** Trailing key=value fields in order; quoted values are unquoted. */
    public final Map<String, String> fields;
    /** The original line. */
    public final String raw;

    public LogLine(long receivedAtMillis, String time, LogLevel level, String message,
                   Map<String, String> fields, String raw) {
        this.receivedAtMillis = receivedAtMillis;
        this.time = time;
        this.level = level;
        this.message = message;
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        this.raw = raw;
    }

    public String field(String key) {
        return fields.get(key);
    }
}
