package us.teaminceptus.plasmaenchants

import com.google.common.collect.ImmutableSet
import com.jeff_media.updatechecker.UpdateCheckSource
import com.jeff_media.updatechecker.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.scheduler.BukkitRunnable
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifacts
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import us.teaminceptus.plasmaenchants.events.PlasmaEvents
import us.teaminceptus.plasmaenchants.events.SpawnEvents
import java.util.Properties
import java.io.InputStream
import java.io.IOException

private const val BSTATS_ID = 18713
private const val github = "Team-Inceptus/PlasmaEnchants"

/**
 * Represents the main PlasmaEnchants Plugin
 */
class PlasmaEnchants : JavaPlugin(), PlasmaConfig, PlasmaRegistry {

    companion object {
        @JvmStatic
        private val passiveTask = object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach {
                    val items = listOfNotNull(
                        it.inventory.armorContents.toList(),
                        listOf(it.inventory.itemInMainHand),
                        listOf(it.inventory.itemInOffHand)
                    ).flatten()

                    items.forEach { item ->
                        val meta = item.itemMeta!!

                        meta.plasmaEnchants.filter { entry -> entry.key.type == PType.PASSIVE }.forEach { entry -> entry.key.accept(PlayerTickEvent(it), entry.value) }
                        if (meta.hasArtifact() && meta.artifact!!.type == PType.PASSIVE) meta.artifact!!.accept(PlayerTickEvent(it))
                    }
                }
            }
        }

        @JvmStatic
        private val enchantments = mutableSetOf<PEnchantment>()

        @JvmStatic
        private val artifacts = mutableSetOf<PArtifact>()
    }

    private class PlayerTickEvent(player: Player) : PlayerEvent(player) {

        private companion object {
            @JvmStatic
            val HANDLERS = HandlerList()

            @JvmStatic
            fun getHandlerList(): HandlerList = HANDLERS
        }

        override fun getHandlers(): HandlerList = HANDLERS

    }

    private fun loadClasses() {
        PlasmaEvents(this)
        SpawnEvents(this)

        PlasmaCommands(this)

        PEnchantments.values().forEach(::register)
        PArtifacts.values().forEach(::register)
    }

    override fun onEnable() {
        saveDefaultConfig()

        loadClasses()
        PlasmaConfig.loadConfig()
        logger.info("Loaded Classes...")

        passiveTask.runTaskTimer(this, 0, 1)
        logger.info("Loaded Tasks...")

        UpdateChecker(this, UpdateCheckSource.GITHUB_RELEASE_TAG, github)
            .setDownloadLink("https://github.com/$github/releases/latest/")
            .setSupportLink("https://discord.gg/WVFNWEvuqX")
            .setNotifyOpsOnJoin(true)
            .setChangelogLink("https://github.com/$github/releases/latest/")
            .setUserAgent("Team-Inceptus/PlasmaEnchants v${PlasmaEnchants::class.java.`package`.implementationVersion}")
            .setColoredConsoleOutput(true)
            .setDonationLink("https://www.patreon.com/teaminceptus")
            .setNotifyRequesters(true)
            .checkEveryXHours(1.0)
            .checkNow()

        Metrics(this, BSTATS_ID)
        logger.info("Loaded Addons...")

        logger.info("Done!")
    }

    override fun onDisable() {
        Companion.artifacts.clear()
        Companion.enchantments.clear()
        logger.info("Unloaded Classes...")
        
        passiveTask.cancel()
        logger.info("Stopped Tasks...")

        logger.info("Done!")
    }

    // Configuration

    override fun get(key: String): String? {
        val p = Properties()
        val lang: String = if (language.equals("en", ignoreCase = true)) "" else "_$language"

        return try {
            val str: InputStream = PlasmaEnchants::class.java.getResourceAsStream("/lang/plasmaenchants$lang.properties") as InputStream

            p.load(str)
            str.close()

            val prop = p.getProperty(key, "null")
            if (prop.equals("null")) return null

            ChatColor.translateAlternateColorCodes('&', p.getProperty(key))
        } catch (e: IOException) {
            print(e)
            null
        }
    }

    // Registry Implementation

    override fun register(enchantment: PEnchantment) {
        if (enchantments.contains(enchantment)) throw IllegalArgumentException("Enchantment already registered!")
        Companion.enchantments.add(enchantment)
    }

    override fun register(artifact: PArtifact) {
        if (artifacts.contains(artifact)) throw IllegalArgumentException("Artifact already registered!")
        Companion.artifacts.add(artifact)
        Bukkit.addRecipe(artifact.recipe)
    }

    override fun unregister(enchantment: PEnchantment) {
        if (!enchantments.contains(enchantment)) throw IllegalArgumentException("Enchantment not registered!")
        Companion.enchantments.remove(enchantment)
    }

    override fun unregister(artifact: PArtifact) {
        if (!artifacts.contains(artifact)) throw IllegalArgumentException("Artifact not registered!")
        Companion.artifacts.remove(artifact)
    }

    override val artifacts: Set<PArtifact>
        get() = ImmutableSet.copyOf(Companion.artifacts)

    override val enchantments: Set<PEnchantment>
        get() = ImmutableSet.copyOf(Companion.enchantments)


}