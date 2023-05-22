import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka") version "1.8.10"
}

description = "API for the Premium Plugin PlasmaEnchants"

tasks {
    kotlinSourcesJar {
        archiveClassifier.set("sources")
    }

    register("javadocJar", Jar::class.java) {
        dependsOn(dokkaJavadoc)

        archiveClassifier.set("javadoc")
        from(dokkaJavadoc.flatMap { it.outputDirectory })
    }

    withType<ShadowJar> {
        dependsOn(kotlinSourcesJar, "javadocJar")
    }
}

artifacts {
    add("archives", tasks["javadocJar"])
}