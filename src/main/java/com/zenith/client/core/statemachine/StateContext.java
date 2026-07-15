package com.zenith.client.core.statemachine;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable bag of data carried through a {@link StateMachine}.
 *
 * <p>States read/write data here to pass information between phases without
 * tight coupling. Macro state machines (farming, mining, combat…) extend this
 * to add typed fields, but the string-key map works for ad-hoc payloads.</p>
 */
public class StateContext {

    private final Map<String, Object> data = new HashMap<>();
    private long stateEnteredAtMs;
    private long lastTickAtMs;

    public Object get(String key) { return data.get(key); }
    public <T> T get(String key, Class<T> type) {
        Object v = data.get(key);
        return type.isInstance(v) ? type.cast(v) : null;
    }
    public void put(String key, Object value) { data.put(key, value); }
    public boolean has(String key) { return data.containsKey(key); }
    public void clear() { data.clear(); }

    public long getStateEnteredAtMs() { return stateEnteredAtMs; }
    public void setStateEnteredAtMs(long ms) { this.stateEnteredAtMs = ms; }
    public long getLastTickAtMs() { return lastTickAtMs; }
    public void setLastTickAtMs(long ms) { this.lastTickAtMs = ms; }

    /** @return milliseconds spent in the current state. */
    public long timeInState() {
        return System.currentTimeMillis() - stateEnteredAtMs;
    }
}
