package com.example.examplemod.command

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.example.examplemod.ExampleMod
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component

/**
 * `/examplemod species <id>` prints a few facts about a Cobblemon species.
 * `/examplemod reload` re-reads the config file (operators only).
 */
object ExampleModCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands
                    .literal(ExampleMod.MOD_ID)
                    .then(
                        Commands
                            .literal("species")
                            .then(
                                Commands
                                    .argument("species", ResourceLocationArgument.id())
                                    .executes(::describeSpecies),
                            ),
                    ).then(
                        Commands
                            .literal("reload")
                            .requires { it.hasPermission(2) }
                            .executes(::reloadConfig),
                    ),
            )
        }
    }

    private fun describeSpecies(context: CommandContext<CommandSourceStack>): Int {
        val id = ResourceLocationArgument.getId(context, "species")
        val species = PokemonSpecies.getByIdentifier(id)
        if (species == null) {
            context.source.sendFailure(Component.translatable("examplemod.command.species.unknown", id.toString()))
            return 0
        }
        val types = species.types.joinToString("/") { it.displayName.string }
        context.source.sendSuccess(
            {
                Component.translatable(
                    "examplemod.command.species.info",
                    species.translatedName,
                    species.nationalPokedexNumber,
                    types,
                    species.catchRate,
                    species.baseStats.values.sum(),
                )
            },
            false,
        )
        return 1
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        ExampleMod.reloadConfig()
        context.source.sendSuccess({ Component.translatable("examplemod.command.reload.success") }, true)
        return 1
    }
}
