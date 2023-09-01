plugins {
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
}

apply(plugin = "maven-publish")

dependencies {
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT") {
        version {
            strictly("1.14.4-R0.1-SNAPSHOT")
        }
    }
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

publishing {
    val github = "Team-Inceptus/PlasmaEnchants"

    publications {
        create<MavenPublication>("maven") {
            pom {
                description.set(project.description)
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://github.com/$github/blob/master/LICENSE")
                    }
                }
                scm {
                    connection.set("scm:git:git://$github.git")
                    developerConnection.set("scm:git:ssh://$github.git")
                    url.set("https://github.com/$github")
                }
            }

            artifact(tasks["javadocJar"])
            artifact(tasks.kotlinSourcesJar)

            from(components["java"])
        }
    }

    repositories {
        maven {
            credentials {
                username = System.getenv("JENKINS_USERNAME")
                password = System.getenv("JENKINS_PASSWORD")
            }

            val releases = "https://repo.codemc.io/repository/maven-releases/"
            val snapshots = "https://repo.codemc.io/repository/maven-snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshots else releases)
        }
    }
}