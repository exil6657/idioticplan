package com.zenith.client.engine.eyes;

import com.zenith.client.ZenithClient;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Priority queue of {@link RotationRequest}s for the eyes engine.
 *
 * <p>Preemption rules:
 * <ul>
 *   <li>A new FAILSAFE request cancels everything.</li>
 *   <li>A non-preemptible current request finishes regardless of priority
 *       (but a FAILSAFE still wins).</li>
 *   <li>A higher-priority request pre-empts a lower-priority preemptible one.</li>
 *   <li>BACKGROUND wander requests are evicted if the queue is full.</li>
 * </ul>
 */
public final class RotationQueue {

    private static final int MAX_QUEUED = 32;

    private final PriorityQueue<RotationRequest> queue =
            new PriorityQueue<>(Comparator.comparingInt((RotationRequest r) -> r.priority.value).reversed());

    private RotationRequest current;

    /** Enqueue a request; returns the request that was actually admitted (may be @{code req} or null). */
    public synchronized RotationRequest submit(RotationRequest req) {
        if (req == null) return null;
        req.submittedAt = System.currentTimeMillis();

        // Handle preemption vs current.
        if (current != null) {
            boolean preemptCurrent =
                    req.priority.value > current.priority.value &&
                    (current.preemptible || req.priority == RotationRequest.Priority.FAILSAFE);
            if (preemptCurrent) {
                cancelCurrent("preempted by " + req.priority);
            }
        }
        queue.offer(req);
        trimOverflow();
        return req;
    }

    public synchronized RotationRequest peekCurrent() { return current; }

    public synchronized void setCurrent(RotationRequest req) { this.current = req; }

    public synchronized RotationRequest pollNext() {
        current = queue.poll();
        if (current != null) current.startedAt = System.currentTimeMillis();
        return current;
    }

    public synchronized void cancelCurrent(String reason) {
        if (current == null) return;
        current.cancelled = true;
        current.cancelReason = reason;
        if (current.callback != null) {
            try { current.callback.complete(false, reason); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[RotationQueue] callback error", t);
            }
        }
        current = null;
    }

    public synchronized void clear() {
        cancelCurrent("cleared");
        for (RotationRequest r : queue) {
            r.cancelled = true;
            r.cancelReason = "cleared";
        }
        queue.clear();
    }

    public synchronized void completeCurrent() {
        if (current == null) return;
        if (current.callback != null) {
            try { current.callback.complete(true, null); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[RotationQueue] completion callback error", t);
            }
        }
        current = null;
    }

    public boolean hasCurrent() { return current != null; }
    public int queuedCount() { return queue.size(); }

    private void trimOverflow() {
        while (queue.size() > MAX_QUEUED) {
            // Evict lowest priority (which is at the tail of the priority queue).
            // PriorityQueue.poll() returns highest; so we rebuild once we detect overflow.
            RotationRequest lowest = null;
            for (RotationRequest r : queue) {
                if (lowest == null || r.priority.value < lowest.priority.value) lowest = r;
            }
            if (lowest == null) break;
            queue.remove(lowest);
            lowest.cancelled = true;
            lowest.cancelReason = "overflow";
        }
    }
}
