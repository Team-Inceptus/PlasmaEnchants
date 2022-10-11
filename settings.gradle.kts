rootProject.name="PlasmaEnchants"

include(":plasmaenchants-api")
project(":plasmaenchants-api").projectDir = rootDir.resolve("api");
include(":plasmaenchants")
project(":plasmaenchants").projectDir = rootDir.resolve("plugin");