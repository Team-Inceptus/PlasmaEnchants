package us.teaminceptus.plasmaenchants.api

import com.google.common.collect.ImmutableMap
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.util.NAMESPACED_KEY
import us.teaminceptus.plasmaenchants.api.util.NAMESPACEDKEY_INT_MAP
import java.util.*

/**
 * The NamespacedKey in the PDC in [ItemMeta] for storing [PEnchantment]s.
 */
val enchantKey
    get() = NamespacedKey(PlasmaConfig.plugin, "enchants")

/**
 * The NamespacedKey in the PDC in [ItemMeta] for storing an item's [PArtifact].
 */
val artifactsKey
    get() = NamespacedKey(PlasmaConfig.plugin, "artifacts")

/**
 * The NamespacedKey in the PDC in [ItemMeta] for identifying a [PArtifact.item].
 */
val artifactItemKey
    get() = NamespacedKey(PlasmaConfig.plugin, "artifact-item")

// Enchantments

inline val ItemMeta.plasmaEnchants: Map<PEnchantment, Int>
    /**
     * Fetches the [PEnchantment]s on this item.
     * @return Map of [PEnchantment]s to their levels
     */
    get() {
        val map = mutableMapOf<PEnchantment, Int>()
        persistentDataContainer[enchantKey, NAMESPACEDKEY_INT_MAP]?.forEach { (key, value) ->
            val enchant = PlasmaConfig.registry.enchantments.firstOrNull { it.key == key } ?: return@forEach
            map[enchant] = value
        }

        return ImmutableMap.copyOf(map)
    }

/**
 * Whether or not this item has a [PEnchantment].
 * @param enchant [PEnchantment] to check for
 * @return true if item has enchantment, false otherwise
 */
fun ItemMeta.hasEnchant(enchant: PEnchantment): Boolean = plasmaEnchants.containsKey(enchant)

/**
 * Whether or not this item has any PEnchantments.
 * @return true if item has any Plasma Enchantments, false otherwise
 */
fun ItemMeta.hasPlasmaEnchants(): Boolean = plasmaEnchants.isNotEmpty()

/**
 * Clears all PEnchantments from this item.
 */
fun ItemMeta.clearPlasmaEnchants() = plasmaEnchants.keys.forEach(::removeEnchant)

/**
 * Fetches the level of a [PEnchantment] on this item.
 * @param enchant [PEnchantment] to check for
 * @return Level of Enchantment
 */
fun ItemMeta.getEnchantLevel(enchant: PEnchantment): Int = persistentDataContainer[enchantKey, NAMESPACEDKEY_INT_MAP]!![enchant.key] ?: 0

/**
 * Whether or not this item has a conflicting [PEnchantment].
 * @param enchant [PEnchantment] to check for
 * @return true if item has conflicting enchantment, false otherwise
 */
fun ItemMeta.hasConflictingEnchant(enchant: PEnchantment): Boolean = plasmaEnchants.filterKeys { it.conflictsWith(enchant) }.isNotEmpty()

/**
 * Adds a [PEnchantment] to this item.
 * @param enchant [PEnchantment] to add
 * @param level Level of Enchantment
 * @throws IllegalArgumentException if item already has enchantment or conflicting enchantment
 */
@Throws(IllegalArgumentException::class)
fun ItemMeta.addEnchant(enchant: PEnchantment, level: Int, ignoreLevelRestriction: Boolean = false) {
    if (hasEnchant(enchant)) removeEnchant(enchant)
    if (hasConflictingEnchant(enchant)) throw IllegalArgumentException("Item has conflicting enchantment with ${enchant.key}")
    
    val level0 = if (ignoreLevelRestriction) level.coerceIn(1, enchant.maxLevel) else level.coerceAtLeast(1)
    
    val map = HashMap(plasmaEnchants.map { it.key.key to it.value }.toMap())
    map[enchant.key] = level0

    persistentDataContainer[enchantKey, NAMESPACEDKEY_INT_MAP] = map

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.add(enchant.toString(level0))
    lore = nLore
}

/**
 * Removes a [PEnchantment] from this item. This will silently fail if the item does not have the enchantment.
 * @param enchant [PEnchantment] to remove
 */
