@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val lampVersion = "3.1.5"

dependencies {
    api(project(":plasmaenchants-api"))

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.Revxrsal.Lamp:bukkit:$lampVersion")
    implementation("com.github.Revxrsal.Lamp:common:$lampVersion")
}

tasks {
    kotlinSourcesJar {
        archiveFileName.set("PlasmaEnchants-${project.version}-sources.jar")
    }

    withType<ProcessResources> {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    withType<ShadowJar> {
        dependsOn(kotlinSourcesJar)
        archiveFileName.set("PlasmaEnchants-${project.version}.jar")
    }
}