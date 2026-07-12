package com.zenith.client.core.event;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.annotation.SubscribeEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight annotation-driven event bus.
 *
 * <p>Subscribers register an arbitrary object; any method annotated with
 * {@link SubscribeEvent} that accepts exactly one parameter (a subclass of
 * {@link ZenithEvent}) is automatically subscribed. Thread-safe: publish can be
 * called from any thread (render thread, tick thread, netty) but event handlers
 * are invoked on the calling thread, so subscribers are responsible for
 * thread-safety if they interact with Minecraft state.</p>
 *
 * <p>Master rule §7: <em>"Use events, not direct references, for cross-system
 * communication."</em> This bus is the only sanctioned channel.</p>
 */
public final class ZenithEventBus {

    private static ZenithEventBus instance;

    /** event class -> list of registered listeners, sorted by priority ascending. */
    private final Map<Class<?>, CopyOnWriteArrayList<RegisteredListener>> listeners = new ConcurrentHashMap<>();
    /** registered owner objects, for unregister-all. */
    private final Map<Object, List<Class<?>>> ownerToEvents = new ConcurrentHashMap<>();

    private ZenithEventBus() {}

    public static ZenithEventBus getInstance() {
        if (instance == null) {
            instance = new ZenithEventBus();
        }
        return instance;
    }

    /** Initialises the bus (idempotent). Called from {@link ZenithClient#onInitializeClient()}. */
    public void init() {
        ZenithClient.LOGGER.info("[EventBus] Initialized.");
    }

    /**
     * Scans {@code subscriber} for {@link SubscribeEvent}-annotated methods and
     * registers them. Methods must be non-static, have exactly one parameter
     * extending {@link ZenithEvent}, and be declared public or package-private.
     */
    public void register(Object subscriber) {
        if (subscriber == null) return;
        List<Class<?>> registered = new CopyOnWriteArrayList<>();
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null) continue;
            if (method.getParameterCount() != 1) {
                ZenithClient.LOGGER.warn("[EventBus] Skipping {}: must take exactly 1 parameter", method);
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                ZenithClient.LOGGER.warn("[EventBus] Skipping {}: static subscribers not supported", method);
                continue;
            }
            Class<?> paramType = method.getParameterTypes()[0];
            if (!ZenithEvent.class.isAssignableFrom(paramType)) {
                ZenithClient.LOGGER.warn("[EventBus] Skipping {}: parameter {} is not a ZenithEvent", method, paramType);
                continue;
            }
            method.setAccessible(true);
            EventPriority priority = annotation.priority();
            RegisteredListener listener = new RegisteredListener(subscriber, method, priority);
            listeners.computeIfAbsent(paramType, k -> new CopyOnWriteArrayList<>()).add(listener);
            sortListeners(paramType);
            registered.add(paramType);
        }
        if (!registered.isEmpty()) {
            ownerToEvents.put(subscriber, registered);
        }
    }

    public void unregister(Object subscriber) {
        List<Class<?>> eventTypes = ownerToEvents.remove(subscriber);
        if (eventTypes == null) return;
        for (Class<?> eventType : eventTypes) {
            CopyOnWriteArrayList<RegisteredListener> list = listeners.get(eventType);
            if (list == null) continue;
            Iterator<RegisteredListener> it = list.iterator();
            while (it.hasNext()) {
                if (it.next().owner == subscriber) {
                    list.remove(it);
                    break;
                }
            }
        }
    }

    /**
     * Dispatches an event to all registered listeners. Listeners fire in priority
     * order; if any listener cancels the event (for cancellable events), remaining
     * lower-priority listeners still see the event but may act on the cancelled
     * state. This mirrors Minecraft/Fabric behaviour so receivers can observe the
     * cancellation and opt-out.
     */
    public <T extends ZenithEvent> T post(T event) {
        if (event == null) return null;
        CopyOnWriteArrayList<RegisteredListener> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) return event;
        for (RegisteredListener listener : list) {
            try {
                listener.method.invoke(listener.owner, event);
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[EventBus] Exception dispatching {} to {}",
                        event.getClass().getSimpleName(), listener.owner.getClass().getSimpleName(), t);
            }
        }
        return event;
    }

    private void sortListeners(Class<?> eventType) {
        CopyOnWriteArrayList<RegisteredListener> list = listeners.get(eventType);
        if (list == null) return;
        List<RegisteredListener> snapshot = new java.util.ArrayList<>(list);
        snapshot.sort((a, b) -> Integer.compare(a.priority.getValue(), b.priority.getValue()));
        listeners.put(eventType, new CopyOnWriteArrayList<>(snapshot));
    }

    public Collection<Class<?>> getRegisteredEventTypes() {
        return Collections.unmodifiableCollection(listeners.keySet());
    }

    private static final class RegisteredListener {
        final Object owner;
        final Method method;
        final EventPriority priority;

        RegisteredListener(Object owner, Method method, EventPriority priority) {
            this.owner = owner;
            this.method = method;
            this.priority = priority;
        }
    }
}
