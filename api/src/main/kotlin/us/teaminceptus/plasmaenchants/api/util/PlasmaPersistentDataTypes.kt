@file:Suppress("unchecked_cast", "deprecation")

package us.teaminceptus.plasmaenchants.api.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.*

abstract class PlasmaPersistentDataTypes<T, Z> internal constructor (

    private val primitive: Class<T>,
    private val complex: Class<Z>) : PersistentDataType<T, Z> {

    override fun getPrimitiveType(): Class<T> = primitive
    override fun getComplexType(): Class<Z> = complex

}

/**
 * A [PersistentDataType] for storing [Map]s of [NamespacedKey]s to [Int]s.
 */
val NAMESPACEDKEY_INT_MAP: PlasmaPersistentDataTypes<ByteArray, Map<NamespacedKey, Int>> = object : PlasmaPersistentDataTypes<ByteArray, Map<NamespacedKey, Int>>(
    ByteArray::class.java,
    Map::class.java as Class<Map<NamespacedKey, Int>>
) {
    override fun toPrimitive(complex: Map<NamespacedKey, Int>, context: PersistentDataAdapterContext): ByteArray {
        val os = ByteArrayOutputStream()
        val oos = ObjectOutputStream(BufferedOutputStream(os))
        oos.writeObject(complex.mapKeys { it.key.toString() })
        oos.close()

        return os.toByteArray()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Map<NamespacedKey, Int> {
        val input = primitive.inputStream()
        val ois = ObjectInputStream(BufferedInputStream(input))
        val map = ois.readObject() as Map<String, Int>
        ois.close()

        return map.mapKeys { NamespacedKey(it.key.split(":")[0], it.key.split(":")[1]) }
    }
}

/**
 * A [PersistentDataType] for storing [NamespacedKey]s.
 */
val NAMESPACED_KEY: PlasmaPersistentDataTypes<ByteArray, NamespacedKey> = object : PlasmaPersistentDataTypes<ByteArray, NamespacedKey>(
    ByteArray::class.java,
    NamespacedKey::class.java
) {

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): ByteArray = complex.toString().toByteArray()
    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): NamespacedKey {
        val str = String(primitive)
        val split = str.split(":")
        return NamespacedKey(split[0], split[1])
    }
}