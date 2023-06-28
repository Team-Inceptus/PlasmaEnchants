rootProject.name = "PlasmaEnchants"

include(":plasmaenchants-api")
project(":plasmaenchants-api").projectDir = rootDir.resolve("api")

include(":plasmaenchants")
project(":plasmaenchants").projectDir = rootDir.resolve("plugin")

listOf(
    "1_15",
    "1_16",
    "1_17",
    "1_19",
    "1_20"
).forEach {
    include(":plasmaenchants-$it")
    project(":plasmaenchants-$it").projectDir = rootDir.resolve("version/$it")
}