package com.kawincodes.ksession.chat.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Result of parsing user input. */
public final class ParsedCommand {
    public final CommandType type;
    public final Map<String, String> args;
    public final String raw;

    public ParsedCommand(CommandType type, Map<String, String> args, String raw) {
        this.type = type;
        this.args = Collections.unmodifiableMap(new LinkedHashMap<>(args == null ? Collections.emptyMap() : args));
        this.raw = raw == null ? "" : raw;
    }

    public String arg(String key) {
        return args.get(key);
    }

    public String arg(String key, String fallback) {
        String v = args.get(key);
        return v == null || v.isEmpty() ? fallback : v;
    }

    @Override
    public String toString() {
        return type + " " + args;
    }
}
