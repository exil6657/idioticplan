package com.zenith.client.flipping.break_;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Actions to perform immediately after a scheduled break (e.g. check orders,
 * rescan AH, refresh purse). FIFO queue drained by FlipEngine on break end.
 */
public final class PostBreakActionQueue {

    private static final PostBreakActionQueue INSTANCE = new PostBreakActionQueue();
    public static PostBreakActionQueue getInstance() { return INSTANCE; }

    private final Queue<Runnable> actions = new LinkedList<>();

    private PostBreakActionQueue() {}

    public void enqueue(Runnable r) { actions.offer(r); }

    public void drain() {
        while (true) {
            Runnable r = actions.poll();
            if (r == null) return;
            try { r.run(); }
            catch (Throwable t) { com.zenith.client.ZenithClient.LOGGER.error("[PostBreak] action failed", t); }
        }
    }
}
