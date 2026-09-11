package com.example.examplemod.gametest

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.util.cobblemonResource
import com.example.examplemod.ExampleMod
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper

/**
 * Game tests run inside a real dedicated server with Cobblemon loaded: `./gradlew runGametest`.
 * The JUnit report lands in `build/test-results/gametest/junit.xml`.
 */
class ExampleModGameTests : FabricGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun cobblemonSpeciesResolve(helper: GameTestHelper) {
        val bidoof = PokemonSpecies.getByIdentifier(cobblemonResource("bidoof"))
        helper.assertTrue(bidoof != null, "Cobblemon should have registered Bidoof")
        helper.succeed()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun configLoadsWithDefaults(helper: GameTestHelper) {
        helper.assertTrue(ExampleMod.config.announceCaptures, "A fresh config should announce captures")
        helper.assertTrue(ExampleMod.config.notableIvTotal in 0..186, "IV threshold should be a valid IV total")
        helper.succeed()
    }
}
