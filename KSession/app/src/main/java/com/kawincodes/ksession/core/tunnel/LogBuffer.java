package com.kawincodes.ksession.core.tunnel;

import com.kawincodes.ksession.core.process.LogLine;

import java.util.List;

/** Ring buffer of a tunnel's recent log lines (kept across restarts until the profile is deleted). */
public final class LogBuffer {

    /** Called on the main thread for each appended line. */
    public interface Listener {
        void onLine(LogLine line);

        default void onCleared() {
        }
    }

    public static final int CAPACITY = 2000;

    /** Snapshot copy, oldest first. */
    public List<LogLine> lines() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void append(LogLine line) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void clear() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void addListener(Listener l) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    public void removeListener(Listener l) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** All lines as plain text (for export/share). */
    public String asText() {
        throw new UnsupportedOperationException("TODO runtime");
    }
}
