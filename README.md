# Cobblemon Fabric mod template

A project template for [Cobblemon](https://cobblemon.com/) side-mods on Fabric. Kotlin, Mojang mappings, game tests
that run in CI, one-command releases, and a layout an AI coding agent can work in without guessing.

Fabric only. If you need NeoForge, start from the official
[cobblemon-mdks](https://gitlab.com/cable-mc/cobblemon-mdks) instead.

## What you get

| Piece | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Cobblemon | 1.8.0 |
| Fabric Loader / Fabric API | 0.19.5 / 0.116.17 |
| Fabric Language Kotlin / Kotlin | 1.14.1 / 2.4.20 |
| Fabric Loom | 1.17.20 (`fabric-loom-remap`) |
| Mappings | Mojang official + Parchment 2024.11.17 |
| Gradle | 9.7.1, Kotlin DSL, version catalog, build cache |
| Java | 21, provisioned by the Foojay toolchain resolver when missing |

Tooling that comes wired up:

- **Split source sets.** `src/main` for server-safe code, `src/client` for client-only code, `src/datagen` for data
  providers, `src/gametest` for in-game tests. The last two never reach the release jar.
- **Tests at two levels.** JUnit 6 for plain logic; Fabric game tests that boot a dedicated server with Cobblemon
  loaded and emit a JUnit XML report.
- **Spotless** with ktlint and Palantir Java Format. The build fails on unformatted code.
- **GitHub Actions.** `build.yml` runs on every push and pull request. `release.yml` runs on `v*` tags and publishes
  to GitHub Releases, Modrinth and CurseForge through the Mod Publish Plugin.
- **Dependabot** for the version catalog and the workflows, with the Cobblemon, Fabric and Parchment Mavens
  registered.
- **Agent guide.** `AGENTS.md` (and `CLAUDE.md`, which points at it) documents commands, layout, rules and gotchas.
  `docs/cobblemon-hooks.md` lists the Cobblemon events and extension points a side-mod is likely to need.

The example code is small but real: a `/examplemod species <id>` command, a Gson config with a reload command, a
capture listener that announces high-IV catches, one Java mixin, a language provider, two game tests and three unit
tests.

## Quick start

1. Click **Use this template** on GitHub, or clone this repository.
2. Rename everything in one go:

   ```bash
   scripts/init-template.sh --id souls --name "Thrill of the Hunt" --group io.github.you --author "You" --repo you/souls
   ```

   Add `--dry-run` first to see what it will do. The script moves packages, renames files, rewrites placeholders,
   writes a fresh README and changelog, then deletes itself.
3. Build and run:

   ```bash
   ./gradlew build
   ./gradlew runClient
   ```

   The first build downloads Minecraft, Cobblemon and their libraries. Give it a few minutes.

## Everyday tasks

| Command | What it does |
| --- | --- |
| `./gradlew build` | Format check, unit tests, remapped jar in `build/libs` |
| `./gradlew runGametest` | Game tests on a headless server; report in `build/test-results/gametest` |
| `./gradlew runClient` / `runServer` | Launch with the mod, Cobblemon and Mod Menu |
| `./gradlew runDatagen` | Regenerate `src/main/generated` |
| `./gradlew spotlessApply` | Fix formatting |
| `./gradlew genSources` | Decompile Minecraft for IDE navigation |
| `MOD_PUBLISH_DRY_RUN=1 ./gradlew publishMods` | Check release config without uploading |

## Updating dependencies

All versions live in `gradle/libs.versions.toml`. Dependabot opens grouped pull requests weekly. Two rules:

- `kotlin` must match the Kotlin version embedded in `fabric-kotlin` (the part after `+kotlin.`).
- Minecraft and Parchment bumps are porting jobs; Dependabot ignores them on purpose. Fabric API, Cobblemon and
  Mod Menu only get patch-level PRs, because their minor or major numbers track the Minecraft version.
- GraalJS and ICU4J stay at the versions Cobblemon bundles in its release jar. Dependabot leaves them alone.

When Cobblemon releases, bump `cobblemon`, run `./gradlew build runGametest`, fix what the compiler flags.

## Releasing

Bump `mod_version`, move the changelog entries under a version heading, tag `vX.Y.Z`, push. `docs/releasing.md`
covers the secrets and destinations.

## Links

- [Cobblemon source](https://gitlab.com/cable-mc/cobblemon) and [wiki](https://wiki.cobblemon.com/)
- [Fabric developer docs](https://docs.fabricmc.net/develop/)
- [Fabric Loom](https://github.com/FabricMC/fabric-loom) and [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin)
- [Mod Publish Plugin](https://modmuss50.github.io/mod-publish-plugin/)

## License

MIT. The template is yours to change; the init script rewrites the copyright line to your name.
