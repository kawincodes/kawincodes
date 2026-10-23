package com.kawincodes.ksession.util;

import java.io.IOException;

/** Minimal blocking HTTP client so services can be unit tested with fakes. */
public interface HttpClient {

    /** Executes the request on the calling thread. Non-2xx responses are returned, not thrown. */
    HttpResponse execute(HttpRequest request) throws IOException;

    /** Streams a GET response body to {@code sink}, reporting progress. Used for large downloads. */
    void download(String url, java.io.OutputStream sink, ProgressListener progress) throws IOException;

    interface ProgressListener {
        /** {@code total} is -1 when unknown. Called on the downloading thread. */
        void onProgress(long bytes, long total);

        /** Returning true aborts the download with an {@link java.io.InterruptedIOException}. */
        default boolean isCancelled() {
            return false;
        }
    }
}
