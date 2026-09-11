package com.example.examplemod.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Plain JUnit tests for code that does not touch Minecraft. Anything that needs a world or registries
 * belongs in `src/gametest`.
 */
class ExampleModConfigTest {
    @Test
    fun `a missing file is created with defaults`(
        @TempDir dir: Path,
    ) {
        val path = dir.resolve("examplemod.json")

        val config = ExampleModConfig.load(path)

        assertEquals(ExampleModConfig(), config)
        assertTrue(Files.exists(path), "load should write the defaults to disk")
    }

    @Test
    fun `fields missing from the file keep their defaults`(
        @TempDir dir: Path,
    ) {
        val path = dir.resolve("examplemod.json")
        Files.writeString(path, """{ "announceCaptures": false }""")

        val config = ExampleModConfig.load(path)

        assertEquals(false, config.announceCaptures)
        assertEquals(ExampleModConfig().notableIvTotal, config.notableIvTotal)
    }

    @Test
    fun `a malformed file falls back to defaults and is left untouched`(
        @TempDir dir: Path,
    ) {
        val path = dir.resolve("examplemod.json")
        Files.writeString(path, "{ this is not json")

        val config = ExampleModConfig.load(path)

        assertEquals(ExampleModConfig(), config)
        assertEquals("{ this is not json", Files.readString(path), "load must not overwrite a file it could not read")
    }

    @Test
    fun `values survive a save and load`(
        @TempDir dir: Path,
    ) {
        val path = dir.resolve("examplemod.json")
        val original = ExampleModConfig(announceCaptures = false, notableIvTotal = 31)

        ExampleModConfig.save(path, original)

        assertEquals(original, ExampleModConfig.load(path))
    }
}
