package com.example.examplemod.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

/**
 * Runs with `./gradlew runDatagen` and writes into `src/main/generated`, which is committed.
 * Add providers here for recipes, tags, loot tables, models and so on.
 */
class ExampleModDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val pack = generator.createPack()
        pack.addProvider { output, registries -> EnglishLanguageProvider(output, registries) }
    }
}

class EnglishLanguageProvider(
    output: FabricDataOutput,
    registries: CompletableFuture<HolderLookup.Provider>,
) : FabricLanguageProvider(output, "en_us", registries) {
    override fun generateTranslations(
        registryLookup: HolderLookup.Provider,
        builder: TranslationBuilder,
    ) {
        builder.add("examplemod.command.species.unknown", "No Cobblemon species matches %s")
        builder.add("examplemod.command.species.info", "%s (#%s, %s): catch rate %s, base stat total %s")
        builder.add("examplemod.command.reload.success", "Example Mod config reloaded")
        builder.add("examplemod.capture.notable", "Notable catch! %s has an IV total of %s out of %s")
    }
}
