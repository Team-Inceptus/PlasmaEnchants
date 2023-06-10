val lampVersion = "3.1.5"

dependencies {
    api(project(":plasmaenchants-api"))

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")
    implementation("com.github.Revxrsal.Lamp:bukkit:$lampVersion")
    implementation("com.github.Revxrsal.Lamp:common:$lampVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.javaParameters = true
    }

    kotlinSourcesJar {
        archiveClassifier.set("sources")
    }

    processResources {
        expand(project.properties)
    }

    shadowJar {
        dependsOn(kotlinSourcesJar)
        archiveClassifier.set("")
    }
}