package com.zenith.client.core.util;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/** System clipboard access — used by the .export and .clip commands. */
public final class ClipboardUtils {

    private ClipboardUtils() {}

    public static void copy(String text) {
        if (text == null) return;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                   .setContents(new StringSelection(text), null);
        } catch (Throwable t) {
            // AWT headless / security manager: fall back to no-op.
        }
    }
}
