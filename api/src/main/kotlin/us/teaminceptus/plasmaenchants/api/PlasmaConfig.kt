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
import us.teaminceptus.plasmaenchants.api.config.CONFIG_MAP
import us.teaminceptus.plasmaenchants.api.config.EnchantmentChanceConfiguration
import us.teaminceptus.plasmaenchants.api.config.EnchantmentTradesChanceConfiguration
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import java.io.File
import java.util.Locale
import java.util.logging.Logger

/**
 * Represents the main PlasmaEnchants Configuration
 */
@Suppress("unchecked_cast")
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
            val config = plugin.config

            for ((key, data) in CONFIG_MAP)
                if (data.section) {
                    if (!config.isConfigurationSection(key)) config.createSection(key.toString())
                } else {
                    val typeValid = data.checker(config, key)
                    when {
                        !typeValid || !config.isSet(key) -> config[key] = data.default
                        !data.validator(config[key]) -> config[key] = data.remapper(config[key])
                    }
                }

            config.save(configFile)

            return config
        }

        // Config Data Util

        private fun String.getSection(file: FileConfiguration) = file.getConfigurationSection(this) ?: file.createSection(this)
        private fun String.getMapList(file: FileConfiguration) = file.getMapList(this).mapNotNull { it as? Map<String, Any> ?: return@mapNotNull null }
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

    var language: String
        /**
         * Fetches the language set in the configuration.
         * @return Language Configured
         */
        get() = "language"[configuration, String::class.java, "en"]
        /**
         * Sets the language in the configuration.
         * @param value Language Key
         */
        set(value) { "language"[configuration, configFile] = value }

    val locale: Locale
        /**
         * Fetches the locale set in the configuration.
         * @return Locale Configured
         */
        get() = when (language) {
            "en" -> Locale.ENGLISH
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "zh" -> Locale.CHINESE
            else -> Locale(language)
        }

    // Enchantment Configuration

    var disabledEnchantments: List<PEnchantment>
        /**
         * Fetches a list of enchantments that are disabled.
         * @return List of Enchantments
         */
        get() = "enchantments.disabled-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that are disabled.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.disabled-enchants"[configuration, configFile] = value.map { it.key.key } }

    var isIgnoreEnchantmentLevelRestriction: Boolean
        /**
         * Fetches whether enchantment level restrictions are ignored.
         * @return true if ignored, false otherwise
         */
        get() = "enchantments.ignore-level-restriction"[configuration, Boolean::class.java, false]
        /**
         * Sets whether enchantment level restrictions are ignored.
         * @param value true if ignored, false otherwise
         */
        set(value) { "enchantments.ignore-level-restriction"[configuration, configFile] = value }

    var isIgnoreEnchantmentConflictRestriction: Boolean
        /**
         * Fetches whether enchantment conflict restrictions are ignored.
         * @return true if ignored, false otherwise
         */
        get() = "enchantments.ignore-conflict-restriction"[configuration, Boolean::class.java, false]
        /**
         * Sets whether enchantment conflict restrictions are ignored.
         * @param value true if ignored, false otherwise
         */
        set(value) { "enchantments.ignore-conflict-restriction"[configuration, configFile] = value }

    var blacklistedSpawnEnchantments: List<PEnchantment>
        /**
         * Fetches a list of enchantments that should not spawn naturally.
         * @return List of Enchantments
         */
        get() = "enchantments.spawn.blacklisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that should not spawn naturally.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.spawn.blacklisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var whitelistedSpawnEnchantments: List<PEnchantment>
        /**
         * Fetches a list of enchantments that only spawn naturally.
         * @return List of Enchantments
         */
        get() = "enchantments.spawn.whitelisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that only spawn naturally.
         * @param value List of Whitelisted Enchantments
         */
        set(value) { "enchantments.spawn.whitelisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentSpawnMinLevel: Int
        /**
         * Fetches the minimum level for a naturally occuring enchantment.
         * @return Minimum Level
         */
        get() = "enchantments.spawn.min-level"[configuration, Int::class.java, 1]
        /**
         * Sets the minimum level for a naturally occuring enchantment.
         * @param value Minimum Level
         */
        set(value) { "enchantments.spawn.min-level"[configuration, configFile] = value }

    var enchantmentSpawnMaxLevel: Int
        /**
         * Fetches the maximum level for a naturally occuring enchantment.
         * @return Maximum Level
         */
        get() = "enchantments.spawn.max-level"[configuration, Int::class.java, 2]
        /**
         * Sets the maximum level for a naturally occuring enchantment.
         * @param value Maximum Level
         */
        set(value) { "enchantments.spawn.max-level"[configuration, configFile] = value }

    var enchantmentSpawnLuckModifier: Double
        /**
         * Fetches the luck modifier for naturally occuring enchantments.
         * @return Spawn Luck Modifier
         */
        get() = "enchantments.spawn.luck-modifier"[configuration, Double::class.java, 0.05]
        /**
         * Sets the luck modifier for naturally occuring enchantments.
         * @param value Spawn Luck Modifier
         */
        set(value) { "enchantments.spawn.luck-modifier"[configuration, configFile] = value }

    var enchantmentSpawnKillingBlacklistedMobs: List<EntityType>
        /**
         * Fetches a list of mobs that should not drop naturally occuring enchantments.
         * @return List of Mobs
         */
        get() = "enchantments.spawn.drops.blacklisted-mobs"[configuration, List::class.java, listOf<String>()].mapNotNull { type -> EntityType.entries.first { it.name == type.toString().uppercase() } }
        /**
         * Sets the list of mobs that should not drop naturally occuring enchantments.
         * @param value List of Mobs
         */
        set(value) { "enchantments.spawn.drops.blacklisted-mobs"[configuration, configFile] = value.map { it.name } }

    var enchantmentSpawnKillingWhitelistedMobs: List<EntityType>
        /**
         * Fetches a list of mobs that should only drop naturally occuring enchantments.
         * @return List of Mobs
         */
        get() = "enchantments.spawn.drops.whitelisted-mobs"[configuration, List::class.java, listOf<String>()].mapNotNull { type -> EntityType.entries.first { it.name == type.toString().uppercase() } }
        /**
         * Sets the list of mobs that should only drop naturally occuring enchantments.
         * @param value List of Mobs
         */
        set(value) { "enchantments.spawn.drops.whitelisted-mobs"[configuration, configFile] = value.map { it.name } }

    var enchantmentSpawnKillingLootingModifier: Double
        /**
         * Fetches the looting modifier for naturally occuring enchantments.
         * @return Spawn Looting Modifier
         */
        get() = "enchantments.spawn.drops.chance.looting-modifier"[configuration, Double::class.java, 0.01]
        /**
         * Sets the looting modifier for naturally occuring enchantments.
         * @param value Spawn Looting Modifier
         */
        set(value) { "enchantments.spawn.drops.chance.looting-modifier"[configuration, configFile] = value }

    var enchantmentSpawnKillingMaxLevel: Int
        /**
         * Fetches the maximum level for naturally occuring enchantments when an entity is killed.
         * @return Maximum Level
         */
        get() = when ("enchantments.spawn.drops.max-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMaxLevel
            else -> "enchantments.spawn.drops.max-level"[configuration, Int::class.java, enchantmentSpawnMaxLevel]
        }/**
         * Sets the maximum level for naturally occuring enchantments when an entity is killed.
         * This accepts integers and the "default" to refer to [enchantmentSpawnMaxLevel].
         * @param value Maximum Level
         */
        set(value) { "enchantments.spawn.drops.max-level"[configuration, configFile] = value }

    var enchantmentSpawnKillingMinLevel: Int
        /**
         * Fetches the minimum level for naturally occuring enchantments when an entity is killed.
         * @return Minimum Level
         */
        get() = when ("enchantments.spawn.drops.min-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMinLevel
            else -> "enchantments.spawn.drops.min-level"[configuration, Int::class.java, enchantmentSpawnMinLevel]
        }
        /**
         * Sets the minimum level for naturally occuring enchantments when an entity is killed.
         * @param value Minimum Level
         */
        set(value) { "enchantments.spawn.drops.min-level"[configuration, configFile] = value }

    var enchantmentSpawnGlobalKillingChance: Double
        /**
         * Fetches the global chance for an enchantment to spawn when an entity is killed.
         * @return Global Chance
         */
        get() = "enchantments.spawn.drops.chance.global"[configuration, Double::class.java, 0.007]
        /**
         * Sets the global chance for an enchantment to spawn when an entity is killed.
         * @param value Global Chance
         */
        set(value) { "enchantments.spawn.drops.chance.global"[configuration, configFile] = value }

    val enchantmentSpawnKillingChanceConfiguration: Set<EnchantmentChanceConfiguration<EntityType>>
        /**
         * Fethces an immutable copy of the additional chance configuration for enchantments to spawn when an entity is killed.
         * @return Set of Enchantment Chance Configurations
         */
        get() =
            "enchantments.spawn.drops.chance.config".getMapList(configuration).map { map ->
                EnchantmentChanceConfiguration<EntityType>(map.mapKeys { it.key }, enchantmentSpawnKillingMinLevel, enchantmentSpawnKillingMaxLevel)
            }.toSet()

    var enchantmentSpawnLootBlacklistedLootTables: List<LootTables>
        /**
         * Fetches an immutable copy of list of loot tables that should not include naturally spawned enchantments.
         * @return List of Blacklisted Loot Tables
         */
        get() = "enchantments.spawn.loot.blacklisted-loottables"[configuration, List::class.java, listOf<String>()].mapNotNull { table -> LootTables.entries.first { it.name == table.toString().uppercase() } }
        /**
         * Sets the list of loot tables that should not include naturally spawned enchantments.
         * @param value List of Blacklisted Loot Tables
         */
        set(value) { "enchantments.spawn.loot.blacklisted-loottables"[configuration, configFile] = value.map { it.name } }

    var enchantmentSpawnLootWhitelistedLootTables: List<LootTables>
        /**
         * Fetches an immutable copy of list of loot tables that should only include naturally spawned enchantments.
         * @return List of Whitelisted Loot Tables
         */
        get() = "enchantments.spawn.loot.whitelisted-loottables"[configuration, List::class.java, listOf<String>()].mapNotNull { table -> LootTables.entries.first { it.name == table.toString().uppercase() } }
        /**
         * Sets the list of loot tables that should only include naturally spawned enchantments.
         * @param value List of Whitelisted Loot Tables
         */
        set(value) { "enchantments.spawn.loot.whitelisted-loottables"[configuration, configFile] = value.map { it.name } }

    var enchantmentSpawnLootMinLevel: Int
        /**
         * Fetches the minimum level for naturally occuring enchantments when an item is looted from a loot table.
         * @return Minimum Level
         */
        get() = when ("enchantments.spawn.loot.min-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMinLevel
            else -> "enchantments.spawn.loot.min-level"[configuration, Int::class.java, enchantmentSpawnMinLevel]
        }
        /**
         * Sets the minimum level for naturally occuring enchantments when an item is looted from a loot table.
         * @param value Minimum Level
         */
        set(value) { "enchantments.spawn.loot.min-level"[configuration, configFile] = value }

    var enchantmentSpawnLootMaxLevel: Int
        /**
         * Fetches the maximum level for naturally occuring enchantments when an item is looted from a loot table.
         * @return Maximum Level
         */
        get() = when ("enchantments.spawn.loot.max-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMaxLevel
            else -> "enchantments.spawn.loot.max-level"[configuration, Int::class.java, enchantmentSpawnMaxLevel]
        }
        /**
         * Sets the maximum level for naturally occuring enchantments when an item is looted from a loot table.
         * @param value Maximum Level
         */
        set(value) { "enchantments.spawn.loot.max-level"[configuration, configFile] = value }

    var enchantmentSpawnGlobalLootChance: Double
        /**
         * Fetches the global chance for an enchantment to spawn when an item is looted from a loot table.
         * @return Global Chance
         */
        get() = "enchantments.spawn.loot.chance.global"[configuration, Double::class.java, 0.3]
        /**
         * Sets the global chance for an enchantment to spawn when an item is looted from a loot table.
         * @param value Global Chance
         */
        set(value) { "enchantments.spawn.loot.chance.global"[configuration, configFile] = value }

    val enchantmentSpawnLootChanceConfiguration: Set<EnchantmentChanceConfiguration<LootTables>>
        /**
         * Fethces an immutable copy of the additional chance configuration for enchantments to spawn when an item is looted from a loot table.
         * @return Set of Enchantment Chance Configurations
         */
        get() =
            "enchantments.spawn.loot.chance.config".getMapList(configuration).map { map ->
                EnchantmentChanceConfiguration<LootTables>(map.mapKeys { it.key }, enchantmentSpawnLootMinLevel, enchantmentSpawnLootMaxLevel)
            }.toSet()

    var enchantmentSpawnFishingBlacklistedEnchants: List<PEnchantment>
        /**
         * Fetches an immutable copy of list of enchantments that should not be naturally spawned when fishing.
         * @return List of Blacklisted Enchantments
         */
        get() = "enchantments.spawn.fishing.blacklisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { enchantment -> registry.getEnchantment(enchantment.toString()) }
        /**
         * Sets the list of enchantments that should not be naturally spawned when fishing.
         * @param value List of Blacklisted Enchantments
         */
        set(value) { "enchantments.spawn.fishing.blacklisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentSpawnFishingWhitelistedEnchants: List<PEnchantment>
        /**
         * Fetches an immutable copy of list of enchantments that should only be naturally spawned when fishing.
         * @return List of Whitelisted Enchantments
         */
        get() = "enchantments.spawn.fishing.whitelisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { enchantment -> registry.getEnchantment(enchantment.toString()) }
        /**
         * Sets the list of enchantments that should only be naturally spawned when fishing.
         * @param value List of Whitelisted Enchantments
         */
        set(value) { "enchantments.spawn.fishing.whitelisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentSpawnFishingMinLevel: Int
        /**
         * Fetches the minimum level for naturally occuring enchantments when an item is fished.
         * @return Minimum Level
         */
        get() = when ("enchantments.spawn.fishing.min-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMinLevel
            else -> "enchantments.spawn.fishing.min-level"[configuration, Int::class.java, enchantmentSpawnMinLevel]
        }
        /**
         * Sets the minimum level for naturally occuring enchantments when an item is fished.
         * @param value Minimum Level
         */
        set(value) { "enchantments.spawn.fishing.min-level"[configuration, configFile] = value }

    var enchantmentSpawnFishingMaxLevel: Int
        /**
         * Fetches the maximum level for naturally occuring enchantments when an item is fished.
         * @return Maximum Level
         */
        get() = when ("enchantments.spawn.fishing.max-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMaxLevel
            else -> "enchantments.spawn.fishing.max-level"[configuration, Int::class.java, enchantmentSpawnMaxLevel]
        }
        /**
         * Sets the maximum level for naturally occuring enchantments when an item is fished.
         * @param value Maximum Level
         */
        set(value) { "enchantments.spawn.fishing.max-level"[configuration, configFile] = value }

    var enchantmentSpawnGlobalFishingChance: Double
        /**
         * Fetches the global chance for an enchantment to spawn when an item is fished.
         * @return Global Chance
         */
        get() = "enchantments.spawn.fishing.chance.global"[configuration, Double::class.java, 0.04]
        /**
         * Sets the global chance for an enchantment to spawn when an item is fished.
         * @param value Global Chance
         */
        set(value) { "enchantments.spawn.fishing.chance.global"[configuration, configFile] = value }

    var enchantmentSpawnFishingLuckOfTheSeaModifier: Double
        /**
         * Fetches the modifier for the Luck of the Sea enchantment when an item is fished.
         * @return Luck of the Sea Modifier
         */
        get() = "enchantments.spawn.fishing.chance.luck-of-the-sea-modifier"[configuration, Double::class.java, 0.03]
        /**
         * Sets the modifier for the Luck of the Sea enchantment when an item is fished.
         * @param value Luck of the Sea Modifier
         */
        set(value) { "enchantments.spawn.fishing.chance.luck-of-the-sea-modifier"[configuration, configFile] = value }

    var enchantmentSpawnMiningBlacklistedBlocks: List<Material>
        /**
         * Fetches an immutable copy of list of blocks that should not naturally spawn enchanted books when mined.
         * @return List of Blacklisted Blocks
         */
        get() = "enchantments.spawn.mining.blacklisted-blocks"[configuration, List::class.java, listOf<String>()].mapNotNull { Material.matchMaterial(it.toString()) }
        /**
         * Sets the list of blocks that should not naturally spawn enchanted books when mined.
         * @param value List of Blacklisted Blocks
         */
        set(value) { "enchantments.spawn.mining.blacklisted-blocks"[configuration, configFile] = value.map { it.name } }

    var enchantmentSpawnMiningWhitelistedBlocks: List<Material>
        /**
         * Fetches an immutable copy of list of blocks that should only naturally spawn enchanted books when mined.
         * @return List of Whitelisted Blocks
         */
        get() = "enchantments.spawn.mining.whitelisted-blocks"[configuration, List::class.java, listOf<String>()].mapNotNull { Material.matchMaterial(it.toString()) }
        /**
         * Sets the list of blocks that should only naturally spawn enchanted books when mined.
         * @param value List of Whitelisted Blocks
         */
        set(value) { "enchantments.spawn.mining.whitelisted-blocks"[configuration, configFile] = value.map { it.name } }

    var isEnchantmentSpawnMiningIgnoreSilkTouch: Boolean
        /**
         * Fetches whether or not Silk Touch is ignored when mining. If true, blocks mined with Silk Touch will not drop enchanted books.
         * @return true if ignored, false otherwise
         */
        get() = "enchantments.spawn.mining.ignore-silk-touch"[configuration, Boolean::class.java, false]
        /**
         * Sets whether or not Silk Touch is ignored when mining. If true, blocks mined with Silk Touch will not drop enchanted books.
         * @param value true if ignored, false otherwise
         */
        set(value) { "enchantments.spawn.mining.ignore-silk-touch"[configuration, configFile] = value }

    var enchantmentSpawnMiningMinLevel: Int
        /**
         * Fetches the minimum level for naturally occuring enchantments when a block is mined.
         * @return Minimum Level
         */
        get() = when ("enchantments.spawn.mining.min-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMinLevel
            else -> "enchantments.spawn.mining.min-level"[configuration, Int::class.java, enchantmentSpawnMinLevel]
        }
        /**
         * Sets the minimum level for naturally occuring enchantments when a block is mined.
         * @param value Minimum Level
         */
        set(value) { "enchantments.spawn.mining.min-level"[configuration, configFile] = value }

    var enchantmentSpawnMiningMaxLevel: Int
        /**
         * Fetches the maximum level for naturally occuring enchantments when a block is mined.
         * @return Maximum Level
         */
        get() = when ("enchantments.spawn.mining.max-level"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentSpawnMaxLevel
            else -> "enchantments.spawn.mining.max-level"[configuration, Int::class.java, enchantmentSpawnMaxLevel]
        }
        /**
         * Sets the maximum level for naturally occuring enchantments when a block is mined.
         * @param value Maximum Level
         */
        set(value) { "enchantments.spawn.mining.max-level"[configuration, configFile] = value }

    var enchantmentSpawnMiningFortuneModifier: Double
        /**
         * Fetches the modifier for the Fortune enchantment when a block is mined.
         * @return Fortune Modifier
         */
        get() = "enchantments.spawn.mining.chance.fortune-modifier"[configuration, Double::class.java, 0.01]
        /**
         * Sets the modifier for the Fortune enchantment when a block is mined.
         * @param value Fortune Modifier
         */
        set(value) { "enchantments.spawn.mining.chance.fortune-modifier"[configuration, configFile] = value }

    var enchantmentSpawnGlobalMiningChance: Double
        /**
         * Fetches the global chance for an enchantment to spawn when a block is mined.
         * @return Global Chance
         */
        get() = "enchantments.spawn.mining.chance.global"[configuration, Double::class.java, 0.0005]
        /**
         * Sets the global chance for an enchantment to spawn when a block is mined.
         * @param value Global Chance
         */
        set(value) { "enchantments.spawn.mining.chance.global"[configuration, configFile] = value }

    val enchantmentSpawnMiningChanceConfiguration: Set<EnchantmentChanceConfiguration<Material>>
        /**
         * Fetches an immutable copy of the enchantment chance configurations for mining.
         * @return Set of Enchantment Chance Configurations
         */
        get() =
            "enchantments.spawn.mining.chance.config".getMapList(configuration).map { map ->
                EnchantmentChanceConfiguration<Material>(map.mapKeys { it.key }, enchantmentSpawnMiningMinLevel, enchantmentSpawnMiningMaxLevel)
            }.toSet()

    var enchantmentTradesProfessions: List<Villager.Profession>
        /**
         * Fetches an immutable copy of the list of professions that can trade enchanted books.
         * @return List of Professions
         */
        get() = "enchantments.trades.professions"[configuration, List::class.java, listOf<String>()].mapNotNull { Villager.Profession.valueOf(it.toString()) }
        /**
         * Sets the list of professions that can trade enchanted books.
         * @param value List of Professions
         */
        set(value) { "enchantments.trades.professions"[configuration, configFile] = value.map { it.name } }

    var enchantmentTradesBlacklistedTypes: List<Villager.Type>
        /**
         * Fetches an immutable copy of the list of villager types that cannot trade enchanted books.
         * @return List of Villager Types
         */
        get() = "enchantments.trades.blacklisted-types"[configuration, List::class.java, listOf<String>()].mapNotNull { Villager.Type.valueOf(it.toString()) }
        /**
         * Sets the list of villager types that cannot trade enchanted books.
         * @param value List of Villager Types
         */
        set(value) { "enchantments.trades.blacklisted-types"[configuration, configFile] = value.map { it.name } }

    var enchantmentTradesWhitelistedTypes: List<Villager.Type>
        /**
         * Fetches an immutable copy of the list of villager types that can trade enchanted books.
         * @return List of Villager Types
         */
        get() = "enchantments.trades.whitelisted-types"[configuration, List::class.java, listOf<String>()].mapNotNull { Villager.Type.valueOf(it.toString()) }
        /**
         * Sets the list of villager types that can trade enchanted books.
         * @param value List of Villager Types
         */
        set(value) { "enchantments.trades.whitelisted-types"[configuration, configFile] = value.map { it.name } }

    var enchantmentTradesMinVillagerLevel: Int
        /**
         * Fetches the minimum villager occupation level for a villager to trade enchanted books.
         * @return Minimum Level
         */
        get() = "enchantments.trades.min-level"[configuration, Int::class.java, 1]
        /**
         * Sets the minimum villager occupation level for a villager to trade enchanted books.
         * @param value Minimum Level
         */
        set(value) { "enchantments.trades.min-level"[configuration, configFile] = value }

    var enchantmentTradesMaxVillagerLevel: Int
        /**
         * Fetches the maximum villager occupation level for a villager to trade enchanted books.
         * @return Maximum Level
         */
        get() = "enchantments.trades.max-level"[configuration, Int::class.java, 5]
        /**
         * Sets the maximum villager occupation level for a villager to trade enchanted books.
         * @param value Maximum Level
         */
        set(value) { "enchantments.trades.max-level"[configuration, configFile] = value }

    var isEnchantmentTradesBuyBooks: Boolean
        /**
         * Fetches whether or not villagers can buy enchanted books, trading them for emeralds.
         * @return true if can buy books, false otherwise
         */
        get() = "enchantments.trades.buy-books"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not villagers can sell enchanted books.
         * @param value True if villagers can sell enchanted books
         */
        set(value) { "enchantments.trades.buy-books"[configuration, configFile] = value }

    var enchantmentTradesBlacklistedEnchantments: List<PEnchantment>
        /**
         * Fetches an immutable copy of the list of enchantments that cannot be traded by villagers.
         * @return List of Enchantments
         */
        get() = "enchantments.trades.enchants.blacklisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that cannot be traded by villagers.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.trades.enchants.blacklisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentTradesWhitelistedEnchantments: List<PEnchantment>
        /**
         * Fetches an immutable copy of the list of enchantments that can be traded by villagers.
         * @return List of Enchantments
         */
        get() = "enchantments.trades.enchants.whitelisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that can be traded by villagers.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.trades.enchants.whitelisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentTradesMinEnchantLevel: Int
        /**
         * Fetches the minimum enchantment level for enchanted books to be traded by villagers.
         * @return Minimum Level
         */
        get() = "enchantments.trades.enchants.min-level"[configuration, Int::class.java, 1]
        /**
         * Sets the minimum enchantment level for enchanted books to be traded by villagers.
         * @param value Minimum Level
         */
        set(value) { "enchantments.trades.enchants.min-level"[configuration, configFile] = value }

    var enchantmentTradesMaxEnchantLevel: Int
        /**
         * Fetches the maximum enchantment level for enchanted books to be traded by villagers.
         * @return Maximum Level
         */
        get() = "enchantments.trades.enchants.max-level"[configuration, Int::class.java, 5]
        /**
         * Sets the maximum enchantment level for enchanted books to be traded by villagers.
         * @param value Maximum Level
         */
        set(value) { "enchantments.trades.enchants.max-level"[configuration, configFile] = value }

    var isEnchantmentTradesReplaceBooksOnly: Boolean
        /**
         * Fetches whether or not villagers can only replace enchanted books with PlasmaEnchants enchanted books.
         * @return true if can only replace books, false otherwise
         */
        get() = "enchantments.trades.enchants.replace.only-enchantment-books"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not villagers can only replace enchanted books with PlasmaEnchants enchanted books.
         * @param value true if can only replace books, false otherwise
         */
        set(value) { "enchantments.trades.enchants.replace.only-enchantment-books"[configuration, configFile] = value }

    var enchantmentTradesReplaceChance: Double
        /**
         * Fetches the chance for a villager to replace an enchanted book with a PlasmaEnchants enchanted book.
         * @return Chance
         */
        get() = "enchantments.trades.enchants.replace.chance"[configuration, Double::class.java, 0.25]
        /**
         * Sets the chance for a villager to replace an enchanted book with a PlasmaEnchants enchanted book.
         * @param value Chance
         */
        set(value) { "enchantments.trades.enchants.replace.chance"[configuration, configFile] = value }

    val enchantmentTradesReplaceConfiguration: Set<EnchantmentTradesChanceConfiguration>
        /**
         * Fetches the set of EnchantmentTradesChanceConfiguration objects that define the chance for a villager to replace an enchanted book with a PlasmaEnchants enchanted book.
         * @return Set of EnchantmentTradesChanceConfiguration objects
         */
        get() = "enchantments.trades.enchants.replace.config".getMapList(configuration).map { map ->
            EnchantmentTradesChanceConfiguration(map.mapKeys { it.toString() })
        }.toSet()

    var enchantmentTradesMaxUses: Int
        /**
         * Fetches the maximum number of times a villager can trade enchanted books.
         * @return Maximum Uses
         */
        get() = "enchantments.trades.max-uses"[configuration, Int::class.java, 20]
        /**
         * Sets the maximum number of times a villager can trade enchanted books.
         * @param value Maximum Uses
         */
        set(value) { "enchantments.trades.max-uses"[configuration, configFile] = value }

    var enchantmentTradesEmeraldPrice: Int
        /**
         * Fetches the default number of emeralds required to trade for an enchanted book.
         * @return Emerald Price
         */
        get() = "enchantments.trades.emerald-price.normal"[configuration, Int::class.java, 15]
        /**
         * Sets the default number of emeralds required to trade for an enchanted book.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.normal"[configuration, configFile] = value }

    var enchantmentTradesAttackingEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for an enchanted book with the [PType.ATTACKING] type.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.attacking"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.attacking"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for an enchanted book with the [PType.ATTACKING] type.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.attacking"[configuration, configFile] = value }

    var enchantmentTradesDefendingEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for an enchanted book with the [PType.DEFENDING] type.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.defending"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.defending"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for an enchanted book with the [PType.DEFENDING] type.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.defending"[configuration, configFile] = value }

    var enchantmentTradesMiningEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for an enchanted book with the [PType.MINING] and [PType.BLOCK_BREAK] type.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.mining"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.mining"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for an enchanted book with the [PType.MINING] and [PType.BLOCK_BREAK] type.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.mining"[configuration, configFile] = value }

    var enchantmentTradesPassiveEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for an enchanted book with the [PType.PASSIVE] type.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.passive"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.passive"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for an enchanted book with the [PType.PASSIVE] type.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.passive"[configuration, configFile] = value }

    var enchantmentTradesRangedEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for an enchanted book with the [PType.SHOOT_BOW] type.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.ranged"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.ranged"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for an enchanted book with the [PType.SHOOT_BOW] type.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.ranged"[configuration, configFile] = value }

    var enchantmentTradesCollectorEmeraldPrice: Int
        /**
         * Fetches the number of emeralds required to trade for a collector enchanted book.
         * @return Emerald Price
         */
        get() = when ("enchantments.trades.emerald-price.collector"[configuration, Any::class.java, "default"]) {
            "default" -> enchantmentTradesEmeraldPrice
            else -> "enchantments.trades.emerald-price.collector"[configuration, Int::class.java, enchantmentTradesEmeraldPrice]
        }
        /**
         * Sets the number of emeralds required to trade for a collector enchanted book.
         * @param value Emerald Price
         */
        set(value) { "enchantments.trades.emerald-price.collector"[configuration, configFile] = value }

    /**
     * Fetches the number of emeralds required to trade for an enchanted book.
     * @param enchant Enchantment to search for
     * @return Emerald Price
     */
    fun getEnchantmentTradesEmeraldPrice(enchant: PEnchantment) =
        when (enchant.type) {
            PType.ATTACKING -> enchantmentTradesAttackingEmeraldPrice
            PType.DEFENDING -> enchantmentTradesDefendingEmeraldPrice
            PType.MINING, PType.BLOCK_BREAK -> enchantmentTradesMiningEmeraldPrice
            PType.PASSIVE -> enchantmentTradesPassiveEmeraldPrice
            PType.SHOOT_BOW -> enchantmentTradesRangedEmeraldPrice
            else -> if (enchant.key.key.contains("_collector")) enchantmentTradesCollectorEmeraldPrice else enchantmentTradesEmeraldPrice
        }

    var isEnchantmentTradesIncludeWanderingTrader: Boolean
    /**
         * Fetches whether or not the Wandering Trader can trade for enchanted books.
         * @return true if included, false otherwise
         */
        get() = "enchantments.trades.include-wandering-trader"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not the Wandering Trader can trade for enchanted books.
         * @param value true if included, false otherwise
         */
        set(value) { "enchantments.trades.include-wandering-trader"[configuration, configFile] = value }

    // Artifact Configuration

    var disabledArtifacts: List<PArtifact>
        /**
         * Fetches a list of artifacts that are disabled.
         * @return List of Artifacts
         */
        get() = "artifacts.disabled-artifacts"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getArtifact(it.toString()) }
        /**
         * Sets the list of artifacts that are disabled.
         * @param value List of Disabled Artifacts
         */
        set(value) { "artifacts.disabled-artifacts"[configuration, configFile] = value.map { it.key.key } }

    var artifactSpawnLuckModifier: Double
        /**
         * Fetches the luck modifier for spawning a [PArtifact.RAW_ARTIFACT].
         * @return Spawn Luck Modifier
         */
        get() = "artifacts.spawn.luck-modifier"[configuration, Double::class.java, 0.05]
        /**
         * Sets the luck modifier for spawning a [PArtifact.RAW_ARTIFACT].
         * @param value Spawn Luck Modifier
         */
        set(value) { "artifacts.spawn.luck-modifier"[configuration, configFile] = value }

    var artifactSpawnKillingLootingModifier: Double
        /**
         * Fetches the looting modifier for spawning a [PArtifact.RAW_ARTIFACT] when killing an entity.
         * @return Spawn Looting Modifier
         */
        get() = "artifacts.spawn.killing.looting-modifier"[configuration, Double::class.java, 0.01]
        /**
         * Sets the looting modifier for spawning a [PArtifact.RAW_ARTIFACT] when killing an entity.
         * @param value Spawn Looting Modifier
         */
        set(value) { "artifacts.spawn.killing.looting-modifier"[configuration, configFile] = value }

    var artifactSpawnGlobalKillChance: Double
        /**
         * Fetches the global chance for an artifact to spawn when an entity is killed.
         * @return Global Killing Drop Chance
         */
        get() = "artifacts.spawn.killing.global-chance"[configuration, Double::class.java, 0.005]
        /**
         * Sets the global chance for an artifact to spawn when an entity is killed.
         * @param value Global Killing Drop Chance
         */
        set(value) { "artifacts.spawn.killing.global-chance"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific entity type for a raw artifact to spawn when its type is killed.
     * @param type The entity type to fetch
     * @return Mining Drop Chance for EntityType
     */
    fun getArtifactSpawnGlobalKillChance(type: EntityType?): Double {
        if (type == null) return artifactSpawnGlobalMiningChance

        return "artifacts.spawn.killing".getSection(configuration).getKeys(false).firstOrNull { it.equals(type.name, true) }?.get(configuration, Double::class.java)
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

    var artifactSpawnGlobalLootChance: Double
        /**
         * Fetches the global chance for an artifact to spawn when a loot table is rolled.
         * @return Global Loot Drop Chance
         */
        get() = "artifacts.spawn.loot.global-chance"[configuration, Double::class.java, 0.25]
        /**
         * Sets the global chance for an artifact to spawn when a loot table is rolled.
         * @param value Global Loot Drop Chance
         */
        set(value) { "artifacts.spawn.loot.global-chance"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific loot table for a raw artifact to spawn when it is rolled.
     * @param type The loot table to fetch
     */
    fun getArtifactSpawnGlobalLootChance(type: LootTables?): Double {
        if (type == null) return artifactSpawnGlobalLootChance

        return "artifacts.spawn.loot".getSection(configuration).getKeys(false).firstOrNull { it.equals(type.name, true) }?.get(configuration, Double::class.java)
            ?: artifactSpawnGlobalLootChance
    }

    /**
     * Sets the chance for a specific loot table for a raw artifact to spawn when it is rolled.
     * @param type The loot table to fetch
     */
    fun setArtifactSpawnGlobalLootChance(type: LootTables, value: Double?) {
        "artifacts.spawn.loot.${type.name}"[configuration, configFile] = value
    }

    var artifactSpawnFishingLuckOfTheSeaModifier: Double
        /**
         * Fetches the luck of the sea modifier for spawning a [PArtifact.RAW_ARTIFACT] when fishing.
         * @return Spawn Luck of the Sea Modifier
         */
        get() = "artifacts.spawn.fishing.luck-of-the-sea-modifier"[configuration, Double::class.java, 0.01]
        /**
         * Sets the luck of the sea modifier for spawning a [PArtifact.RAW_ARTIFACT] when fishing.
         * @param value Spawn Luck of the Sea Modifier
         */
        set(value) { "artifacts.spawn.fishing.luck-of-the-sea-modifier"[configuration, configFile] = value }

    var artifactSpawnGlobalFishingChance: Double
        /**
         * Fetches the chance for an artifact to spawn when fishing.
         * @return Fishing Drop Chance
         */
        get() = "artifacts.spawn.fishing.global-chance"[configuration, Double::class.java, 0.01]
        /**
         * Sets the chance for an artifact to spawn when fishing.
         * @param value Fishing Drop Chance
         */
        set(value) { "artifacts.spawn.fishing.global-chance"[configuration, configFile] = value }

    var artifactSpawnMiningFortuneModifier: Double
        /**
         * Fetches the fortune modifier for spawning a [PArtifact.RAW_ARTIFACT] when a block is mined.
         * @return Spawn Fortune Modifier
         */
        get() = "artifacts.spawn.mining.fortune-modifier"[configuration, Double::class.java, 0.02]
        /**
         * Sets the fortune modifier for spawning a [PArtifact.RAW_ARTIFACT] when a block is mined.
         * @param value Spawn Fortune Modifier
         */
        set(value) { "artifacts.spawn.mining.fortune-modifier"[configuration, configFile] = value }

    var artifactSpawnGlobalMiningChance: Double
        /**
         * Fetches the global chance for an artifact to spawn when a block is mined.
         * @return Global Mining Drop Chance
         */
        get() = "artifacts.spawn.mining.global-chance"[configuration, Double::class.java, 0.0003]
        /**
         * Sets the global chance for an artifact to spawn when a block is mined.
         * @param value Global Mining Drop Chance
         */
        set(value) { "artifacts.spawn.mining.global-chance"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific material for a raw artifact to spawn when the block is mined.
     * @param material The material to fetch
     * @return Mining Drop Chance for Material
     */
    fun getArtifactSpawnGlobalMiningChance(material: Material?): Double {
        if (material == null || !material.isBlock) return artifactSpawnGlobalMiningChance

        return "artifacts.spawn.mining".getSection(configuration).getKeys(false).firstOrNull { it.equals(material.name, true) }?.get(configuration, Double::class.java)
            ?: artifactSpawnGlobalMiningChance
    }

    /**
     * Sets the chance for a specific material for a raw artifact to spawn when the block is mined.
     * @param material The material to fetch
     * @param value The new Mining Drop Chance for Material
     */
    fun setArtifactSpawnGlobalMiningChance(material: Material, value: Double?) {
        "artifacts.spawn.mining.${material.name}"[configuration, configFile] = value
    }

    var artifactTradesProfessions: List<Villager.Profession>
        /**
         * Fetches the list of professions that can trade artifacts.
         * @return List of Villager Professions
         */
        get() = "artifacts.trades.professions"[configuration, List::class.java, listOf<String>()].mapNotNull { profession -> Villager.Profession.entries.firstOrNull { it.name.equals(profession.toString(), ignoreCase = true) } }
        /**
         * Sets the list of professions that can trade artifacts.
         * @param value List of Villager Professions
         */
        set(value) { "artifacts.trades.professions"[configuration, configFile] = value }

    var isArtifactTradesCraftableEnabled: Boolean
        /**
         * Fetches whether or not villagers can trade crafted artifacts.
         * @return true if enabled, false otherwise
         */
        get() = "artifacts.trades.craftable-artifacts.enabled"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not villagers can trade crafted artifacts.
         * @param value true if enabled, false otherwise
         */
        set(value) { "artifacts.trades.craftable-artifacts.enabled"[configuration, configFile] = value }

    var artifactTradesCraftableProfessions: List<Villager.Profession>
        /**
         * Fetches the list of professions that can trade crafted artifacts.
         * @return List of Villager Professions
         */
        get() = "artifacts.trades.craftable-artifacts.professions"[configuration, List::class.java, listOf<String>()].mapNotNull { profession -> Villager.Profession.entries.firstOrNull { it.name.equals(profession.toString(), ignoreCase = true) } }.ifEmpty { artifactTradesProfessions }
        /**
         * Sets the list of professions that can trade crafted artifacts.
         * @param value List of Villager Professions
         */
        set(value) { "artifacts.trades.craftable-artifacts.professions"[configuration, configFile] = value }

    var artifactTradesCraftableBlacklist: List<PArtifact>
        /**
         * Fetches the list of artifacts that cannot be traded by villagers.
         * @return List of Artifacts
         */
        get() = "artifacts.trades.craftable-artifacts.blacklisted-artifacts"[configuration, List::class.java, listOf<String>()].mapNotNull { artifact -> registry.artifacts.firstOrNull { it.key.toString() == artifact.toString().lowercase() || it.key.key == artifact.toString().lowercase() } }
        /**
         * Sets the list of artifacts that cannot be traded by villagers.
         * @param value List of Artifacts
         */
        set(value) { "artifacts.trades.craftable-artifacts.blacklisted-artifacts"[configuration, configFile] = value }

    var artifactTradesCraftableWhitelist: List<PArtifact>
        /**
         * Fetches the list of artifacts that can only be traded by villagers.
         * @return List of Whitelisted Artifacts
         */
        get() = "artifacts.trades.craftable-artifacts.whitelisted-artifacts"[configuration, List::class.java, listOf<String>()].mapNotNull { artifact -> registry.artifacts.firstOrNull { it.key.toString() == artifact.toString().lowercase() || it.key.key == artifact.toString().lowercase() } }
        /**
         * Sets the list of artifacts that can only be traded by villagers.
         * @param value List of Whitelisted Artifacts
         */
        set(value) { "artifacts.trades.craftable-artifacts.whitelisted-artifacts"[configuration, configFile] = value }

    var artifactTradesMinVillagerLevel: Int
        /**
         * Fetches the minimum villager level for artifact trades.
         * @return Minimum Villager Level
         */
        get() = "artifacts.trades.min-villager-level"[configuration, Int::class.java, 1]
        /**
         * Sets the minimum villager level for artifact trades.
         * @param value Minimum Villager Level
         */
        set(value) { "artifacts.trades.min-villager-level"[configuration, configFile] = value }

    var artifactTradesMaxVillagerLevel: Int
        /**
         * Fetches the maximum villager level for artifact trades.
         * @return Maximum Villager Level
         */
        get() = "artifacts.trades.max-villager-level"[configuration, Int::class.java, 5]
        /**
         * Sets the maximum villager level for artifact trades.
         * @param value Maximum Villager Level
         */
        set(value) { "artifacts.trades.max-villager-level"[configuration, configFile] = value }

    var artifactTradesMaxUses: Int
        /**
         * Fetches the maximum uses for artifact trades.
         * @return Maximum Uses
         */
        get() = "artifacts.trades.max-uses"[configuration, Int::class.java, 10]
        /**
         * Sets the maximum uses for artifact trades.
         * @param value Maximum Uses
         */
        set(value) { "artifacts.trades.max-uses"[configuration, configFile] = value }

    var artifactTradesChance: Double
        /**
         * Fetches the chance for any villager trade to be replaced by an artifact trade.
         * @return Trade Chance
         */
        get() = "artifacts.trades.chance"[configuration, Double::class.java, 0.25]
        /**
         * Sets the chance for any villager trade to be replaced by an artifact trade.
         * @param value Trade Chance
         */
        set(value) { "artifacts.trades.chance"[configuration, configFile] = value }

    var artifactTradesCraftableChance: Double
        /**
         * Fetches the chance for any villager trade to be replaced by a crafted artifact trade.
         * @return Trade Chance
         */
        get() = when ("artifacts.trades.craftable-artifacts.chance.global"[configuration, Any::class.java, "default"]) {
            "default" -> artifactTradesChance
            else -> "artifacts.trades.craftable-artifacts.chance.global"[configuration, Double::class.java, artifactTradesChance]
        }
        /**
         * Sets the chance for any villager trade to be replaced by a crafted artifact trade.
         * @param value Trade Chance
         */
        set(value) { "artifacts.trades.craftable-artifacts.chance.global"[configuration, configFile] = value }

    /**
     * Fetches the chance for a specific artifact to be traded by a villager.
     * @param artifact The artifact to fetch
     * @return Trade Chance
     */
    fun getArtifactTradesCraftableChance(artifact: PArtifact) =
        "artifacts.trades.craftable-artifacts.chance".getSection(configuration).getValues(false).toList().firstOrNull { artifact.key.toString() == it.first.lowercase() || artifact.key.key == it.first.lowercase() }?.second?.toString()?.toDoubleOrNull() ?: artifactTradesCraftableChance

    /**
     * Sets the chance for a specific artifact to be traded by a villager.
     * @param artifact The artifact to fetch
     * @param value Trade Chance
     */
    fun setArtifactTradesCraftableChance(artifact: PArtifact, value: Double?) {
        "artifacts.trades.craftable-artifacts.chance.${artifact.key}"[configuration, configFile] = value
    }

    var isArtifactTradesBuyEnabled: Boolean
        /**
         * Fetches whether or not villagers can buy raw artifacts.
         * @return true if enabled, false otherwise
         */
        get() = "artifacts.trades.buy-artifacts.enabled"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not villagers can buy raw artifacts.
         * @param value true if enabled, false otherwise
         */
        set(value) { "artifacts.trades.buy-artifacts.enabled"[configuration, configFile] = value }

    var artifactTradesBuyPrice: Int
        /**
         * Fetches the emerald price for villagers buying raw artifacts.
         * @return Price
         */
        get() = "artifacts.trades.buy-artifacts.price"[configuration, Int::class.java, 7]
        /**
         * Sets the emerald price for villagers buying raw artifacts.
         * @param value Price
         */
        set(value) { "artifacts.trades.buy-artifacts.price"[configuration, configFile] = value }

    var isArtifactTradesIncludeWanderingTrader: Boolean
        /**
         * Fetches whether or not the wandering trader can trade artifacts.
         * @return true if enabled, false otherwise
         */
        get() = "artifacts.trades.include-wandering-trader"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not the wandering trader can trade artifacts.
         * @param value true if enabled, false otherwise
         */
        set(value) { "artifacts.trades.include-wandering-trader"[configuration, configFile] = value }

    var maxAnvilCost: Int
        /**
         * Fetches the maximum anvil cost for merging items with artifacts and items.
         * @return Maximum Anvil Cost
         */
        get() = "max-anvil-cost"[configuration, Int::class.java, 100]
        /**
         * Sets the maximum anvil cost for merging items with artifacts and items.
         * @param value Maximum Anvil Cost
         */
        set(value) { "max-anvil-cost"[configuration, configFile] = value }

    var isEnchantmentBarteringEnabled: Boolean
        /**
         * Fetches whether or not bartering with piglins can yield enchanted books.
         * @return true if enabled, false otherwise
         */
        get() = "enchantments.bartering.enabled"[configuration, Boolean::class.java, true]
        /**
         * Sets whether or not bartering with piglins can yield enchanted books.
         * @param value true if enabled, false otherwise
         */
        set(value) { "enchantments.bartering.enabled"[configuration, configFile] = value }

    var enchantmentBarteringChance: Double
        /**
         * Fetches the chance for a bartering trade to yield an enchanted book.
         * @return Chance
         */
        get() = "enchantments.bartering.chance"[configuration, Double::class.java, 0.05]
        /**
         * Sets the chance for a bartering trade to yield an enchanted book.
         * @param value Chance
         */
        set(value) { "enchantments.bartering.chance"[configuration, configFile] = value }

    var blacklistedBarteringEnchantments: List<PEnchantment>
        /**
         * Fetches the list of enchantments that cannot be received through bartering.
         * @return List of Enchantments
         */
        get() = "enchantments.bartering.blacklisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that cannot be received through bartering.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.bartering.blacklisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var whitelistedBarteringEnchantments: List<PEnchantment>
        /**
         * Fetches the list of enchantments that can only be received through bartering.
         * @return List of Enchantments
         */
        get() = "enchantments.bartering.whitelisted-enchants"[configuration, List::class.java, listOf<String>()].mapNotNull { registry.getEnchantment(it.toString()) }
        /**
         * Sets the list of enchantments that can only be received through bartering.
         * @param value List of Enchantments
         */
        set(value) { "enchantments.bartering.whitelisted-enchants"[configuration, configFile] = value.map { it.key.key } }

    var enchantmentBarteringMinEnchantLevel: Int
        /**
         * Fetches the minimum enchantment level for enchanted books received through bartering.
         * @return Minimum Level
         */
        get() = "enchantments.bartering.min-level"[configuration, Int::class.java, 1]
        /**
         * Sets the minimum enchantment level for enchanted books received through bartering.
         * @param value Minimum Level
         */
        set(value) { "enchantments.bartering.min-level"[configuration, configFile] = value }

    var enchantmentBarteringMaxEnchantLevel: Int
        /**
         * Fetches the maximum enchantment level for enchanted books received through bartering.
         * @return Maximum Level
         */
        get() = "enchantments.bartering.max-level"[configuration, Int::class.java] ?: Int.MAX_VALUE
        /**
         * Sets the maximum enchantment level for enchanted books received through bartering.
         * @param value Maximum Level
         */
        set(value) { "enchantments.bartering.max-level"[configuration, configFile] = value }
}
