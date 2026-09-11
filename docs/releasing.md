# Releasing

1. Move the `Unreleased` entries in `CHANGELOG.md` under a new `## [x.y.z] - YYYY-MM-DD` heading.
2. Set `mod_version=x.y.z` in `gradle.properties`.
3. Commit, then tag and push:

   ```bash
   git tag vX.Y.Z
   git push origin main vX.Y.Z
   ```

The release workflow refuses tags that do not match `mod_version`, runs the full build and the game tests, then
runs `./gradlew publishMods`.

## Destinations

The publish plugin uploads wherever an id is configured:

| Destination | Configure | Secret |
| --- | --- | --- |
| GitHub release | `github_repo` in `gradle.properties` | `GITHUB_TOKEN` (provided by Actions) |
| Modrinth | `modrinth_project_id` | `MODRINTH_TOKEN` repository secret |
| CurseForge | `curseforge_project_id` and `curseforge_project_slug` | `CURSEFORGE_TOKEN` repository secret |

Leave an id blank to skip that destination. The release notes are the newest versioned section of
`CHANGELOG.md`.

## Dry run

```bash
MOD_PUBLISH_DRY_RUN=1 ./gradlew publishMods
```

Validates the configuration and writes the request payloads to `build/publishMods` without uploading.

## Maven

`./gradlew publish` publishes the remapped jar and sources through the `maven` publication. Add a repository
block under `publishing.repositories` in `build.gradle.kts` before using it.
