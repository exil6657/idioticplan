package com.zenith.client.api.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.ZenithClientInfo;
import com.zenith.client.api.HttpClient;
import com.zenith.client.api.ratelimit.RateLimiter;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

/**
 * Checks GitHub releases (or a static JSON endpoint) for a newer client version.
 *
 * <p>Outbound request goes to {@code api.github.com/repos/exil6657/idioticplan/releases/latest},
 * which is public and requires no auth. Check is asynchronous; failures are silently
 * ignored (we never log the user out or crash on network errors).</p>
 */
public final class UpdateChecker {

    private static final UpdateChecker INSTANCE = new UpdateChecker();
    public static UpdateChecker getInstance() { return INSTANCE; }

    private static final String LATEST_RELEASE =
            "https://api.github.com/repos/exil6657/idioticplan/releases/latest";

    private final UpdateConfig config = new UpdateConfig();
    private boolean started = false;
    private String availableVersion;

    private UpdateChecker() {}

    public UpdateConfig config() { return config; }
    public String availableVersion() { return availableVersion; }
    public boolean isUpdateAvailable() {
        return availableVersion != null && !availableVersion.equals(ZenithClientInfo.VERSION);
    }

    public void start() {
        if (started) return;
        started = true;
        // First check after 30 s (let the mod initialise), then every config.checkIntervalMs.
        ThreadUtils.scheduler().scheduleWithFixedDelay(
                this::check, 30, Math.max(60L, config.checkIntervalMs / 1000L), TimeUnit.SECONDS);
    }

    private void check() {
        if (!config.enabled) return;
        if (!RateLimiter.getInstance().tryAcquire("update.check")) return;
        try {
            String body = HttpClient.getInstance().get(LATEST_RELEASE).join();
            if (body == null) return;
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            String tag = o.has("tag_name") ? o.get("tag_name").getAsString() : null;
            if (tag != null && !tag.equals(ZenithClientInfo.VERSION)) {
                availableVersion = tag;
                if (!tag.equals(config.lastSeenVersion) && config.notifyToasts) {
                    ZenithChat.getInstance().info("Zenith update available: {} (you are on {})", tag, ZenithClientInfo.VERSION);
                    config.lastSeenVersion = tag;
                }
            }
        } catch (Exception e) {
            ZenithClient.LOGGER.debug("[Update] check failed (offline?): {}", e.getMessage());
        }
    }
}
