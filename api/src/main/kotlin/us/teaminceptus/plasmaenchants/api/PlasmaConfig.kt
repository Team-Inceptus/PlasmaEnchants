package us.teaminceptus.plasmaenchants.api;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.logging.Logger

/**
 * Represents the main PlasmaEnchants Configuration
 */
interface PlasmaConfig {

    companion object Config {
        private val p: Plugin? = if (getPlugin() == null) null else getPlugin()

        @JvmStatic
        fun getPlugin(): Plugin? {
            return Bukkit.getPluginManager().getPlugin("PlasmaEnchants")
        }

        @JvmStatic
        fun getLogger(): Logger? {
            if (p == null) return null;
            return p.getLogger();
        }

        @JvmStatic
        fun getConfig(): PlasmaConfig? {
            if (p == null) return null
            if (p is PlasmaConfig) return p

            return null
        }

        @JvmStatic
        fun print(e: Throwable) {
            val logger: Logger? = getLogger();
            if (logger == null) return;

            logger.severe(e.javaClass.getSimpleName());
            logger.severe("--------------");
            logger.severe(e.message);
            for (stack: StackTraceElement in e.stackTrace) logger.severe(stack.toString());
        }
    }

    
    fun get(key: String): String

    fun getMessage(key: String): String {
        return get("plugin.prefix") + get("plugin.prefix")
    }

    fun getLanguage(): String?

}