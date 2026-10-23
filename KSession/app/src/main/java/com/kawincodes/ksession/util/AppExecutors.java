package com.kawincodes.ksession.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared executors: an elastic pool for blocking I/O and the main thread. */
public final class AppExecutors {

    private final ExecutorService io = Executors.newCachedThreadPool(new NamedFactory("ksession-io"));
    private final Handler main = new Handler(Looper.getMainLooper());

    /** Pool for blocking work (network, disk, process management). */
    public ExecutorService io() {
        return io;
    }

    /** Runs {@code r} on the main thread (immediately if already there). */
    public void main(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) r.run();
        else main.post(r);
    }

    public void mainDelayed(Runnable r, long delayMillis) {
        main.postDelayed(r, delayMillis);
    }

    public void cancelMain(Runnable r) {
        main.removeCallbacks(r);
    }

    private static final class NamedFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger n = new AtomicInteger();

        NamedFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
