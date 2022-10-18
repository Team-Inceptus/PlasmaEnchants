package us.teaminceptus.plasmaenchants;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
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

    private var config: FileConfiguration? = null

    override fun onEnable() {
        config = getConfig()

        getLogger().info("Done!")
    }

    override fun onDisable() {
        
    }

    // Configuration

    override fun get(key: String): String {
        val p: Properties = Properties();
        val lang: String = if (getLanguage().equals("en", ignoreCase = true)) "" else "_" + getLanguage();

        try {
            val str: InputStream = PlasmaEnchants::class.java.getResourceAsStream("/lang/plasmaenchants" + lang + ".properties")

            p.load(str);
            str.close();
            return ChatColor.translateAlternateColorCodes('&', p.getProperty(key, "Unknown Value"));
        } catch (e: IOException) {
            print(e);
            return "Unknown Value";
        }
    }

    override fun getLanguage(): String? {
        return config?.getString("language", "en")
    }

}