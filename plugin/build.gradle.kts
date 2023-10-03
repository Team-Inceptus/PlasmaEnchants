import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val lampVersion = "3.1.7"

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT") {
        version {
            strictly("1.14.4-R0.1-SNAPSHOT")
        }
    }

    api(project(":plasmaenchants-api"))

    listOf(
        "1_15",
        "1_16",
        "1_17",
        "1_19",
        "1_20"
    ).forEach {
        api(project(":plasmaenchants-$it"))
    }

    compileOnly("com.mojang:authlib:1.5.25")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")
    implementation("com.github.Revxrsal.Lamp:bukkit:$lampVersion")
    implementation("com.github.Revxrsal.Lamp:common:$lampVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.javaParameters = true
    }

    processResources {
        expand(project.properties)
    }
}