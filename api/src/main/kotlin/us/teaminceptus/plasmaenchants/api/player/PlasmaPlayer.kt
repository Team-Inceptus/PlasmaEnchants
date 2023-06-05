package us.teaminceptus.plasmaenchants.api.player

import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import java.io.File

/**
 * Represents a PlasmaEnchants Player.
 * @param p OfflinePlayer to use
 */
class PlasmaPlayer(private val p: OfflinePlayer) {

    private val pConfig: FileConfiguration
    private val pFile: File = PlasmaConfig.getDataFolder().resolve("${p.uniqueId}.yml")

    init {
        if (!pFile.exists()) pFile.createNewFile()

        this.pConfig = pFile.let { YamlConfiguration.loadConfiguration(it) }
    }

    /**
     * Fetches the player that belongs to this PlasmaPlayer.
     * @return OfflinePlayer
     */
    fun getPlayer(): OfflinePlayer {
        return p
    }

    /**
     * Fetches the Player's Configuration.
     * @return Player's Configuration
     */
    fun getConfig(): FileConfiguration {
        return pConfig
    }

    /**
     * Fetches the Player's File that the configuration is stored in.
     * @return Player's File
     */
    fun getFile(): File {
        return pFile
    }

}