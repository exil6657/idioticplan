package com.zenith.client.core.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public field of a Module as a user-configurable setting.
 *
 * <p>The field must be an instance of one of the setting classes in
 * {@link com.zenith.client.core.module.settings}.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SettingInfo {
    String name();
    String description() default "";
}