fun ItemMeta.removeEnchant(enchant: PEnchantment) {
    val map = HashMap(plasmaEnchants.map { it.key.key to it.value }.toMap())
    map.remove(enchant.key)

    val amount = getEnchantLevel(enchant)

    persistentDataContainer[enchantKey, NAMESPACEDKEY_INT_MAP] = map

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.remove(enchant.toString(amount))
    lore = nLore
}

/**
 * Combines the [PEnchantment]s on this item with another [ItemMeta]. This will not check [PTarget.isValid].
 * @param other [ItemMeta] to combine with
 * @throws IllegalArgumentException if other meta has conflicting enchantments
 */
@Throws(IllegalArgumentException::class)
fun ItemMeta.combinePlasmaEnchants(other: ItemMeta, ignoreLevelRestriction: Boolean = false) {
    val others = other.plasmaEnchants
    if (others.isEmpty()) return
    if (plasmaEnchants.isEmpty() && others.isEmpty()) return
    if (others.any { hasConflictingEnchant(it.key) }) throw IllegalArgumentException("Item has conflicting enchantments while merging: ${others.keys.first { hasConflictingEnchant(it) }.key} }}")

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())

    val map = HashMap(plasmaEnchants.map { it.key.key to it.value }.toMap())

    others.forEach { (enchant, level) ->
        val oldLevel = map[enchant.key] ?: 0
        val newLevel = (when {
            oldLevel < level -> level
            oldLevel == level -> level + 1
            else -> oldLevel
        }).coerceAtMost(if (ignoreLevelRestriction) Integer.MAX_VALUE else enchant.maxLevel)

        map[enchant.key] = newLevel

        if (oldLevel != 0) nLore.remove(enchant.toString(oldLevel))
        nLore.add(enchant.toString(newLevel))
    }

    if (map == plasmaEnchants.map { it.key.key to it.value }.toMap()) return

    persistentDataContainer[enchantKey, NAMESPACEDKEY_INT_MAP] = map
    lore = nLore
}

// Artifacts
inline var ItemMeta.artifact: PArtifact?
    /**
     * Gets the [PArtifact] on this item.
     * @return [PArtifact] on this item, or null if none
     */
    get() {
        return PlasmaConfig.registry.getArtifact(persistentDataContainer[artifactsKey, NAMESPACED_KEY] ?: return null)
    }
    /**
     * Sets the [PArtifact] on this item.
     * @param value [PArtifact] on this item, null to remove
     */
    set(value) {
        if (value == null) return removeArtifact()
        val previous = artifact != null

        persistentDataContainer[artifactsKey, NAMESPACED_KEY] = value.key

        val nLore = mutableListOf<String>()
        nLore.addAll(lore ?: mutableListOf())

        if (previous)
            nLore[0] = value.asString()
        else
            nLore.add(0, value.asString())
        lore = nLore
    }

/**
 * Whether or not this item is an [PArtifact] item that can be combined with its target tool to give it its artifact.
 * This will return true on [PArtifact.item] items.
 * @return true if item is artifact item, false otherwise
 */
inline val ItemMeta.isArtifactItem: Boolean
    get() = artifact != null && persistentDataContainer[artifactItemKey, PersistentDataType.BYTE] == 1.toByte()

/**
 * Whether or not this item has an [PArtifact].
 * @return true if item has artifact, false otherwise
 */
fun ItemMeta.hasArtifact(): Boolean = persistentDataContainer.has(artifactsKey, NAMESPACED_KEY)

/**
 * Removes the [PArtifact] from this item. This will silently fail if the item does not have an artifact.
 */
fun ItemMeta.removeArtifact() {
    if (artifact == null) return
    persistentDataContainer.remove(artifactsKey)

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.removeAt(0)
    lore = nLore
}

// Kotlin Util

private val ROMAN_NUMERALS = TreeMap<Long, String>().apply {
    putAll(mutableMapOf(
        1000L to "M",
        900L to "CM",
        500L to "D",
        400L to "CD",
        100L to "C",
        90L to "XC",
        50L to "L",
        40L to "XL",
        10L to "X",
        9L to "IX",
        5L to "V",
        4L to "IV",
        1L to "I"
    ))
}

fun Number.toRoman(): String {
    val number = toLong()
    val l: Long = ROMAN_NUMERALS.floorKey(number)
    return if (number == l) ROMAN_NUMERALS[number]!! else ROMAN_NUMERALS[l] + (number - l).toRoman()
}