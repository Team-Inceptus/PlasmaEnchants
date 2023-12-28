package us.teaminceptus.plasmaenchants.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PiglinBarterEvent
import org.bukkit.plugin.Plugin
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import java.security.SecureRandom

internal class Events1_16(plugin: Plugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    val r: SecureRandom = SecureRandom()

    @EventHandler
    fun barter(event: PiglinBarterEvent) {
        if (r.nextDouble() > PlasmaConfig.config.enchantmentBarteringChance) return

        val blacklist = PlasmaConfig.config.blacklistedBarteringEnchantments
        val whitelist = PlasmaConfig.config.whitelistedBarteringEnchantments

        val enchantment = PlasmaConfig.registry.enchantments
            .filter { !it.isDisabled && !it.isSpawnBlacklisted }
            .filter {
                if (whitelist.isNotEmpty())
                    whitelist.contains(it)
                else
                    !blacklist.contains(it)
            }
            .randomOrNull() ?: return

        val minLevel = PlasmaConfig.config.enchantmentBarteringMinEnchantLevel.coerceAtLeast(1)
        val maxLevel = PlasmaConfig.config.enchantmentBarteringMaxEnchantLevel.coerceIn(minLevel..(enchantment.maxLevel + 1))

        val level = (if (minLevel >= maxLevel) minLevel else r.nextInt(minLevel, maxLevel)).coerceAtMost(enchantment.maxLevel)
        event.outcome.add(enchantment.generateBook(level))
    }

}