import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka") version "1.8.10"
}

description = "API for the Premium Plugin PlasmaEnchants"

tasks {
    kotlinSourcesJar {
        archiveFileName.set("PlasmaEnchants-API-${project.version}-sources.jar")
    }

    register("javadocJar", Jar::class.java) {
        dependsOn(dokkaJavadoc)

        archiveFileName.set("PlasmaEnchants-API-${project.version}-javadoc.jar")
        from(dokkaJavadoc.flatMap { it.outputDirectory })
    }

    withType<ShadowJar> {
        dependsOn(kotlinSourcesJar, "javadocJar")
        archiveFileName.set("PlasmaEnchants-API-${project.version}.jar")
    }
}

artifacts {
    add("archives", tasks["javadocJar"])
}