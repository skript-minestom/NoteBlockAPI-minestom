import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    java
    `maven-publish`
}

group = "com.xxmicloxx"
version = "1.7.0-SNAPSHOT"
description = "NoteBlockAPI for Minestom"

val minestomVersion = "2026.08.16-26.2"

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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

publishing {
    val mcVersion = minestomVersion.split("-")[1]
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val ver = "$date-$mcVersion"
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = project.name + "-sm"
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
