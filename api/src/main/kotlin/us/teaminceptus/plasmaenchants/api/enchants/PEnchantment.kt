package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Keyed
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.toRoman
import java.util.function.BiConsumer

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : BiConsumer<Event, Int>, Keyed {

    /**
     * Fetches the display name of this PEnchantment.
     * @return Name of Enchantment
     */
    val displayName: String

    /**
     * Fetches the description of this PEnchantment.
     * @return Description of Enchantment
     */
    val description: String

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
     * Whether or not this PEnchantment conflicts with another PEnchantment.
     * @param enchantment PEnchantment to check for conflicts with.
     * @return true if conflicts with enchantment, false otherwise
     */
    fun conflictsWith(enchantment: PEnchantment?): Boolean {
        if (enchantment == null) return false
        return conflicts.contains(enchantment) || enchantment.conflicts.contains(this)
    }

    /**
     * Converts this PEnchantment to a string as shown in the lore.
     * @param level Level of Enchantment
     * @return String Representation of Enchantment
     */
    fun toString(level: Int): String = "${type.color}$displayName ${level.toRoman()}"

}