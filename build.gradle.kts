plugins {
    kotlin("jvm") version "1.7.20"
}

allprojects {
    group = "us.teaminceptus.plasmaenchants"
    version = "1.0.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}