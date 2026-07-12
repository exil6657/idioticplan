package com.zenith.client.keybind;

import com.zenith.client.core.input.KeybindMapper;
import com.zenith.client.core.input.PlayerKeybindReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks Zenith keybinds against Minecraft's bound key codes to warn about conflicts.
 *
 * <p>Phase 2: detects obvious duplicates by GLFW key code. Later phases may add
 * conflicts with modded keybinds from other mods.</p>
 */
public final class KeybindConflictChecker {

    private KeybindConflictChecker() {}

    public static List<String> findConflicts(KeybindConfig cfg) {
        List<String> problems = new ArrayList<>();
        Map<Integer, String> usedByMc = new HashMap<>();
        PlayerKeybindReader reader = PlayerKeybindReader.getInstance();
        for (KeybindMapper km : KeybindMapper.values()) {
            int code = reader.getKey(km);
            if (code != 0) usedByMc.put(code, km.name());
        }
        for (Map.Entry<ZenithKeybinds, String> e : cfg.snapshot().entrySet()) {
            int code = parseGlfw(e.getValue());
            if (code == 0 || code == -1) continue;
            String mc = usedByMc.get(code);
            if (mc != null) {
                problems.add(e.getKey().name() + " conflicts with Minecraft " + mc);
            }
        }
        return problems;
    }

    /** Parse "KEY_W" style identifiers to GLFW codes. Phase 2: handles the common subset needed. */
    static int parseGlfw(String s) {
        if (s == null || s.isBlank() || s.equals("KEY_UNKNOWN")) return 0;
        // Simple fallback — full table in Phase 3 KeybindManager
        return switch (s) {
            case "KEY_RIGHT_CONTROL" -> 345;
            case "KEY_END"           -> 269;
            default -> -1;
        };
    }
}
