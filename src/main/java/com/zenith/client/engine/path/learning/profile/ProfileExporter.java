package com.zenith.client.engine.path.learning.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.FileUtils;
import com.zenith.client.engine.path.learning.MovementProfile;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Writes a {@link MovementProfile} to {@code data/movement_profiles/<name>.json}. */
public final class ProfileExporter {

    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();

    public static boolean export(MovementProfile p) {
        try {
            Path dir = Paths.get("data", "movement_profiles");
            FileUtils.ensureDirectory(dir);
            String name = p.name == null ? "learned" : p.name.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path file = dir.resolve(name + ".json");
            FileUtils.writeString(file, G.toJson(p));
            ZenithClient.LOGGER.info("[MovementLearner] Profile exported to {}", file);
            return true;
        } catch (Exception e) {
            ZenithClient.LOGGER.error("[MovementLearner] Export failed", e);
            return false;
        }
    }

    private ProfileExporter() {}
}
