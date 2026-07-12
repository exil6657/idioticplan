package com.zenith.client.core.module;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.AutopilotDecisionEvent;
import com.zenith.client.core.module.annotation.ModuleInfo;
import com.zenith.client.core.module.annotation.SettingInfo;
import com.zenith.client.core.module.settings.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all Zenith modules/macros.
 *
 * <p>Subclasses must be annotated with {@link ModuleInfo}. Settings are declared
 * as public {@link Setting} fields with {@link SettingInfo}; they are auto-discovered
 * by reflection and registered with {@link ModuleManager}.</p>
 *
 * <p>Modules subscribe to the event bus automatically on enable and unsubscribe
 * on disable — any {@code @SubscribeEvent} annotated method becomes active while
 * enabled.</p>
 */
public abstract class Module {

    private final String id;
    private final String displayName;
    private final String description;
    private final ModuleCategory category;
    private final int defaultKey;
    private final boolean enabledByDefault;
    private final boolean toggleable;

    private boolean enabled = false;
    private final List<Setting<?>> settings = new ArrayList<>();
    private int keybind;
    private long lastToggledAt;

    protected Module() {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            throw new IllegalStateException("Module " + getClass().getName() + " is missing @ModuleInfo");
        }
        this.id = info.id();
        this.displayName = info.displayName();
        this.description = info.description();
        this.category = info.category();
        this.defaultKey = info.defaultKey();
        this.keybind = info.defaultKey();
        this.enabledByDefault = info.enabledByDefault();
        this.toggleable = info.toggleable();
        discoverSettings();
    }

    private void discoverSettings() {
        for (Field f : getClass().getDeclaredFields()) {
            SettingInfo si = f.getAnnotation(SettingInfo.class);
            if (si == null) continue;
            if (!Setting.class.isAssignableFrom(f.getType())) {
                ZenithClient.LOGGER.warn("[Module] Field {} in {} is annotated @SettingInfo but is not a Setting",
                        f.getName(), getClass().getSimpleName());
                continue;
            }
            f.setAccessible(true);
            try {
                Setting<?> s = (Setting<?>) f.get(this);
                if (s != null) settings.add(s);
            } catch (IllegalAccessException e) {
                ZenithClient.LOGGER.error("[Module] Unable to read setting " + f.getName(), e);
            }
        }
    }

    // ---- lifecycle ------------------------------------------------------

    public final void enable() {
        if (enabled || !toggleable) return;
        enabled = true;
        lastToggledAt = System.currentTimeMillis();
        ZenithEventBus.getInstance().register(this);
        try { onEnable(); }
        catch (Throwable t) {
            ZenithClient.LOGGER.error("[Module] {} onEnable failed", id, t);
            enabled = false;
            ZenithEventBus.getInstance().unregister(this);
        }
    }

    public final void disable() {
        if (!enabled) return;
        enabled = false;
        lastToggledAt = System.currentTimeMillis();
        try { onDisable(); }
        catch (Throwable t) {
            ZenithClient.LOGGER.error("[Module] {} onDisable failed", id, t);
        }
        ZenithEventBus.getInstance().unregister(this);
    }

    public final void toggle() {
        if (enabled) disable(); else enable();
    }

    protected void onEnable()  { /* subclasses override */ }
    protected void onDisable() { /* subclasses override */ }

    /** Called once per client tick (20 tps) while enabled; default no-op. */
    public void onTick() { /* subclasses override */ }

    /** Called once per render frame while enabled. */
    public void onRender(float tickDelta) { /* subclasses override */ }

    // ---- accessors ------------------------------------------------------

    public String getId()              { return id; }
    public String getDisplayName()     { return displayName; }
    public String getDescription()     { return description; }
    public ModuleCategory getCategory(){ return category; }
    public boolean isEnabled()         { return enabled; }
    public boolean isToggleable()      { return toggleable; }
    public int getDefaultKey()         { return defaultKey; }
    public int getKeybind()            { return keybind; }
    public void setKeybind(int k)      { this.keybind = k; }
    public boolean isEnabledByDefault(){ return enabledByDefault; }
    public long getLastToggledAt()     { return lastToggledAt; }
    public List<Setting<?>> getSettings() { return Collections.unmodifiableList(settings); }

    public Setting<?> getSetting(String name) {
        for (Setting<?> s : settings) if (s.getName().equalsIgnoreCase(name)) return s;
        return null;
    }

    /** @return debug snapshot for the Brain View HUD (Phase 8 Debug). */
    public java.util.Map<String, Object> getDebugData() {
        java.util.LinkedHashMap<String, Object> d = new java.util.LinkedHashMap<>();
        d.put("enabled", enabled);
        d.put("category", category.name());
        d.put("uptime_ms", enabled ? (System.currentTimeMillis() - lastToggledAt) : 0);
        return d;
    }

    /** Dummy @SubscribeEvent so subclasses with no events still register cleanly. */
    @SubscribeEvent
    void __noop(AutopilotDecisionEvent ignored) {}
}
