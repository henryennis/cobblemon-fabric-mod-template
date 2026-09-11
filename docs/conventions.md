# Conventions

## Language and formatting

- Kotlin for everything except mixins. Java for mixins.
- `./gradlew spotlessApply` before committing. ktlint uses the official code style with a 140 column limit;
  Palantir formats Java. CI fails on unformatted code.
- Four spaces, LF line endings, UTF-8. `.editorconfig` enforces this in editors that honour it.

## Packages

```
<group>.<modid>
├── <ModName>.kt          entrypoint and shared state (logger, config, id helper)
├── command/              Brigadier commands
├── config/               Gson config classes
├── listener/             Cobblemon and Fabric event subscriptions
├── mixin/                Java mixins (src/main/java)
├── client/               client entrypoint and rendering (src/client)
├── datagen/              data providers (src/datagen)
└── gametest/             in-game tests (src/gametest)
```

Add packages as features grow (`spawning/`, `capture/`, `economy/`). Keep the entrypoint thin: it wires things up
and nothing else.

## Server and client

`src/main` must run on a dedicated server. Anything that imports rendering, screens or `Minecraft.getInstance()`
goes in `src/client`. Loom compiles the two separately, so a wrong import fails the build instead of crashing a
server.

## Data and translations

Anything Minecraft can express as data (recipes, tags, loot, models, language files) is generated in
`src/datagen` and committed under `src/main/generated`. Hand-written JSON goes in `src/main/resources` only when a
provider does not exist for it.

## Config

One Gson data class per config file. Every field has a default. Read once at startup, expose a reload command.

## Testing

- Pure logic: JUnit in `src/test`. Fast, no Minecraft bootstrap.
- Anything that touches registries, worlds, entities or Cobblemon: game tests in `src/gametest`.
  Each test method gets an empty structure and a `GameTestHelper`; call `helper.succeed()` when done.

## Versioning and changelog

- `mod_version` in `gradle.properties` follows semver. The build appends `+<minecraft version>`.
- Pre-releases use `-alpha.N` or `-beta.N`; the publish plugin maps them to the matching release type.
- Every user-visible change gets a line under `Unreleased` in `CHANGELOG.md`. Cutting a release renames that
  section to the version and date.

## Commits

Describe what changed for the player or server owner. No class names, method names or file paths in the subject.
`Wild Pokémon with high IV totals now flee from approaching players` is good;
`Add FleeGoal to PokemonEntityMixin` is not.
