plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks {
    compileJava {
        val jvmVersion: String by project
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
    compileKotlin {
        kotlinOptions {
            val jvmVersion: String by project
            jvmTarget = jvmVersion
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.14-R0.1-SNAPSHOT")
}