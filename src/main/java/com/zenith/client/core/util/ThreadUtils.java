package com.zenith.client.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread-pool factory. <b>Never use {@code Thread.sleep} on the render/tick thread.</b> */
public final class ThreadUtils {

    private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);
    private static volatile ScheduledExecutorService SCHEDULER;
    private static volatile ExecutorService IO_POOL;

    private ThreadUtils() {}

    /** Shared scheduled executor for periodic macro tasks (runs on daemon threads). */
    public static ScheduledExecutorService scheduler() {
        ScheduledExecutorService s = SCHEDULER;
        if (s == null) {
            synchronized (ThreadUtils.class) {
                s = SCHEDULER;
                if (s == null) {
                    s = Executors.newScheduledThreadPool(2, namedFactory("zenith-sched-"));
                    SCHEDULER = s;
                }
            }
        }
        return s;
    }

    /** Shared I/O pool for config writes, web lookups, Discord RPC calls. */
    public static ExecutorService ioPool() {
        ExecutorService p = IO_POOL;
        if (p == null) {
            synchronized (ThreadUtils.class) {
                p = IO_POOL;
                if (p == null) {
                    p = Executors.newCachedThreadPool(namedFactory("zenith-io-"));
                    IO_POOL = p;
                }
            }
        }
        return p;
    }

    public static void runAsync(Runnable r) {
        ioPool().submit(r);
    }

    public static void shutdown() {
        if (SCHEDULER != null) SCHEDULER.shutdownNow();
        if (IO_POOL != null) IO_POOL.shutdownNow();
    }

    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger n = new AtomicInteger(0);
        return r -> {
            Thread t = new Thread(r, prefix + n.incrementAndGet());
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((th, ex) ->
                com.zenith.client.ZenithClient.LOGGER.error("[Thread] {}", th.getName(), ex));
            return t;
        };
    }
}
