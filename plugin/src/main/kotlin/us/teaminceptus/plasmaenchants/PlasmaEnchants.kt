package us.teaminceptus.plasmaenchants

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.ChatColor
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.PlasmaEnchants
import java.util.Properties
import java.io.InputStream
import java.io.IOException

/**
 * Represents the main PlasmaEnchants Plugin
 */
class PlasmaEnchants : JavaPlugin(), PlasmaConfig {

    private fun loadClasses() {
        PlasmaEvents(this)
        PlasmaCommands(this)
    }

    override fun onEnable() {
        saveDefaultConfig()

        loadClasses()
        logger.info("Loaded Classes...")

        logger.info("Done!")
    }

    override fun onDisable() {
        
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

}