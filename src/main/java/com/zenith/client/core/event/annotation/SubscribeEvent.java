package com.zenith.client.core.event.annotation;

import com.zenith.client.core.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event subscriber.
 *
 * <p>The method must be non-static, take exactly one parameter extending
 * {@link com.zenith.client.core.event.ZenithEvent}, and live on an object
 * passed to {@link com.zenith.client.core.event.ZenithEventBus#register(Object)}.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
}
