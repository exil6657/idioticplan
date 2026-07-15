package com.zenith.client.api.coflnet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.MayorChangeEvent;
import com.zenith.client.core.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

/**
 * Polls Coflnet {@code /api/v1/timer} for the current mayor/minister/Jacob contest.
 * Refreshes every 60 seconds and posts {@link MayorChangeEvent} when the mayor
 * actually changes.
 */
public final class CoflnetMayorAPI {

    private static final CoflnetMayorAPI INSTANCE = new CoflnetMayorAPI();
    public static CoflnetMayorAPI getInstance() { return INSTANCE; }

    private String currentMayor = "";
    private boolean started = false;

    private CoflnetMayorAPI() {}

    public void start() {
        if (started) return;
        started = true;
        refresh();
        ThreadUtils.scheduler().scheduleAtFixedRate(this::refresh, 60, 60, TimeUnit.SECONDS);
    }

    public String currentMayor() { return currentMayor; }

    private void refresh() {
        ThreadUtils.runAsync(() -> {
            try {
                String body = CoflnetClient.getInstance().get("/api/v1/timer").join();
                if (body == null) return;
                JsonElement el = JsonParser.parseString(body);
                if (!el.isJsonObject()) return;
                JsonObject o = el.getAsJsonObject();
                JsonObject mayor = o.has("mayor") && o.get("mayor").isJsonObject() ? o.getAsJsonObject("mayor") : null;
                String name = mayor != null && mayor.has("name") ? mayor.get("name").getAsString() : null;
                if (name != null && !name.equals(currentMayor)) {
                    String minister = null;
                    if (mayor.has("minister") && mayor.get("minister").isJsonObject()) {
                        var mm = mayor.getAsJsonObject("minister");
                        if (mm.has("name")) minister = mm.get("name").getAsString();
                    }
                    currentMayor = name;
                    ZenithEventBus.getInstance().post(new MayorChangeEvent(name, minister == null ? "" : minister, true));
                    ZenithClient.LOGGER.info("[Coflnet.mayor] Mayor is now {} (minister={})", name, minister);
                }
            } catch (Exception e) {
                ZenithClient.LOGGER.warn("[Coflnet.mayor] fetch failed", e);
            }
        });
    }
}
