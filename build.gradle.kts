import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.sonarqube") version "4.0.0.2929"
    id("com.github.johnrengelman.shadow") version "8.1.1"

    java
    `java-library`
    `maven-publish`
    jacoco
}

val pGroup = "us.teaminceptus.plasmaenchants"
val pVersion = "1.0.0-SNAPSHOT"
val pAuthor = "Team-Inceptus"

val jvmVersion: JavaVersion = JavaVersion.VERSION_11

sonarqube {
    properties {
        property("sonar.projectKey", "${pAuthor}_PlasmaEnchants")
        property("sonar.organization", "team-inceptus")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

allprojects {
    apply<JavaPlugin>()
    apply<JavaLibraryPlugin>()
    apply<JacocoPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.sonarqube")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = pGroup
    version = pVersion
    description = "Advanced, Premium Custom Enchantments Plugin, written in Kotlin, and developed for Spigot 1.14+"

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
        compileOnly("org.jetbrains:annotations:24.0.1")
        compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")

        testImplementation("org.mockito:mockito-core:5.2.0")
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
        testImplementation("com.github.seeseemelk:MockBukkit-v1.13:0.2.0")
    }

    java {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = jvmVersion.toString()
        }

        jacocoTestReport {
            dependsOn(test)

            reports {
                xml.required.set(false)
                csv.required.set(false)

                html.required.set(true)
                html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
            }
        }

        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
            finalizedBy(jacocoTestReport)
        }

        javadoc {
            enabled = false
            options.encoding = "UTF-8"
            options.memberLevel = JavadocMemberLevel.PROTECTED
        }

        jar.configure {
            enabled = false
            dependsOn("shadowJar")
        }

        withType<ShadowJar> {
            manifest {
                attributes["Implementation-Title"] = project.name
                attributes["Implementation-Version"] = project.version
                attributes["Implementation-Vendor"] = pAuthor
            }

            from("src/main/resources") {
                include("**/*")
            }

            relocate("revxrsal.commands", "us.teaminceptus.shaded.lamp")
            relocate("org.bstats.bukkit", "us.teaminceptus.shaded.bstats")

            archiveFileName.set("${project.name}-${project.version}.jar")
        }
    }
}