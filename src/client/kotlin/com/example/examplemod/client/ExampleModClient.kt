package com.example.examplemod.client

import com.example.examplemod.ExampleMod
import net.fabricmc.api.ClientModInitializer

/**
 * Client-only entrypoint. Code in `src/client` can use rendering and screen classes;
 * code in `src/main` cannot, which is what keeps dedicated servers from crashing.
 */
object ExampleModClient : ClientModInitializer {
    override fun onInitializeClient() {
        ExampleMod.LOGGER.debug("Client entrypoint ran")
    }
}
