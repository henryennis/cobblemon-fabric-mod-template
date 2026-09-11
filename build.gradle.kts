import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask

plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    alias(libs.plugins.mod.publish)
    `maven-publish`
}

// Mod identity comes from gradle.properties; dependency versions from gradle/libs.versions.toml.
val modIdProvider = providers.gradleProperty("mod_id")
val modId = modIdProvider.get()
val modName = providers.gradleProperty("mod_name").get()
val modVersion = providers.gradleProperty("mod_version").get()
val modAuthor = providers.gradleProperty("mod_author").get()
val modDescription = providers.gradleProperty("mod_description").get()
val modLicense = providers.gradleProperty("mod_license").get()
val githubRepo = providers.gradleProperty("github_repo").get()
val minecraftVersion = libs.versions.minecraft.get()
val cobblemonVersion = libs.versions.cobblemon.get()

group = providers.gradleProperty("maven_group").get()
version = "$modVersion+$minecraftVersion"
base.archivesName = modId

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://artefacts.cobblemon.com/releases/") { name = "Cobblemon" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

// Game tests live in their own source set and ship as a separate dev-only mod, so nothing test-related ends up in the release jar.
val gametest: SourceSet =
    sourceSets.create("gametest") {
        val main = sourceSets.main.get()
        compileClasspath += main.compileClasspath + main.output
        runtimeClasspath += main.runtimeClasspath + main.output
    }

configurations {
    named(gametest.compileClasspathConfigurationName) { extendsFrom(getByName(sourceSets.main.get().compileClasspathConfigurationName)) }
    named(gametest.runtimeClasspathConfigurationName) { extendsFrom(getByName(sourceSets.main.get().runtimeClasspathConfigurationName)) }
}

// GraalJS needs ICU4J. Minecraft ships ICU4J only on the client, and Loom strips client-only libraries from server runs,
// so a normal dependency would resolve to the client copy and vanish. This configuration resolves on its own and is
// appended to the server-side run tasks below.
val cobblemonServerRuntime: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

loom {
    // src/client holds client-only code. Referencing it from src/main is a compile error, which keeps the mod server-safe.
    splitEnvironmentSourceSets()

    mods {
        register("$modId-gametest") {
            sourceSet(gametest)
        }
    }

    runs {
        named("client") {
            programArguments.addAll("--username", "Dev")
        }
        register("gametest") {
            server()
            displayName = "Game Test"
            sourceSet = gametest.name
            runDirectory = layout.buildDirectory.dir("gametest")
            jvmArguments.addAll(
                "-Dfabric-api.gametest",
                "-Dfabric-api.gametest.report-file=${layout.buildDirectory.get().asFile}/test-results/gametest/junit.xml",
            )
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
        createSourceSet = true
        this.modId = modIdProvider
        strictValidation = true
    }
}

// configureDataGeneration registered the mod entry with the datagen source set. Main and client are the same mod.
loom.mods.named(modId) {
    sourceSet(sourceSets.main.get())
    sourceSet(sourceSets.getByName("client"))
}

dependencies {
    minecraft(libs.minecraft)
    mappings(
        loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraftVersion:${libs.versions.parchment.get()}@zip")
        },
    )

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    // The Fabric artifact bundles the shared Cobblemon code and ships a sources jar; Loom remaps both to Mojang names.
    modImplementation(libs.cobblemon.fabric)
    // Cobblemon nests GraalJS in its release jar for its battle engine; dev runs do not see nested jars.
    localRuntime(libs.bundles.cobblemon.runtime)
    cobblemonServerRuntime(libs.icu4j)

    // Dev-only convenience; not a dependency of the released mod.
    modLocalRuntime(libs.modmenu)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
    }

    withType<ProcessResources>().configureEach {
        val properties =
            mapOf(
                "version" to project.version.toString(),
                "mod_id" to modId,
                "mod_name" to modName,
                "mod_description" to modDescription,
                "mod_author" to modAuthor,
                "mod_license" to modLicense,
                "github_repo" to githubRepo,
                "minecraft_version" to minecraftVersion,
                "fabric_loader_version" to
                    libs.versions.fabric.loader
                        .get(),
                "fabric_kotlin_version" to
                    libs.versions.fabric.kotlin
                        .get(),
                "cobblemon_version" to cobblemonVersion.substringBefore("+"),
            )
        inputs.properties(properties)
        filesMatching("fabric.mod.json") { expand(properties) }
    }

    jar {
        from("LICENSE") { rename { "${it}_$modId" } }
    }

    test {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }

    listOf("runServer", "runGametest").forEach { runTask ->
        named<RunGameTask>(runTask) { classpath(cobblemonServerRuntime) }
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
    }
}

fun latestChangelogSection(): String {
    val lines = file("CHANGELOG.md").takeIf { it.exists() }?.readLines() ?: return "No changelog."
    val start = lines.indexOfFirst { it.startsWith("## ") && !it.contains("unreleased", ignoreCase = true) }
    if (start < 0) return "No changelog entry for this version."
    val next = lines.drop(start + 1).indexOfFirst { it.startsWith("## ") }
    val end = if (next < 0) lines.size else start + 1 + next
    return lines.subList(start + 1, end).joinToString("\n").trim()
}

publishMods {
    file = tasks.named<RemapJarTask>("remapJar").flatMap { it.archiveFile }
    changelog = latestChangelogSection()
    type =
        when {
            "alpha" in modVersion -> ReleaseType.ALPHA
            "beta" in modVersion -> ReleaseType.BETA
            else -> ReleaseType.STABLE
        }
    modLoaders.add("fabric")
    displayName = "$modName $modVersion for Minecraft $minecraftVersion"
    // Set MOD_PUBLISH_DRY_RUN=1 to exercise the task without uploading anything.
    dryRun = providers.environmentVariable("MOD_PUBLISH_DRY_RUN").isPresent

    val modrinthId = providers.gradleProperty("modrinth_project_id").getOrElse("")
    if (modrinthId.isNotBlank()) {
        modrinth {
            projectId = modrinthId
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add(minecraftVersion)
            requires("fabric-api", "fabric-language-kotlin", "cobblemon")
        }
    }

    val curseforgeId = providers.gradleProperty("curseforge_project_id").getOrElse("")
    if (curseforgeId.isNotBlank()) {
        curseforge {
            projectId = curseforgeId
            projectSlug = providers.gradleProperty("curseforge_project_slug")
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add(minecraftVersion)
            client = true
            server = true
            requires("fabric-api", "fabric-language-kotlin", "cobblemon")
        }
    }

    if (githubRepo.isNotBlank() && !githubRepo.startsWith("your-github/")) {
        github {
            repository = githubRepo
            accessToken = providers.environmentVariable("GITHUB_TOKEN")
            commitish = providers.environmentVariable("GITHUB_SHA").orElse("main")
            tagName = "v$modVersion"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = modId
        }
    }
    // Add the Maven repository you publish to here. This block is for publishing, not for resolving dependencies.
    repositories {}
}
