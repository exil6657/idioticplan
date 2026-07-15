package com.zenith.client.failsafe.detection;

import com.zenith.client.ZenithClient;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;

/**
 * Base class for all failsafe detectors.
 *
 * <p>Each detector polls state once per client tick and calls
 * {@link #trigger(FailsafeType, String, FailsafeStrictness)} or
 * {@link #clear(FailsafeType)} on the manager. Detectors must be side-effect
 * free except for reporting through the manager — they must never modify
 * player state, send packets, or toggle modules directly.</p>
 *
 * <p>The manager catches {@link Throwable} from {@link #tick(long)} so a buggy
 * detector cannot crash the game.</p>
 */
public abstract class AbstractDetector {

    /** Called once per client tick with the current wall-clock time (ms). */
    public abstract void tick(long nowMs);

    /** Human-readable name used for logging. */
    public String name() { return getClass().getSimpleName(); }

    // ---- Convenience methods for subclasses ------------------------------

    protected final Minecraft mc() { return Minecraft.getInstance(); }

    protected final void trigger(FailsafeType type, String reason) {
        FailsafeManager.getInstance().trigger(type, reason, null);
    }

    protected final void trigger(FailsafeType type, String reason, FailsafeStrictness severity) {
        FailsafeManager.getInstance().trigger(type, reason, severity);
    }

    protected final void clear(FailsafeType type) {
        FailsafeManager.getInstance().clear(type);
    }

    protected final void debug(String fmt, Object... args) {
        // Phase 7: guarded by SLF4J debug level (off by default).
        ZenithClient.LOGGER.debug("[Fail.{}] " + fmt, prepend(name(), args));
    }

    private static Object[] prepend(String name, Object[] args) {
        if (args == null || args.length == 0) return new Object[]{ name };
        Object[] out = new Object[args.length + 1];
        out[0] = name;
        System.arraycopy(args, 0, out, 1, args.length);
        return out;
    }
}
