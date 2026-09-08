import groovy.json.JsonSlurper
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TreeMap

plugins {
    java
    `maven-publish`
}

group = "com.xxmicloxx"
version = "1.7.0-SNAPSHOT"
description = "NoteBlockAPI for Minestom"

val minestomVersion = "2026.08.16-26.2"
val minecraftVersion = "26.2"

val generatedResourcesDir = layout.buildDirectory.dir("generated-resources")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom:$minestomVersion")
}

sourceSets {
    named("main") {
        resources.srcDir(generatedResourcesDir)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

/**
 * Downloads vanilla sounds.json and writes sound-file-index.properties into build/generated-resources.
 * Runs automatically before processResources. Edit [minecraftVersion] above to retarget.
 */
val updateSoundIndex by tasks.registering {
    group = "build"
    description = "Download vanilla sounds.json and regenerate sound-file-index.properties"

    inputs.property("minecraftVersion", minecraftVersion)
    val outputFile = generatedResourcesDir.map { it.file("sound-file-index.properties") }
    outputs.file(outputFile)

    doLast {
        val url = "https://raw.githubusercontent.com/misode/mcmeta/$minecraftVersion-summary/sounds/data.json"
        logger.lifecycle("Downloading sounds for Minecraft $minecraftVersion from $url")

        val jsonText = URI(url).toURL().openStream().bufferedReader().use { it.readText() }
        @Suppress("UNCHECKED_CAST")
        val root = JsonSlurper().parseText(jsonText) as Map<String, Any>

        val reverse = TreeMap<String, String>()
        for ((eventId, value) in root) {
            val entry = value as? Map<*, *> ?: continue
            val sounds = entry["sounds"] as? List<*> ?: continue
            for (sound in sounds) {
                val path = when (sound) {
                    is String -> sound
                    is Map<*, *> -> sound["name"] as? String
                    else -> null
                } ?: continue
                val normalized = path.removeSuffix(".ogg").removePrefix("minecraft:").removePrefix("minecraft/")
                reverse.putIfAbsent(normalized, eventId)
            }
        }

        val outFile = outputFile.get().asFile
        outFile.parentFile.mkdirs()

        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        outFile.bufferedWriter().use { writer ->
            writer.appendLine("# Generated from Minecraft $minecraftVersion sounds.json ($date)")
            writer.appendLine("# Do not edit by hand. Produced by Gradle task updateSoundIndex.")
            writer.appendLine("# Source: $url")
            for ((path, eventId) in reverse) {
                writer.appendLine("$path=$eventId")
            }
        }

        logger.lifecycle("Wrote ${reverse.size} path→event mappings to ${outFile.relativeTo(projectDir)}")
    }
}

tasks.named("processResources") {
    dependsOn(updateSoundIndex)
}

tasks.named("sourcesJar") {
    dependsOn(updateSoundIndex)
}

publishing {
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val ver = "$date-$minecraftVersion"
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = project.name
            version = ver
        }
    }

    repositories {
        maven {
            url = uri("https://maven.hapily.me/releases/")
            credentials {
                username = System.getenv("REPO_HAPILY_USERNAME")
                password = System.getenv("REPO_HAPILY_PASSWORD")
            }
        }
    }
}
