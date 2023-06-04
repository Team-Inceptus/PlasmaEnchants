package us.teaminceptus.plasmaenchants.api

import com.google.common.collect.ImmutableMap
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.io.*

private val key = NamespacedKey(PlasmaConfig.getPlugin(), "enchants")

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

fun ItemMeta.getPlasmaEnchants(): Map<PEnchantment, Int> {
    val map = mutableMapOf<PEnchantment, Int>()
    persistentDataContainer[key, stringIntMap]?.forEach { (key, value) ->
        val enchant = PEnchantments.values().firstOrNull { it.key.key == key } ?: return@forEach
        map[enchant] = value
    }

    return ImmutableMap.copyOf(map)
}

fun ItemMeta.hasEnchant(enchant: PEnchantment): Boolean = getPlasmaEnchants().containsKey(enchant)
fun ItemMeta.getEnchantLevel(enchant: PEnchantment): Int = persistentDataContainer[key, stringIntMap]!![enchant.key.key] ?: 0
fun ItemMeta.hasConflictingEnchant(enchant: PEnchantment): Boolean = hasEnchant(enchant) && getPlasmaEnchants().filterKeys { it.conflictsWith(enchant) }.isNotEmpty()

fun ItemMeta.addEnchant(enchant: PEnchantment, level: Int) {
    val map = HashMap(getPlasmaEnchants().map { it.key.key.key to it.value }.toMap())
    map[enchant.key.key] = level

    persistentDataContainer[key, stringIntMap] = map
}

fun ItemMeta.removeEnchant(enchant: PEnchantment) {
    val map = HashMap(getPlasmaEnchants().map { it.key.key.key to it.value }.toMap())
    map.remove(enchant.key.key)

    persistentDataContainer[key, stringIntMap] = map
}