# Zenith Client — Developer Setup (Phase 1)

## Requirements

- **JDK 25** — *Gradle JVM* (required by Fabric Loom 1.15 for Minecraft 26.1.x).
  Free distributions: [Eclipse Temurin 25](https://adoptium.net/temurin/releases/?version=25),
  [Oracle JDK 25](https://www.oracle.com/java/technologies/downloads/), or Zulu.
- **JDK 21** — *language/bytecode target* (the mod's `.class` files are Java 21).
  You can point the Gradle JVM at JDK 25 and Loom will automatically cross-compile
  to Java 21 through the toolchain declared in `build.gradle`.
- **Minecraft 26.1.2** installed via the Fabric Loader 0.18.9+ launcher profile.
- **IntelliJ IDEA 2025.3+** (required for correct Mixin annotation processing on 26.1,
  per the Fabric 26.1 release notes).
- **Git** for version control.

## Build Toolchain (Resolved by R001 Research)

| Component | Version | Notes |
|-----------|---------|-------|
| Minecraft | 26.1.2 | First unobfuscated MC release |
| Fabric Loom | 1.15 | New unobfuscated mode — no remapJar, no mappings |
| Gradle | 9.4.0 | Pulled automatically by `gradlew` |
| Fabric Loader | 0.18.9 | Minimum is 0.18.4 |
| Fabric API | 0.145.4+26.1.2 | Uses Mojang names (Yarn deprecated) |
| Gradle JVM | Java 25 | Set in IDE Gradle settings |
| Source/Target | Java 21 | `options.release = 21` in build.gradle |

## Quick Start

```bash
# 1. Clone the repository
git clone <repo-url> zenith-client
cd zenith-client

# 2. Make sure JAVA_HOME points at JDK 25
export JAVA_HOME=/path/to/jdk-25
java -version   # should show 25.x

# 3. Build
./gradlew build

#   Output:
#     build/libs/zenith-client-1.0.0.jar          — dev JAR
#     build/libs/zenith-client-1.0.0-sources.jar  — source JAR
#
#   (Release JAR with ProGuard obfuscation is a Phase 20 task.)

# 4. Run the dev client (regenerates run/ configs on first launch)
./gradlew runClient
```

On first run the Fabric development client will start. You should see in the log:

```
==============================================================
  Zenith Client v1.0.0 loaded successfully.
  Target:     Minecraft 26.1.2
  Developer:  Exil
  Phase:      1 of 20 — skeleton only, no macros active.
==============================================================
```

## IDE Setup (IntelliJ IDEA 2025.3+)

1. Open the project root in IntelliJ (it will auto-import Gradle).
2. **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM**
   → Select JDK 25.
3. **File → Project Structure → Project SDK** → JDK 21 (for code analysis; Loom
   will handle the bytecode target, but a JDK must be set).
4. Ensure annotation processing is enabled for Mixin (Loom does this automatically
   in modern versions; if Mixin entries are flagged red run `./gradlew genSources`).
5. Run configuration: `./gradlew runClient` will register an "Minecraft Client"
   run configuration automatically.

## Phase 1 Verification Checklist

After `./gradlew build` and `./gradlew runClient` succeed:

- [ ] Game launches to the Minecraft title screen with no crash.
- [ ] Log shows the Zenith Client "loaded successfully" banner.
- [ ] No `ClassNotFoundException` / `NoSuchMethodError` from mixin packages.
- [ ] Typing an invalid `.z`-prefixed message in chat is not yet intercepted
      (CommandInterceptor is a Phase 2 task — this is expected).
- [ ] Pressing Right-Ctrl does nothing yet (GUI is a Phase 4 task — expected).
- [ ] JAR file at `build/libs/zenith-client-1.0.0.jar` is ≥ ~6 MB
      (contains JDA+Gson jar-in-jar).

## Project Layout Reference

See `README.md` for the top-level layout and `docs/ARCHITECTURE.md` for the
system initialization order. The Phase 1 deliverables are:

- `ZenithClient.java` — entrypoint (logs banner only)
- `ZenithClientInfo.java` — mod metadata constants
- 12 mixin stubs under `mixin/` (compile, do not inject yet)
- 3 accessor interfaces under `mixin/accessor/`
- `fabric.mod.json` — correct dependency metadata for 26.1.2
- `zenithclient.mixins.json` — all 12 mixins + 3 accessors listed
- `build.gradle` — Loom 1.15 / unobfuscated mode
- `gradle-wrapper.properties` — Gradle 9.4.0
- ProGuard rules file (Phase 20 task reference)

## Troubleshooting

**"Unsupported class file major version"** — the Gradle JVM is too old.
You must run Gradle on Java 25. The mod itself is still compiled to Java 21
bytecode through the `options.release = 21` setting.

**"Mixin injection error" at startup** — some mixin target classes changed
between Fabric 26.1 release notes and 26.1.2 final. Update the target class
names in the affected mixin stub until the game loads cleanly. All mixins
in Phase 1 are empty stubs so this should not occur.

**"Could not resolve net.fabricmc:fabric-loader:0.18.9"** — run
`./gradlew --refresh-dependencies` or check `https://maven.fabricmc.net/`
for the latest Loader version if 0.18.9 has been superseded.
