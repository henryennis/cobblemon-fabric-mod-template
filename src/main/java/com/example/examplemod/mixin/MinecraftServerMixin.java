package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixins stay in Java: the Mixin annotation processor does not understand Kotlin sources.
 * Method names use Mojang mappings, which is what the IDE shows for vanilla classes.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void examplemod$beforeLevelLoad(CallbackInfo ci) {
        ExampleMod.LOGGER.debug("Server is about to load its levels");
    }
}
