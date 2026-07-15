package com.zenith.client.core.util;

import java.util.Set;

/**
 * Block-related helpers.
 *
 * <p><b>Phase 2 note:</b> concrete methods that interact with {@code BlockState}/
 * {@code BlockPos} are added in Phase 6 (World subsystem) once the access to
 * the client world is wired. Skeleton API surface is declared now so that
 * dependent modules can compile against the stable signature.</p>
 */
public final class BlockUtils {

    /** Crop IDs treated as farmland blocks for Farming macro checks. */
    public static final Set<String> CROP_IDS = Set.of(
        "wheat", "carrots", "potatoes", "sugar_cane", "nether_wart",
        "cocoa", "melon", "pumpkin", "mushroom", "cactus"
    );

    private BlockUtils() {}

    /** @return true if the supplied SkyBlock item/block id represents a walkable surface. */
    public static boolean isWalkable(String blockId) {
        if (blockId == null) return false;
        return !blockId.contains("air") && !blockId.contains("fire") && !blockId.contains("lava") && !blockId.contains("water");
    }
}
