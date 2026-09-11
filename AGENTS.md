# Agent guide

Read this before changing anything. It is short on purpose; the details live in `docs/`.

## What this is

Example Mod is a Cobblemon side-mod for Minecraft 1.21.1 on Fabric, written in Kotlin with Java for mixins.
It depends on Cobblemon, Fabric API and Fabric Language Kotlin. It does not target NeoForge.

If `scripts/init-template.sh` still exists, this checkout is the untouched template. Run the script (or apply the
placeholder table at the bottom of this file) before writing real features.

## Commands

| Task | Command | Notes |
| --- | --- | --- |
| Full check | `./gradlew build` | Formatting, unit tests, jar. Run this before you say a change is done. |
| Unit tests only | `./gradlew test` | JUnit under `src/test`. No Minecraft classes may be loaded here. |
| Game tests | `./gradlew runGametest` | Boots a dedicated server with Cobblemon; report in `build/test-results/gametest/junit.xml`. |
| Fix formatting | `./gradlew spotlessApply` | ktlint for Kotlin, Palantir for Java. `build` fails on unformatted code. |
| Data generation | `./gradlew runDatagen` | Rewrites `src/main/generated`. Commit the output. |
| Client | `./gradlew runClient` | Opens a window; not for CI. |
| Server | `./gradlew runServer` | Dedicated server in `run/`. |
| Decompiled Minecraft | `./gradlew genSources` | Sources jar for IDE navigation. Cobblemon sources come down automatically. |
| Dry-run publish | `MOD_PUBLISH_DRY_RUN=1 ./gradlew publishMods` | Validates release config without uploading. |

The first build downloads Minecraft and a few hundred megabytes of libraries. Later builds are incremental and cached.

## Layout

```
gradle/libs.versions.toml   every dependency version (edit here, nowhere else)
gradle.properties           mod id, name, version, group, author, publishing ids
build.gradle.kts            Loom, Kotlin, Spotless, publishing
src/main/kotlin/...         server and common code (entrypoint, commands, config, listeners)
src/main/java/.../mixin     mixins, Java only
src/main/resources          fabric.mod.json, mixin config, assets, hand-written data
src/main/generated          output of runDatagen, committed, never edited by hand
src/client/kotlin/...       client-only code; cannot be referenced from src/main
src/datagen/kotlin/...      data providers, dev-only
src/gametest/kotlin/...     in-game tests, dev-only, packaged as a separate mod
src/test/kotlin/...         plain JUnit tests
docs/                       Cobblemon hook points, conventions, releasing
```

## Rules

- Mixins are Java. Kotlin mixins do not work with the Mixin annotation processor.
- Mappings are Mojang official names with Parchment parameter names. Vanilla class and method names match what
  mojmap shows in the IDE; there are no Yarn names anywhere in this project.
- Client code lives in `src/client`. If something in `src/main` needs a client class, the design is wrong.
- Dependency versions change only in `gradle/libs.versions.toml`. `kotlin` must equal the Kotlin version embedded in
  `fabric-kotlin` (the part after `+kotlin.`); bump them together. `graal` and `icu4j` must equal what the Cobblemon
  release bundles under `META-INF/jars` in its jar. Fabric API, Cobblemon and Mod Menu versions must end in or target
  the same Minecraft version as `minecraft`.
- Translations are generated. Add keys in `EnglishLanguageProvider`, run `runDatagen`, commit the JSON.
- Config is a Gson data class where every field has a default. Keep it that way or old config files break.
- Subscribe to Cobblemon events once, in `onInitialize`. See `docs/cobblemon-hooks.md` for what exists.
- Commits describe the behaviour change, not the code shuffle. No class or function names in commit messages.
- Do not commit `run/`, `build/` or IDE files. Do not hand-edit the Gradle wrapper.

## Done means

1. `./gradlew build` passes.
2. `./gradlew runGametest` passes when the change touches anything that runs in-game.
3. New behaviour has a game test or a unit test, whichever fits.
4. `CHANGELOG.md` has a line under `Unreleased` for anything a player or server owner would notice.

## Gotchas

- `fabric.mod.json` is a template; `${...}` placeholders are filled by Gradle from `gradle.properties` and the
  version catalog. Do not replace them with literals.
- Entrypoints use the `kotlin` adapter so they can be Kotlin `object`s. A plain class also works.
- The game test source set is its own mod (`examplemod_gametest`) so test classes stay out of the release jar.
- Cobblemon bundles GraalJS and ICU4J inside its release jar for its battle engine. Dev runs do not load nested
  jars, so `build.gradle.kts` adds GraalJS as a local runtime dependency and appends ICU4J to the server-side run
  tasks only. ICU4J cannot be a normal dependency: Gradle resolves it to the Minecraft client copy, Loom strips
  client-only libraries from server runs, and Cobblemon then hangs while starting Showdown. The symptom is
  `NoClassDefFoundError: com/ibm/icu/text/DateFormat` in `runServer` or `runGametest`. New server-side run tasks
  need the same `classpath(cobblemonServerRuntime)` line.
- Depend on `com.cobblemon:fabric` only. The `com.cobblemon:mod` artifact the official MDK also lists has no
  `fabric.mod.json`, so Loom leaves it in intermediary names and it breaks compilation against Mojang names.

## Template placeholders

`scripts/init-template.sh` performs these replacements in this order. Doing them by hand works too.

| Placeholder | Meaning | Also renames |
| --- | --- | --- |
| `your-github/examplemod` | GitHub owner/name | |
| `com.example.examplemod` | root package | directories under every `src/*/kotlin` and `src/*/java` |
| `ExampleMod` | class name prefix | `ExampleMod*.kt` files |
| `Example Mod` | display name | |
| `examplemod` | mod id | `examplemod.mixins.json`, `assets/examplemod`, generated assets |
| `com.example` | Maven group | |
| `Your Name` | author | LICENSE copyright line |
