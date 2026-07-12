package com.zenith.client.engine.path.learning.profile;

import com.google.gson.Gson;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.FileUtils;
import com.zenith.client.engine.path.learning.MovementProfile;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Loads a profile from {@code data/movement_profiles/<name>.json}. */
public final class ProfileImporter {

    private static final Gson G = new Gson();

    public static MovementProfile load(String name) {
        try {
            Path file = Paths.get("data", "movement_profiles", name + ".json");
            if (!FileUtils.fileExists(file)) return null;
            return G.fromJson(FileUtils.readString(file), MovementProfile.class);
        } catch (Exception e) {
            ZenithClient.LOGGER.error("[MovementLearner] Import failed for {}", name, e);
            return null;
        }
    }

    private ProfileImporter() {}
}
