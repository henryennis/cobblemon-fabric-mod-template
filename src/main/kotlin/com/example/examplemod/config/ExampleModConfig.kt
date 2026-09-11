package com.example.examplemod.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Plain JSON config stored in the Fabric config directory.
 *
 * Every property has a default, which gives the class a no-argument constructor. Gson uses it,
 * so fields missing from an older config file keep their defaults instead of becoming zero or false.
 */
data class ExampleModConfig(
    /** Tell the catcher when a captured Pokémon is worth a second look. */
    val announceCaptures: Boolean = true,
    /** Minimum IV total (out of 186) that counts as a notable capture. */
    val notableIvTotal: Int = 150,
) {
    companion object {
        private val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        fun load(path: Path): ExampleModConfig {
            if (Files.notExists(path)) {
                return ExampleModConfig().also { save(path, it) }
            }
            return Files.newBufferedReader(path).use { GSON.fromJson(it, ExampleModConfig::class.java) }
                ?: ExampleModConfig()
        }

        fun save(
            path: Path,
            config: ExampleModConfig,
        ) {
            path.parent?.let(Files::createDirectories)
            Files.newBufferedWriter(path).use { GSON.toJson(config, it) }
        }
    }
}
