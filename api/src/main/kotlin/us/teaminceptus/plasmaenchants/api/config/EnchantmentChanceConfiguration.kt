package us.teaminceptus.plasmaenchants.api.config

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.loot.LootTables
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment

class EnchantmentChanceConfiguration<T : Enum<T>> {

    /**
     * Whether this enchantment is whitelisted (if not empty) or blacklisted (if not empty).
     * @return true if whitelisted, false if blacklisted
     */
    fun isAllowed(enchantment: PEnchantment): Boolean {
        if (whitelistedEnchants.isNotEmpty())
            return whitelistedEnchants.contains(enchantment)

        return !blacklistedEnchants.contains(enchantment)
    }

    constructor(
        type: T, chance: Double, minLevel: Int, maxLevel: Int,
        whitelistedEnchants: List<PEnchantment>, blacklistedEnchants: List<PEnchantment>
    ) {
        this.typeClass = type::class.java
        this.type = type
        this.chance = chance
        this.minLevel = minLevel
        this.maxLevel = maxLevel
        this.whitelistedEnchants = whitelistedEnchants
        this.blacklistedEnchants = blacklistedEnchants
    }

    @Suppress("unchecked_cast")
    internal constructor(data: Map<String, Any>, minLevel: Int, maxLevel: Int) {
        this.type = when {
            data["mob"] != null -> EntityType.valueOf(data["mob"].toString().uppercase()) as T
            data["table"] != null -> LootTables.valueOf(data["table"].toString().uppercase()) as T
            data["block"] != null -> Material.valueOf(data["block"].toString().uppercase()) as T

            else -> throw IllegalArgumentException("Invalid first for EnchantmentChanceConfiguration: $data")
        }
        this.typeClass = type::class.java

        this.chance = data["chance"].toString().toDouble()
        this.minLevel = when (data["min-level"].toString()) {
            "default" -> minLevel
            else -> data["min-level"].toString().toInt()
        }
        this.maxLevel = when (data["max-level"].toString()) {
            "default" -> maxLevel
            else -> data["max-level"].toString().toInt()
        }

        this.whitelistedEnchants = (data["whitelisted-enchants"] as List<String>).mapNotNull {
            PlasmaConfig.registry.getEnchantment(it)
        }

        this.blacklistedEnchants = (data["blacklisted-enchants"] as List<String>).mapNotNull {
            PlasmaConfig.registry.getEnchantment(it)
        }
    }

    /**
     * Fetches the type class of this EnchantmentChanceConfiguration.
     * @return Type Class
     */
    val typeClass: Class<out T>

    /**
     * Fetches the inputted type of this EnchantmentChanceConfiguration.
     * @return Input Type
     */
    val type: T

    /**
     * Fetches the chance of this EnchantmentChanceConfiguration.
     * @return Spawn Chance
     */
    val chance: Double

    /**
     * Fetches the minimum level of this EnchantmentChanceConfiguration.
     * @return Minimum Level for Spawned Enchantment
     */
    val minLevel: Int

    /**
     * Fetches the maximum level of this EnchantmentChanceConfiguration.
     * @return Maximum Level for Spawned Enchantment
     */
    val maxLevel: Int

    /**
     * Fetches the list of whitelisted enchants for this EnchantmentChanceConfiguration.
     * @return Whitelisted Enchants
     */
    val whitelistedEnchants: List<PEnchantment>

    /**
     * Fetches the list of blacklisted enchants for this EnchantmentChanceConfiguration.
     * @return Blacklisted Enchants
     */
    val blacklistedEnchants: List<PEnchantment>

}