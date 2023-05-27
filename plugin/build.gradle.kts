@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val lampVersion = "3.1.5"

dependencies {
    api(project(":plasmaenchants-api"))

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.Revxrsal.Lamp:bukkit:$lampVersion")
    implementation("com.github.Revxrsal.Lamp:common:$lampVersion")
}

tasks {
    kotlinSourcesJar {
        archiveClassifier.set("")
    }

    withType<ProcessResources> {
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(project.properties)
        }
    }

    withType<ShadowJar> {
        dependsOn(kotlinSourcesJar)
        archiveClassifier.set("")
    }
}