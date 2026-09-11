# Cobblemon hook points

Where a side-mod can plug into Cobblemon 1.8 without forking it. Paths are relative to
`com.cobblemon.mod.common`. The upstream source is at https://gitlab.com/cable-mc/cobblemon.

The sources jar Loom attaches covers only the Fabric-specific classes. For the shared code, attach
`mod-<version>-sources.jar` from https://artefacts.cobblemon.com/releases/com/cobblemon/mod/ in your IDE, or read
the GitLab tree. Both are written against Mojang names, so they match what you see in this project.

## Events

`api.events.CobblemonEvents` holds one observable per event. Subscribe with a priority and a lambda:

```kotlin
CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL) { event -> /* event.pokemon, event.player */ }
```

`CancelableObservable` events expose `event.cancel()`. `Priority` runs `HIGHEST` first and `LOWEST` last.

Events that matter most for catching and spawning:

| Event | When | Useful fields |
| --- | --- | --- |
| `POKEMON_CATCH_RATE` | before a ball is evaluated | catch rate to mutate |
| `POKE_BALL_CAPTURE_CALCULATED` | after the shake count is decided | capture result |
| `THROWN_POKEBALL_HIT` | ball touches a Pokémon, cancelable | ball entity, target |
| `POKEMON_CAPTURED` | capture succeeded | `pokemon`, `player`, `pokeBallEntity` |
| `SHINY_CHANCE_CALCULATION` | shiny roll | chance to mutate |
| `POKEMON_ENTITY_SPAWN` | wild Pokémon placed in the world | entity, spawn context |
| `SPAWN_BUCKET_CHOSEN` | rarity bucket picked for a spawn | bucket |
| `BATTLE_VICTORY`, `BATTLE_FAINTED`, `BATTLE_FLED` | battle outcomes | battle, actors |
| `POKEMON_RELEASED_EVENT_PRE` / `_POST` | release from PC or party | pokemon, player |
| `EVOLUTION_COMPLETE` | evolution finished | pokemon, evolution |

## Capture rate

`config.CobblemonConfig.captureCalculator` selects a `api.pokeball.catching.calculators.CaptureCalculator` by id.
Register your own with `CaptureCalculators.register(calculator)` during initialisation, then point the server's
Cobblemon config at its id. The calculator receives the thrower, the ball and the target Pokémon, so IVs, nature,
shiny state and marks are all available when deciding the odds.

## Spawning

- `api.spawning.influence.SpawningInfluence` can rewrite bucket weights, filter spawn details, adjust weights per
  position, mutate the spawn action (for example set `minPerfectIVs`) and post-process the spawned entity.
  Attach one per player by adding a builder to `PlayerSpawnerFactory.influenceBuilders`.
- Spawn pools and buckets are JSON under `data/cobblemon/spawn_pool_world` and
  `spawning/best-spawner-config.json`. Conditions accept MoLang expressions, so a data pack can gate or reweight
  spawns without code.
- `api.spawning.CobblemonSpawnPools` lists the loaded pools if you need to inspect them at runtime.

## Pokémon data

- `pokemon.Pokemon` carries `ivs`, `evs`, `nature`, `shiny`, `ability`, `marks`, `scaleModifier` and more.
  `ivs.total()` sums the six IVs; `IVs.MAX_TOTAL` is 186.
- `api.pokemon.PokemonSpecies.getByIdentifier(ResourceLocation)` resolves a species.
  `util.cobblemonResource("bidoof")` builds the identifier.
- `Pokemon.getDisplayName()` returns a translatable component that respects nicknames.

## Commands and permissions

Register commands through Fabric's `CommandRegistrationCallback`. Cobblemon uses
`fabric-permissions-api` for its own commands; `CommandSourceStack.hasPermission(level)` works without it.

## Client rendering

Cobblemon's Pokémon renderers, animations and poses are data-driven (Blockbench models plus MoLang). Overriding
visuals is usually a resource pack job, not code.
