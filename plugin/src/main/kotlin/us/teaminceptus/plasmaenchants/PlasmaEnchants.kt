package us.teaminceptus.plasmaenchants

import com.google.common.collect.ImmutableSet
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.ChatColor
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.PlasmaRegistry
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifacts
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.util.Properties
import java.io.InputStream
import java.io.IOException

/**
 * Represents the main PlasmaEnchants Plugin
 */
class PlasmaEnchants : JavaPlugin(), PlasmaConfig, PlasmaRegistry {

    companion object {
        @JvmStatic
        private val enchantments = mutableSetOf<PEnchantment>()

        @JvmStatic
        private val artifacts = mutableSetOf<PArtifact>()
    }

    private fun loadClasses() {
        PlasmaEvents(this)
        PlasmaCommands(this)

        PEnchantments.values().forEach(::register)
        PArtifacts.values().forEach(::register)
    }

    override fun onEnable() {
        saveDefaultConfig()

        loadClasses()
        logger.info("Loaded Classes...")

        logger.info("Done!")
    }

    override fun onDisable() {
        artifacts.clear()
        enchantments.clear()
        logger.info("Unloaded Classes...")

        logger.info("Done!")
    }

    // Configuration

    override fun get(key: String): String? {
        val p = Properties()
        val lang: String = if (getLanguage().equals("en", ignoreCase = true)) "" else "_" + getLanguage()

        return try {
            val str: InputStream = PlasmaEnchants::class.java.getResourceAsStream("/lang/plasmaenchants$lang.properties") as InputStream

            p.load(str)
            str.close()

            val prop = p.getProperty("key", "null")
            if (prop.equals("null")) return null

            ChatColor.translateAlternateColorCodes('&', p.getProperty(key))
        } catch (e: IOException) {
            print(e)
            null
        }
    }

    override fun getLanguage(): String? {
        return config.getString("language", "en")
    }

    // Registry Implementation

    override fun register(enchantment: PEnchantment) {
        if (enchantments.contains(enchantment)) throw IllegalArgumentException("Enchantment already registered!")
        enchantments.add(enchantment)
    }

    override fun register(artifact: PArtifact) {
        if (artifacts.contains(artifact)) throw IllegalArgumentException("Artifact already registered!")
        artifacts.add(artifact)
    }

    override fun getEnchantments(): Set<PEnchantment> {
        return ImmutableSet.copyOf(enchantments)
    }

    override fun unregister(enchantment: PEnchantment) {
        if (!enchantments.contains(enchantment)) throw IllegalArgumentException("Enchantment not registered!")
        enchantments.remove(enchantment)
    }

    override fun unregister(artifact: PArtifact) {
        if (!artifacts.contains(artifact)) throw IllegalArgumentException("Artifact not registered!")
        artifacts.remove(artifact)
    }

    override fun getArtifacts(): Set<PArtifact> {
        return ImmutableSet.copyOf(artifacts)
    }


}