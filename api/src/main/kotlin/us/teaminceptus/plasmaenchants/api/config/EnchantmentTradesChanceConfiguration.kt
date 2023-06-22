package us.teaminceptus.plasmaenchants.api.config

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment

/**
 * Represents a configuration for replacing a bukkit enchantment book trade with a PlasmaEnchants enchantment book trade.
 */
class EnchantmentTradesChanceConfiguration{

    constructor(
        bukkit: Enchantment, plasma: PEnchantment,
        minBukkitLevel: Int, maxBukkitLevel: Int,
        minPlasmaLevel: Int, maxPlasmaLevel: Int
    ) {
        this.bukkit = bukkit
        this.plasma = plasma
        this.minBukkitLevel = minBukkitLevel
        this.maxBukkitLevel = maxBukkitLevel
        this.minPlasmaLevel = minPlasmaLevel
        this.maxPlasmaLevel = maxPlasmaLevel
    }

    internal constructor(data: Map<String, Any>) {
        this.bukkit = Enchantment.getByKey(NamespacedKey.minecraft(data["bukkit"].toString().lowercase())) ?: throw IllegalArgumentException("Invalid bukkit enchantment for EnchantmentTradesChanceConfiguration: 'minecraft:${data["bukkit"]}'")
        this.plasma = PlasmaConfig.registry.getEnchantment(data["plasma"].toString()) ?: throw IllegalArgumentException("Invalid PlasmaEnchants enchantment for EnchantmentTradesChanceConfiguration: '${data["plasma"]}'")
        this.minBukkitLevel = data["min-bukkit-level"].toString().toInt()
        this.maxBukkitLevel = data["max-bukkit-level"].toString().toInt()
        this.minPlasmaLevel = data["min-plasma-level"].toString().toInt()
        this.maxPlasmaLevel = data["max-plasma-level"].toString().toInt()
    }

    /**
     * The Bukkit Enchantment to replace.
     * @return Bukkit Enchantment
     */
    val bukkit: Enchantment

    /**
     * The PlasmaEnchants Enchantment to replace it with. The Level will be the same as the Bukkit Enchantment, coerced in [minPlasmaLevel] and [maxPlasmaLevel].
     * @return PlasmaEnchants Enchantment
     */
    val plasma: PEnchantment

    /**
     * The minimum level of the Bukkit Enchantment in order to replace.
     * @return Minimum level
     */
    val minBukkitLevel: Int

    /**
     * The maximum level of the Bukkit Enchantment in order to replace.
     * @return Maximum level
     */
    val maxBukkitLevel: Int

    /**
     * The minimum level of the PlasmaEnchants Enchantment replaced.
     * @return Minimum level
     */
    val minPlasmaLevel: Int

    /**
     * The maximum level of the PlasmaEnchants Enchantment replaced.
     * @return Maximum level
     */
    val maxPlasmaLevel: Int

}