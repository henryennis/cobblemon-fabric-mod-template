package com.example.examplemod

import com.example.examplemod.command.ExampleModCommand
import com.example.examplemod.config.ExampleModConfig
import com.example.examplemod.listener.CaptureListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Server and common entrypoint. Fabric instantiates this through the Kotlin language adapter
 * declared in `fabric.mod.json`, so it can stay an `object`.
 */
object ExampleMod : ModInitializer {
    const val MOD_ID = "examplemod"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var config: ExampleModConfig
        private set

    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

    fun reloadConfig() {
        config = ExampleModConfig.load(FabricLoader.getInstance().configDir.resolve("$MOD_ID.json"))
    }

    override fun onInitialize() {
        reloadConfig()
        ExampleModCommand.register()
        CaptureListener.register()
        LOGGER.info("{} initialised", MOD_ID)
    }
}
