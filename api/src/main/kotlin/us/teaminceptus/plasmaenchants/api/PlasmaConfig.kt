package us.teaminceptus.plasmaenchants.api

import org.bukkit.plugin.Plugin
import org.bukkit.Bukkit
import java.io.File
import java.util.Locale
import java.util.logging.Logger

/**
 * Represents the main PlasmaEnchants Configuration
 */
interface PlasmaConfig {

    companion object {
        /**
         * Fetches the PlasmaEnchants plugin.
         * @return Plugin
         */
        @JvmStatic
        fun getPlugin(): Plugin {
            return Bukkit.getPluginManager().getPlugin("PlasmaEnchants") ?: throw IllegalStateException("PlasmaEnchants is not loaded!")
        }

        /**
         * Fetches the PlasmaEnchant plugin's logger.
         * @return Plugin's Logger
         */
        @JvmStatic
        fun getLogger(): Logger? {
            return getPlugin().logger
        }

        /**
         * Fetches the PlasmaConfig instance.
         * @return PlasmaConfig Instance
         */
        @JvmStatic
        fun getConfig(): PlasmaConfig {
            return getPlugin() as PlasmaConfig
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
        @JvmStatic
        fun getDataFolder(): File {
            return getPlugin().dataFolder
        }

        /**
         * Fetches the Player Data Directory.
         * @return Player Data Directory
         */
        @JvmStatic
        fun getPlayerDirectory(): File {
            return getDataFolder().resolve("players")
        }

        /**
         * Fetches the PlasmaRegistry instance.
         * @return PlasmaRegistry Instance
         */
        @JvmStatic
        fun getRegistry(): PlasmaRegistry {
            return getPlugin() as PlasmaRegistry
        }
    }

    /**
     * Fetches a message from the Language file.
     * @param key The key to fetch
     * @return The message
     */
    fun get(key: String): String?

    /**
     * Fetches a message from the Language file, with the plugin prefix in front.
     * @param key The key to fetch
     * @return The message
     */
    fun getMessage(key: String): String? {
        if (get(key) == null) return null
        return get("plugin.prefix") + get(key)
    }

    /**
     * Fetches the language set in the configuration.
     * @return Language Configured
     */
    fun getLanguage(): String?

    /**
     * Fetches the locale set in the configuration.
     * @return Locale Configured
     */
    fun getLocale(): Locale {
        return when (getLanguage()) {
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "zh" -> Locale.CHINESE
            else -> Locale.ENGLISH
        }
    }

}