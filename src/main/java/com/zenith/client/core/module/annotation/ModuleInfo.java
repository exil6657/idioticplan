package com.zenith.client.core.module.annotation;

import com.zenith.client.core.module.ModuleCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for a {@link com.zenith.client.core.module.Module}.
 *
 * <p>Applied to the module class; the {@code ModuleManager} reads this
 * reflectively at registration time.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    String id();
    String displayName();
    String description() default "";
    ModuleCategory category();
    int defaultKey() default 0;  // GLFW key code, 0 = unbound
    boolean enabledByDefault() default false;
    boolean toggleable() default true;
}
