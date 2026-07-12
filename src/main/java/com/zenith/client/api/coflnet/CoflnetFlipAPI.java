package com.zenith.client.api.coflnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.ThreadUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connects to the Coflnet flip-feed endpoint every ~2 s and posts incoming
 * flips as FlipEvents (Phase 9 will consume them). This Phase-8 version just
 * polls; websockets are deferred to a later phase.
 *
 * <p>Results flow through {@link com.zenith.client.core.event.events.BigFlipCompleteEvent}
 * / {@link com.zenith.client.core.event.events.FlipCompleteEvent} for other
 * subsystems to react.</p>
 */
public final class CoflnetFlipAPI {

    private static final CoflnetFlipAPI INSTANCE = new CoflnetFlipAPI();
    public static CoflnetFlipAPI getInstance() { return INSTANCE; }

    private final AtomicBoolean started = new AtomicBoolean(false);

    private CoflnetFlipAPI() {}

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        // Poll every 3 seconds for recommended flips. Real-time websocket deferred.
        ThreadUtils.scheduler().scheduleAtFixedRate(this::poll, 2, 3, TimeUnit.SECONDS);
        ZenithClient.LOGGER.info("[Coflnet.flip] Poller started.");
    }

    private void poll() {
        CompletableFuture<String> f = CoflnetClient.getInstance().get("/api/v1/flips",
                Map.of("limit", "20"));
        f.thenAccept(body -> {
            if (body == null) return;
            try {
                JsonElement el = JsonParser.parseString(body);
                if (!el.isJsonArray()) return;
                JsonArray arr = el.getAsJsonArray();
                for (JsonElement je : arr) {
                    if (!je.isJsonObject()) continue;
                    JsonObject o = je.getAsJsonObject();
                    // Phase 9 will construct FlipCandidate objects and submit to the flipper.
                    // For now we just log a sample.
                    if (o.has("itemId") && o.has("profit")) {
                        // ZenithEventBus.getInstance().post(new FlipCandidateEvent(...));
                    }
                }
            } catch (Exception ex) {
                ZenithClient.LOGGER.warn("[Coflnet.flip] parse failed", ex);
            }
        });
    }
}
