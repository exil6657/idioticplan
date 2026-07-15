package com.zenith.client.core.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Filesystem helpers — all operations are sandboxed to the game run directory. */
public final class FileUtils {

    private FileUtils() {}

    public static void ensureDirectory(Path p) throws IOException {
        if (p == null) return;
        if (!Files.exists(p)) Files.createDirectories(p);
    }

    public static String readString(Path p) throws IOException {
        byte[] bytes = Files.readAllBytes(p);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(Path p, String content) throws IOException {
        ensureDirectory(p.getParent());
        Files.writeString(p, content, StandardCharsets.UTF_8);
    }

    public static boolean fileExists(Path p) {
        return p != null && Files.exists(p) && Files.isRegularFile(p);
    }

    public static void deleteQuietly(Path p) {
        if (p == null) return;
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
    }
}
