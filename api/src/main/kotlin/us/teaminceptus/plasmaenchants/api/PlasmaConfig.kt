package us.teaminceptus.plasmaenchants.api

import com.google.common.collect.ImmutableMap
import org.bukkit.plugin.Plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.loot.LootTables
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import java.io.File
import java.util.Locale
import java.util.logging.Logger

/**
 * Represents the main PlasmaEnchants Configuration
 */
interface PlasmaConfig {

    companion object {
        /**
         * Fetches the PlasmaEnchants plugin.
         * @return Plugin
         */
        @JvmStatic
        val plugin: Plugin
            get() = Bukkit.getPluginManager().getPlugin("PlasmaEnchants") ?: throw IllegalStateException("PlasmaEnchants is not loaded!")

        /**
         * Fetches the PlasmaEnchant plugin's logger.
         * @return Plugin's Logger
         */
        @JvmStatic
        val logger: Logger
            get() = plugin.logger

        /**
         * Fetches the PlasmaConfig instance.
         * @return PlasmaConfig Instance
         */
        @JvmStatic
        val config: PlasmaConfig
            get() = plugin as PlasmaConfig

        /**
         * Prints a throwable in the plugin namespace.
         */
        @JvmStatic
        fun print(e: Throwable) {
            logger.severe(e.javaClass.simpleName)
            logger.severe("--------------")
            logger.severe(e.message)
            for (stack: StackTraceElement in e.stackTrace) logger.severe(stack.toString())
        }

        /**
         * Fetches the Plugin's Data Folder.
         * @return Data Folder
         */
        @JvmStatic
        val dataFolder: File
            get() = plugin.dataFolder

        /**
         * Fetches the Player Data Directory.
         * @return Player Data Directory
         */
        @JvmStatic
        val playerDirectory: File
            get() = dataFolder.resolve("players")

        /**
         * Fetches the PlasmaRegistry instance.
         * @return PlasmaRegistry Instance
         */
        @JvmStatic
        val registry: PlasmaRegistry
            get() = plugin as PlasmaRegistry

        /**
         * Fetches the PlasmaEnchants Configuration.
         * @return PlasmaEnchants Configuration Instance
         */
        @JvmStatic
        val configuration: FileConfiguration
            get() = plugin.config

        /**
         * Fetches the PlasmaEnchants Configuration File.
         * @return PlasmaEnchants Configuration File
         */
        @JvmStatic
        val configFile: File
            get() = File(dataFolder, "config.yml")

        /**
         * Loads the PlasmaEnchants Configuration File.
         * @return Loaded Configuration File
         */
        @JvmStatic
        fun loadConfig(): FileConfiguration {
            for (entry in CONFIG_MAP) {
                val key = entry.key
                val data = entry.value

                if (data.section)
                    if (!configuration.isConfigurationSection(key)) configuration.createSection(key.toString())
                else
                    when {
                        data.checker(configuration, key) && !data.validator(configuration[key]) -> configuration.set(key, data.remapper(configuration[key]))
                        !data.checker(configuration, key) -> configuration.set(key, data.default)
                    }
            }

            return configuration
        }

        // Load Config Util

        private val isNumber: (FileConfiguration, String) -> Boolean = { config, path -> config.isNumber(path) }

        private val CONFIG_MAP = ImmutableMap.builder<String, ConfigData>()
            .put("language", FileConfiguration::isString, "en")

            // Enchantment Configuration
            .putSection("enchantments")

            .putSection("enchantments.spawn")
            .put("enchantments.spawn.blacklisted-enchants", FileConfiguration::isList, listOf<String>())
            .put("enchantments.spawn.whitelisted-enchants", FileConfiguration::isList, listOf<String>())
            .put("enchantments.spawn.min-level", FileConfiguration::isInt, 1)
            .put("enchantments.spawn.max-level", FileConfiguration::isInt, 2)
            .put("enchantments.spawn.luck-modifier", isNumber, 1.05)

            .putSection("enchantments.spawn.drops")
            .put("enchantments.spawn.drops.blacklisted-mobs", FileConfiguration::isList, listOf<String>(),
                { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
                { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            )
            .put("enchantments.spawn.drops.whitelisted-mobs", FileConfiguration::isList, listOf<String>(),
                { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
                { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            )
            .put("enchantments.spawn.drops.min-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .put("enchantments.spawn.drops.max-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )

            .putSection("enchantments.spawn.drops.chance")
            .put("enchantments.spawn.drops.chance.global", isNumber, 0.3)
            .put("enchantments.spawn.drops.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
                { value -> value.all { map -> map.keyNotNull("bukkit") { enchant -> Enchantment.values().map { it.key.key.lowercase() }.contains(enchant.toString().lowercase()) } &&
                            map.keyNotNull("plasma") { it.isEnchantment() } &&
                            map.keyNotNull("min-bukkit-level") { it.isChildLevel() } &&
                            map.keyNotNull("max-bukkit-level") { it.isChildLevel() } && map.keyNotNull("min-plasma-level") { it.isChildLevel() } &&
                            map.keyNotNull("max-plasma-level") { it.isChildLevel() }
                }}
            )

            .putSection("enchantments.spawn.loot")
            .put("enchantments.spawn.loot.blacklisted-loottables", FileConfiguration::isList, listOf<String>(),
                { value -> LootTables.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) })
                { old -> old.filter { type -> LootTables.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            .put("enchantments.spawn.loot.whitelisted-loottables", FileConfiguration::isList, listOf<String>(),
                { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
                { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            )
            .put("enchantments.spawn.loot.min-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .put("enchantments.spawn.loot.max-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .putSection("enchantments.spawn.loot.chance")
            .put("enchantments.spawn.loot.chance.global", isNumber, 0.3)
            .put("enchantments.spawn.loot.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
                { value -> value.all { map -> map.keyNotNull("table") { table -> LootTables.values().map { it.name }.contains(table.toString().uppercase())}
                        map.keyNotNull("chance") { it.isNumber() } &&
                        map.keyNotNull("min-level") { it.isChildLevel() } &&
                        map.keyNotNull("max-level") { it.isChildLevel() } &&
                        map.keyNotNull("blacklisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } } &&
                        map.keyNotNull("whitelisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } }
                }}
            )

            .putSection("enchantments.spawn.fishing")
            .put("enchantments.spawn.fishing.blacklisted-enchants", FileConfiguration::isList, listOf<String>())
            .put("enchantments.spawn.fishing.whitelisted-enchants", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { it.isEnchantment() } })
            .put("enchantments.spawn.fishing.min-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .put("enchantments.spawn.fishing.max-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .putSection("enchantments.spawn.fishing.chance")
            .put("enchantments.spawn.fishing.chance.global", isNumber, 0.04)
            .put("enchantments.spawn.fishing.chance.luck-of-the-sea-modifier", isNumber, 1.05)

            .putSection("enchantments.spawn.mining")
            .put("enchantments.spawn.mining.blacklisted-blocks", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { Material.matchMaterial(it) != null } },
                { old -> old.filter { Material.matchMaterial(it) != null } }
            )
            .put("enchantments.spawn.mining.whitelisted-blocks", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { Material.matchMaterial(it) != null } },
                { old -> old.filter { Material.matchMaterial(it) != null } }
            )
            .put("enchantments.spawn.mining.min-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .put("enchantments.spawn.mining.max-level", FileConfiguration::isSet, "default",
                { value -> value.isChildLevel() }
            )
            .putSection("enchantments.spawn.mining.chance")
            .put("enchantments.spawn.mining.chance.global", isNumber, 0.01)
            .put("enchantments.spawn.mining.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
                { value -> value.all { map -> map.keyNotNull("block") { block -> Material.matchMaterial(block.toString().uppercase()) != null } &&
                        map.keyNotNull("chance") { it.isNumber() } &&
                        map.keyNotNull("min-level") { it.isChildLevel() } &&
                        map.keyNotNull("max-level") { it.isChildLevel() } &&
                        map.keyNotNull("blacklisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } } &&
                        map.keyNotNull("whitelisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } }
                }}
            )


            .putSection("enchantments.trades")
            .put("enchantments.trades.professions", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } },
                { old -> old.filter { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } }
            )
            .put("enchantments.trades.blacklisted-types", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } },
                { old -> old.filter { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            )
            .put("enchantments.trades.whitelisted-types", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } },
                { old -> old.filter { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
            )
            .put("enchantments.trades.min-villager-level", FileConfiguration::isInt, 1)
            .put("enchantments.trades.max-villager-level", FileConfiguration::isInt, 5)
            .putSection("enchantments.trades.enchants")
            .put("enchantments.trades.enchants.blacklisted-enchants", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { it.isEnchantment() } },
                { old -> old.filter { it.isEnchantment() } }
            )
            .put("enchantments.trades.enchants.whitelisted-enchants", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { it.isEnchantment() } },
                { old -> old.filter { it.isEnchantment() } }
            )
            .put("enchantments.trades.enchants.min-level", FileConfiguration::isInt, 1)
            .put("enchantments.trades.enchants.max-level", FileConfiguration::isInt, 5)
            .putSection("enchantments.trades.enchants.replace")
            .put("enchantments.trades.enchants.replace.only-enchantment-books", FileConfiguration::isBoolean, false)
            .put("enchantments.trades.enchants.replace.chance", isNumber, 0.25)
            .put("enchantments.trades.enchants.replace.config", FileConfiguration::isSet, listOf<Map<String, Any>>(),
                { value -> value.all { map -> map.keyNotNull("bukkit") { enchant -> Enchantment.values().map { it.key.key.lowercase() }.contains(enchant.toString().lowercase()) } &&
                        map.keyNotNull("plasma") { it.isEnchantment() } &&
                        map.keyNotNull("min-bukkit-level") { it.isChildLevel() } &&
                        map.keyNotNull("max-bukkit-level") { it.isChildLevel() } && map.keyNotNull("min-plasma-level") { it.isChildLevel() } &&
                        map.keyNotNull("max-plasma-level") { it.isChildLevel() }
                }}
            )

            // Artifact Configuration
            .putSection("artifacts")
            .put("artifacts.disabled-artifacts", FileConfiguration::isList, listOf<String>(),
                { value -> value.all { it.isArtifact() }},
                { old -> old.filter { it.isArtifact() }}
            )

            .putSection("artifacts.spawn")
            .putSection("artifacts.spawn.killing")
            .put("artifacts.spawn.killing.global-chance", isNumber, 0.05)

            .putSection("artifacts.spawn.mining")
            .put("artifacts.spawn.mining.global-chance", isNumber, 0.03)
            .build()

        private data class ConfigData(
            val checker: (FileConfiguration, String) -> Boolean,
            val default: Any?,
            val validator: (Any?) -> Boolean = { true },
            val remapper: (Any?) -> Any? = { it },
            val section: Boolean = false,
        )

        private fun FileConfiguration.isNumber(path: String): Boolean =
            this.isInt(path) || this.isDouble(path) || this.isLong(path)

        private fun Any.isChildLevel() = toString() == "default" || toString().toIntOrNull() != null
        private fun Any.isNumber() = toString().toDoubleOrNull() != null
        private fun Any?.isEnchantment(): Boolean {
            if (this == null) return false
            return registry.enchantments.map { it.key.key }.contains(toString().lowercase())
        }
        private fun Any?.isArtifact(): Boolean {
            if (this == null) return false
            return registry.artifacts.map { it.key.key }.contains(toString().lowercase())
        }

        private inline fun <K, reified CV> ImmutableMap.Builder<K, ConfigData>.put(
            key: K,
            noinline checker: (FileConfiguration, String) -> Boolean,
            default: CV? = null,
            crossinline validator: (CV) -> Boolean = { true },
            crossinline remapper: (CV) -> CV? = { it },
        ): ImmutableMap.Builder<K, ConfigData> = put(key, ConfigData(
            checker, default,
            { if (it is CV) validator(it) else false },
            { if (it is CV) remapper(it) else it },
            false)
        )

        private fun <T> ImmutableMap.Builder<T, ConfigData>.putSection(key: T) =
            put(key, ConfigData(FileConfiguration::isConfigurationSection, null, { true }, { it }, true))

        private fun <K, V> Map<K, V>.key(key: K, predicate: (V?) -> Boolean): Boolean = predicate(get(key))

        private fun <K, V> Map<K, V>.keyNotNull(key: K, predicate: (V) -> Boolean): Boolean {
            return predicate(get(key) ?: return false)
        }

        private operator fun <T : ConfigurationSection> String.get(file: FileConfiguration, cast: Class<T>) = cast.cast(file.getConfigurationSection(this))
        private operator fun <T> String.get(file: FileConfiguration, cast: Class<T>) = file.getObject(this, cast)
        private operator fun <T> String.get(file: FileConfiguration, cast: Class<T>, default: T) = file.getObject(this, cast) ?: default

        private operator fun String.set(config: FileConfiguration, file: File, value: Any?) {
            config[this] = value
            config.save(file)
        }
    }

    /**
     * Fetches a message from the Language file.
     * @param key The key to fetch
     * @return The message
     */
    fun get(key: String): String?

    /**
     * Fetches a message from the Language file, with the plugin prefix in front.
     * @param key The key to fetch
     * @return The message
     */
    fun getMessage(key: String): String? {
        if (get(key) == null) return null
        return get("plugin.prefix") + get(key)
    }

    /**
     * Fetches the language set in the configuration.
     * @return Language Configured
     */
    var language: String
        get() = "language"[configuration, String::class.java, "en"]
        set(value) { "language"[configuration, configFile] = value }

    /**
     * Fetches the locale set in the configuration.
     * @return Locale Configured
     */
    val locale: Locale
        get() = when (language) {
            "en" -> Locale.ENGLISH
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "zh" -> Locale.CHINESE
            else -> Locale(language)
        }

    // Enchantment Configuration

    /**
     * Fetches a list of enchantments that should not spawn naturally.
     * @return List of Enchantments
     */
    var blacklistedSpawnEnchantments: List<PEnchantment>
        get() = "enchantments.spawn.blacklisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        set(value) { "enchantments.spawn.blacklisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    /**
     * Fetches a list of enchantments that only spawn naturally.
     * @return List of Enchantments
     */
    var whitelistedSpawnEnchantments: List<PEnchantment>
        get() = "enchantments.spawn.whitelisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        set(value) { "enchantments.spawn.whitelisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    /**
     * Fetches the minimum level for a naturally occuring enchantment.
     * @return Minimum Level
     */
    var enchantmenSpawnMinLevel: Int
        get() = "enchantments.spawn.min-level"[configuration, Int::class.java, 1]
        set(value) { "enchantments.spawn.min-level"[configuration, configFile] = value }

    /**
     * Fetches the maximum level for a naturally occuring enchantment.
     * @return Maximum Level
     */
    var enchantmentSpawnMaxLevel: Int
        get() = "enchantments.spawn.max-level"[configuration, Int::class.java, 2]
        set(value) { "enchantments.spawn.max-level"[configuration, configFile] = value }

    /**
     * Fetches the luck modifier for naturally occuring enchantments.
     * @return Spawn Luck Modifier
     */
    var enchantmentSpawnLuckModifier: Double
        get() = "enchantments.spawn.luck-modifier"[configuration, Double::class.java, 1.05]
        set(value) { "enchantments.spawn.luck-modifier"[configuration, configFile] = value }

    // Artifact Configuration

    /**
     * Fetches a list of artifacts that are disabled.
     * @return List of Artifacts
     */
    var disabledArtifacts: List<PArtifact>
        get() = "artifacts.disabled-artifacts"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getArtifact(it.toString()) }
        set(value) { "artifacts.disabled-artifacts"[configuration, configFile] = value.map { it.key.key } }

    /**
     * Fetches the global chance for an artifact to spawn when an entity is killed.
     * @return Global Killing Drop Chance
     */
    var artifactSpawnGlobalKillChance: Double
        get() = "artifacts.spawn.killing.global-chance"[configuration, Double::class.java, 0.05]
        set(value) { "artifacts.spawn.killing.global-chance"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific entity type for a raw artifact to spawn when its type is killed.
     * @param type The entity type to fetch
     * @return Mining Drop Chance for EntityType
     */
    fun getArtifactSpawnGlobalKillChance(type: EntityType?): Double {
        if (type == null) return artifactSpawnGlobalMiningChance

        return "artifacts.spawn.killing"[configuration, ConfigurationSection::class.java].getKeys(false).firstOrNull { it.equals(type.name, true) }?.get(configuration, Double::class.java)
            ?: artifactSpawnGlobalMiningChance
    }

    /**
     * Sets the chance for a specific entity type for a raw artifact to spawn when its type is killed.
     * @param type The entity type to fetch
     * @param value Mining Drop Chance for EntityType
     */
    fun setArtifactSpawnGlobalKillChance(type: EntityType, value: Double?) {
        "artifacts.spawn.killing.${type.name}"[configuration, configFile] = value
    }

    /**
     * Fetches the global chance for an artifact to spawn when a block is mined.
     * @return Global Mining Drop Chance
     */
    var artifactSpawnGlobalMiningChance: Double
        get() = "artifacts.spawn.mining.global-chance"[configuration, Double::class.java, 0.03]
        set(value) { "artifacts.spawn.mining.global-chance"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific material for a raw artifact to spawn when the block is mined.
     * @param material The material to fetch
     * @return Mining Drop Chance for Material
     */
    fun getArtifactSpawnGlobalMiningChance(material: Material?): Double {
        if (material == null || !material.isBlock) return artifactSpawnGlobalMiningChance

        return "artifacts.spawn.mining"[configuration, ConfigurationSection::class.java].getKeys(false).firstOrNull { it.equals(material.name, true) }?.get(configuration, Double::class.java)
            ?: artifactSpawnGlobalMiningChance
    }

    /**
     * Sets the chance for a specific material for a raw artifact to spawn when the block is mined.
     * @param material The material to fetch
     * @return Mining Drop Chance for Material
     */
    fun setArtifactSpawnGlobalMiningChance(material: Material, value: Double?) {
        "artifacts.spawn.mining.${material.name}"[configuration, configFile] = value
    }

}