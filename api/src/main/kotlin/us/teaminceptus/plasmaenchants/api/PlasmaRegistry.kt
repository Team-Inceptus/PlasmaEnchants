package us.teaminceptus.plasmaenchants.api

import org.bukkit.NamespacedKey
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment

interface PlasmaRegistry {

    /**
     * Registers a PEnchantment to the registry.
     * @param enchantment PEnchantment to register
     * @throws IllegalArgumentException if the PEnchantment is already registered
     */
    @Throws(IllegalArgumentException::class)
    fun register(enchantment: PEnchantment)

    /**
     * Fetches a PEnchantment from the registry.
     * @param key NamespacedKey of PEnchantment
     */
    fun getEnchantment(key: NamespacedKey): PEnchantment? {
        return getEnchantments().firstOrNull { it.key == key }
    }

    /**
     * Fetches all of the registered PEnchantments.
     * @return Immutable Set of PEnchantments
     */
    fun getEnchantments(): Set<PEnchantment>

    /**
     * Checks if a PEnchantment is registered.
     * @param enchantment PEnchantment to check
     */
    fun isRegistered(enchantment: PEnchantment): Boolean {
        return getEnchantments().contains(enchantment)
    }

    /**
     * Unregisters a PEnchantment from the registry.
     * @param enchantment PEnchantment to unregister
     * @throws IllegalArgumentException if the PEnchantment is not registered
     */
    @Throws(IllegalArgumentException::class)
    fun unregister(enchantment: PEnchantment)

    /**
     * Fetches all of the registered PlasmaEnchants Artifacts.
     * @return Immutable Set of PArtifacts
     */
    fun getArtifacts(): Set<PArtifact>

    /**
     * Fetches a PArtifact from the registry.
     * @param key NamespacedKey of PArtifact
     * @return [PArtifact] with key, or null if not found
     */
    fun getArtifact(key: NamespacedKey): PArtifact? {
        return getArtifacts().firstOrNull { it.key == key }
    }

    /**
     * Checks if a PArtifact is registered.
     * @param artifact [PArtifact] to check
     * @return true if registered, false if not
     */
    fun isRegistered(artifact: PArtifact): Boolean {
        return getArtifacts().contains(artifact)
    }

    /**
     * Registers a PArtifact to the registry.
     * @param artifact [PArtifact] to register
     * @throws IllegalArgumentException if the PArtifact is already registered
     */
    @Throws(IllegalArgumentException::class)
    fun register(artifact: PArtifact)

    /**
     * Unregisters a PArtifact from the registry.
     * @param artifact [PArtifact] to unregister
     * @throws IllegalArgumentException if the PArtifact is not registered
     */
    @Throws(IllegalArgumentException::class)
    fun unregister(artifact: PArtifact)

}