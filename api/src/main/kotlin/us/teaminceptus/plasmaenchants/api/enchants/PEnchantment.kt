package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import us.teaminceptus.plasmaenchants.api.*
import java.util.function.BiConsumer

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : BiConsumer<Event, Int>, Keyed {

    /**
     * Fetches the display name of this PEnchantment.
     * @return Name of Enchantment
     */
    val displayName
        get() = key.key.uppercase().split("_").joinToString(" ") {
                it -> if (it.lowercase() == "of") it.lowercase() else it.lowercase().replaceFirstChar { it.uppercase() }
        }

    /**
     * Fetches the maximum level of this PEnchantment.
     * @return Maximum Level
     */
    val maxLevel: Int
        get() = 1

    /**
     * Fetches the Target Type of this PEnchantment.
     * @return Target Type
     */
    val target: PTarget

    /**
     * Fetches the Type of this PEnchantment.
     * @return Type
     */
    val type: PType<*>

    /**
     * Fetches the list of conflicts for this PEnchantment.
     * @return List of Conflicts
     */
    val conflicts: List<PEnchantment>

    /**
     * Whether or not this PEnchantment conflicts with another PEnchantment. This will return false if [PlasmaConfig.isIgnoreEnchantmentConflictRestriction] is true.
     * @param enchantment PEnchantment to check for conflicts with.
     * @return true if conflicts with enchantment, false otherwise
     */
    fun conflictsWith(enchantment: PEnchantment?): Boolean {
        if (enchantment == null) return false
        if (PlasmaConfig.config.isIgnoreEnchantmentConflictRestriction) return false

        return conflicts.contains(enchantment) || enchantment.conflicts.contains(this)
    }

    /**
     * Converts this PEnchantment to a string as shown in the lore.
     * @param level Level of Enchantment
     * @return String Representation of Enchantment
     */
    fun toString(level: Int): String {
        var str = "${type.color}$displayName"
        if (maxLevel != 1)
            str += " ${level.toRoman()}"

        return str
    }

    /**
     * Generates an [Material.ENCHANTED_BOOK] book for this PEnchantment.
     * @param level Level of Enchantment
     * @return Enchanted Book
     */
    fun generateBook(level: Int): ItemStack = ItemStack(Material.ENCHANTED_BOOK).apply {
        itemMeta = itemMeta!!.apply {
            addEnchant(this@PEnchantment, level.coerceAtMost(if (PlasmaConfig.config.isIgnoreEnchantmentLevelRestriction) Integer.MAX_VALUE else maxLevel))
        }
    }

    val isDisabled: Boolean
        /**
         * Whether or not this PEnchantment is disabled.
         * @return true if disabled, false otherwise
         */
        get() = PlasmaConfig.config.disabledEnchantments.contains(this)

    val isSpawnBlacklisted: Boolean
        /**
         * Whether or not this PEnchantment is blacklisted from spawning.
         * @return true if blacklisted, false otherwise
         */
        get() {
            if (PlasmaConfig.config.whitelistedSpawnEnchantments.isNotEmpty())
                return !PlasmaConfig.config.whitelistedSpawnEnchantments.contains(this)

            return PlasmaConfig.config.blacklistedSpawnEnchantments.contains(this)
        }

}