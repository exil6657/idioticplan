# ========================================
# Zenith Client — ProGuard Rules (release obfuscation)
# Reference: Part 5 §3 of the master development document.
# ========================================

# Keep entry point
-keep class com.zenith.client.ZenithClient { *; }
-keep class com.zenith.client.ZenithClientInfo { *; }

# Keep all Mixin classes (Mixin requires exact class/method names)
-keep class com.zenith.client.mixin.** { *; }

# Keep Fabric annotations
-keep @interface net.fabricmc.api.Environment
-keep @interface net.fabricmc.api.EnvType

# Keep event bus
-keep class com.zenith.client.core.event.ZenithEvent { *; }
-keep class com.zenith.client.core.event.ZenithEventBus { *; }

# Keep config classes (Gson serialization — field names must be preserved)
-keepclassmembers class com.zenith.client.config.** { *; }
-keepclassmembers class *Config { *; }
-keepclassmembers class *Data { *; }

# Keep serialized persistence classes
-keep class com.zenith.client.session.SessionData { *; }
-keep class com.zenith.client.flipping.profit.FlipRecord { *; }

# Keep module & macro class names (referenced by string from config)
-keepnames class ** extends com.zenith.client.core.module.Module
-keepnames class ** extends com.zenith.client.core.macro.AbstractMacro

# Preserve enum valueOf/values (referenced by name from JSON)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep bundled libraries (jar-in-jar — must not be obfuscated)
-keep class net.dv8tion.jda.** { *; }
-dontwarn net.dv8tion.jda.**
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Don't obfuscate into Minecraft/Fabric namespaces
-keep class net.minecraft.** { *; }
-keep class net.fabricmc.** { *; }
-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**

# Output mapping file (KEEP PRIVATE — never distribute)
-printmapping build/proguard-mapping.txt
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optimisation passes
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
