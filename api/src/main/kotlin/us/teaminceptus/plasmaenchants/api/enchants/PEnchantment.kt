package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Keyed
import org.bukkit.event.Event
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PType
import java.util.function.BiConsumer

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : BiConsumer<Event, Int>, Keyed {

    /**
     * Fetches the name of this PEnchantment.
     * @return Name of Enchantment
     */
    fun getName(): String

    /**
     * Fetches the description of this PEnchantment.
     * @return Description of Enchantment
     */
    fun getDescription(): String

    /**
     * Fetches the maximum level of this PEnchantment.
     * @return Maximum Level
     */
    fun getMaxLevel(): Int = 1

    /**
     * Fetches the Target Type of this PEnchantment.
     * @return Target Type
     */
    fun getTarget(): PTarget

    /**
     * Fetches the Type of this PEnchantment.
     * @return Type
     */
    fun getType(): PType<*>

    /**
     * Fetches the list of conflicts for this PEnchantment.
     * @return List of Conflicts
     */
    fun getConflicts(): List<PEnchantment>

    /**
     * Whether or not this PEnchantment conflicts with another PEnchantment.
     * @param enchantment PEnchantment to check for conflicts with.
     * @return true if conflicts with enchantment, false otherwise
     */
    fun conflictsWith(enchantment: PEnchantment?): Boolean {
        if (enchantment == null) return false
        return getConflicts().contains(enchantment) || enchantment.getConflicts().contains(this)
    }

}