package com.example.examplemod.listener

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.pokemon.IVs
import com.example.examplemod.ExampleMod
import net.minecraft.network.chat.Component

/**
 * Cobblemon exposes its game events as observables on [CobblemonEvents].
 * Subscribe once during initialisation; the subscription lives for the whole session.
 */
object CaptureListener {
    fun register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL) { event ->
            val config = ExampleMod.config
            if (!config.announceCaptures) return@subscribe

            val ivTotal = event.pokemon.ivs.total()
            if (ivTotal < config.notableIvTotal) return@subscribe

            event.player.sendSystemMessage(
                Component.translatable(
                    "examplemod.capture.notable",
                    event.pokemon.getDisplayName(),
                    ivTotal,
                    IVs.MAX_TOTAL,
                ),
            )
        }
    }
}
