package com.zenith.client.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Screenshot helpers — brain-view / debug captures.
 *
 * <p>Phase 2 stub; Phase 5 wires to the framebuffer.</p>
 */
public final class ScreenshotUtils {

    private ScreenshotUtils() {}

    public static Path getScreenshotDir() {
        return Paths.get("screenshots");
    }

    /** Queue an async screenshot on the render thread. */
    public static void capture(String filename) {
        // Phase 5 — render-thread framebuffer readback.
    }
}
