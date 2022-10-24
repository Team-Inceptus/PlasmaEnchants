package us.teaminceptus.plasmaenchants.api

import org.bukkit.plugin.Plugin
import org.bukkit.Bukkit
import java.io.File
import java.util.logging.Logger

/**
 * Represents the main PlasmaEnchants Configuration
 */
interface PlasmaConfig {

    companion object {
        private val p: Plugin? = if (getPlugin() == null) null else getPlugin()

        /**
         * Fetches the PlasmaEnchants plugin.
         * @return Plugin
         */
        @JvmStatic
        fun getPlugin(): Plugin? {
            return Bukkit.getPluginManager().getPlugin("PlasmaEnchants")
        }

        /**
         * Fetches the PlasmaEnchant plugin's logger.
         * @return Plugin's Logger
         */
        @JvmStatic
        fun getLogger(): Logger? {
            if (p == null) return null
            return p.logger
        }

        /**
         * Fetches the PlasmaConfig instance.
         * @return PlasmaConfig Instance
         */
        @JvmStatic
        fun getConfig(): PlasmaConfig? {
            if (p == null) return null
            if (p is PlasmaConfig) return p

            return null
        }

        /**
         * Prints a throwable in the plugin namespace.
         */
        @JvmStatic
        fun print(e: Throwable) {
            val logger: Logger = getLogger() ?: return

            logger.severe(e.javaClass.simpleName)
            logger.severe("--------------")
            logger.severe(e.message)
            for (stack: StackTraceElement in e.stackTrace) logger.severe(stack.toString())
        }

        /**
         * Fetches the Plugin's Data Folder.
         * @return Data Folder
         */
        fun getDataFolder(): File? {
            return p?.dataFolder
        }

        /**
         * Fetches the Player Data Directory.
         * @return Player Data Directory
         */
        @JvmStatic
        fun getPlayerDirectory(): File? {
            return getDataFolder()?.resolve("players")
        }
    }

    /**
     * Fetches a message from the Language file.
     * @param key The key to fetch
     * @return The message
     */
    fun get(key: String): String

    /**
     * Fetches a message from the Language file, with the plugin prefix in front.
     * @param key The key to fetch
     * @return The message
     */
    fun getMessage(key: String): String {
        return get("plugin.prefix") + get("plugin.prefix")
    }

    /**
     * Fetches the language set in the configuration.
     * @return Language Configured
     */
    fun getLanguage(): String?

}