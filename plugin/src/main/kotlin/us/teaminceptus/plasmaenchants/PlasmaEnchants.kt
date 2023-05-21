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

    override fun onEnable() {
        saveDefaultConfig()

        logger.info("Done!")
    }

    override fun onDisable() {
        
    }

    // Configuration

    override fun get(key: String): String {
        val p = Properties()
        val lang: String = if (getLanguage().equals("en", ignoreCase = true)) "" else "_" + getLanguage()

        return try {
            val str: InputStream = PlasmaEnchants::class.java.getResourceAsStream("/lang/plasmaenchants$lang.properties") as InputStream

            p.load(str)
            str.close()
            ChatColor.translateAlternateColorCodes('&', p.getProperty(key, "Unknown Value"))
        } catch (e: IOException) {
            print(e)
            "Unknown Value"
        }
    }

    override fun getLanguage(): String? {
        return config.getString("language", "en")
    }

}