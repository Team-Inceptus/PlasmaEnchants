plugins {
    id("org.jetbrains.dokka") version "1.8.20"
}

dependencies {
    compileOnly("com.mojang:authlib:1.5.25")
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

    processResources {
        include("**/*.properties")
    }

    shadowJar {
        dependsOn(kotlinSourcesJar, "javadocJar")
    }
}

artifacts {
    add("archives", tasks["javadocJar"])
}