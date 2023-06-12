package us.teaminceptus.plasmaenchants.api

import com.google.common.collect.ImmutableMap
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import java.io.*
import java.util.*
import kotlin.collections.HashMap

private val enchantKey = NamespacedKey(PlasmaConfig.getPlugin(), "enchants")
private val artifactsKey = NamespacedKey(PlasmaConfig.getPlugin(), "artifacts")

@Suppress("unchecked_cast")
private val stringIntMap: PersistentDataType<ByteArray, Map<String, Int>> = object : PersistentDataType<ByteArray, Map<String, Int>> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<Map<String, Int>> {
        return Map::class.java as Class<Map<String, Int>>
    }

    override fun toPrimitive(complex: Map<String, Int>, context: PersistentDataAdapterContext): ByteArray {
        val os = ByteArrayOutputStream()
        val oos = ObjectOutputStream(BufferedOutputStream(os))
        oos.writeObject(complex)
        oos.close()

        return os.toByteArray()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Map<String, Int> {
        val input = primitive.inputStream()
        val ois = ObjectInputStream(BufferedInputStream(input))
        val map = ois.readObject() as Map<String, Int>
        ois.close()

        return map
    }

}

// Enchantments

fun ItemMeta.getPlasmaEnchants(): Map<PEnchantment, Int> {
    val map = mutableMapOf<PEnchantment, Int>()
    persistentDataContainer[enchantKey, stringIntMap]?.forEach { (key, value) ->
        val enchant = PlasmaConfig.getRegistry().getEnchantments().firstOrNull { it.key.key == key } ?: return@forEach
        map[enchant] = value
    }

    return ImmutableMap.copyOf(map)
}
fun ItemMeta.hasEnchant(enchant: PEnchantment): Boolean = getPlasmaEnchants().containsKey(enchant)
fun ItemMeta.hasPlasmaEnchants(): Boolean = getPlasmaEnchants().isNotEmpty()
fun ItemMeta.clearPlasmaEnchants() { persistentDataContainer[enchantKey, stringIntMap] = mutableMapOf() }
fun ItemMeta.getEnchantLevel(enchant: PEnchantment): Int = persistentDataContainer[enchantKey, stringIntMap]!![enchant.key.key] ?: 0
fun ItemMeta.hasConflictingEnchant(enchant: PEnchantment): Boolean = getPlasmaEnchants().filterKeys { it.conflictsWith(enchant) }.isNotEmpty()
fun ItemMeta.addEnchant(enchant: PEnchantment, level: Int) {
    val map = HashMap(getPlasmaEnchants().map { it.key.key.key to it.value }.toMap())
    map[enchant.key.key] = level

    persistentDataContainer[enchantKey, stringIntMap] = map

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.add(enchant.toString(level))
    lore = nLore
}
fun ItemMeta.removeEnchant(enchant: PEnchantment) {
    val map = HashMap(getPlasmaEnchants().map { it.key.key.key to it.value }.toMap())
    map.remove(enchant.key.key)

    persistentDataContainer[enchantKey, stringIntMap] = map

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.removeIf { it.contains(enchant.displayName) }
    lore = nLore
}

// Artifacts
fun ItemMeta.getArtifact(): PArtifact? {
    return PlasmaConfig.getRegistry().getArtifact(
        NamespacedKey(
            PlasmaConfig.getPlugin(),
            persistentDataContainer[artifactsKey, PersistentDataType.STRING] ?: return null
        )
    )
}
fun ItemMeta.hasArtifact(): Boolean = persistentDataContainer.has(artifactsKey, PersistentDataType.STRING)
fun ItemMeta.setArtifact(artifact: PArtifact) {
    persistentDataContainer.set(artifactsKey, PersistentDataType.STRING, artifact.key.key)

    val nLore = mutableListOf<String>()
    nLore.addAll(lore ?: mutableListOf())
    nLore.add(0, artifact.asString())
    lore = nLore
}
fun ItemMeta.removeArtifact() {
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