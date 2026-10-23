package com.kawincodes.ksession.core.server;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;
import com.kawincodes.ksession.util.Callback;

import java.util.Collections;
import java.util.List;

/**
 * Built-in HTTP server on 127.0.0.1 so a tunnel can be tested without another app. Serves a
 * radar-styled hello page at "/", JSON status at "/api/status" and shared files at
 * "/share/<token>/<name>" (unguessable capability URLs; nothing lists them publicly).
 */
public final class PocketServer {

    /** A file shared through the server. */
    public static final class SharedFile {
        public final String token;
        public final String name;
        public final Uri uri;
        public final long size;
        public final String mimeType;

        public SharedFile(String token, String name, Uri uri, long size, String mimeType) {
            this.token = token;
            this.name = name;
            this.uri = uri;
            this.size = size;
            this.mimeType = mimeType;
        }

        /** Path below the server root, e.g. "/share/Ab3.../photo.jpg" (URL-encoded name). */
        public String path() {
            throw new UnsupportedOperationException("TODO cloud");
        }
    }

    /** Server state for the UI. */
    public static final class State {
        public static final State STOPPED = new State(false, 0, 0, null, Collections.emptyList());

        public final boolean running;
        public final int port;
        public final long requestCount;
        public final String error;
        public final List<SharedFile> shared;

        public State(boolean running, int port, long requestCount, String error, List<SharedFile> shared) {
            this.running = running;
            this.port = port;
            this.requestCount = requestCount;
            this.error = error;
            this.shared = shared;
        }
    }

    private final AppGraph graph;
    private final MutableLiveData<State> state = new MutableLiveData<>(State.STOPPED);

    public PocketServer(AppGraph graph) {
        this.graph = graph;
    }

    public LiveData<State> state() {
        return state;
    }

    /** Binds 127.0.0.1:port; delivers the bound port. */
    public void start(int port, Callback<Integer> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void stop() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public int port() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** "http://127.0.0.1:<port>" (also valid while stopped, using the configured port). */
    public String localUrl() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Shares a document (takes a persistable read permission when possible). */
    public SharedFile share(Uri uri) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void unshare(String token) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public List<SharedFile> sharedFiles() {
        throw new UnsupportedOperationException("TODO cloud");
    }
}
