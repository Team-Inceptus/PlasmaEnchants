package us.teaminceptus.plasmaenchants

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import revxrsal.commands.annotation.*
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.bukkit.annotation.CommandPermission
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import java.lang.String.format
import java.util.*

internal class PlasmaCommands(private val plugin: PlasmaEnchants) {

    companion object {
        @JvmStatic
        lateinit var handler: BukkitCommandHandler

        @JvmStatic
        lateinit var plugin: PlasmaEnchants

        @JvmStatic
        private fun hasHandler(): Boolean = ::handler.isInitialized

        @JvmStatic
        private fun get(key: String): String = PlasmaConfig.config.get(key) ?: "Unknown Value"

        @JvmStatic
        private fun getMessage(key: String): String = PlasmaConfig.config.getMessage(key) ?: "Unknown Value"

        @JvmStatic
        private fun getSuccess(key: String): String = "${get("plugin.prefix")}${ChatColor.GREEN}${get(key)}"

        @JvmStatic
        private fun getFailure(key: String): String = "${get("plugin.prefix")}${ChatColor.RED}${get(key)}"
    }

    init {
        run {
            if (hasHandler()) return@run

            Companion.plugin = plugin
            handler = BukkitCommandHandler.create(plugin)

            handler
                .registerValueResolver(PEnchantment::class.java) resolver@{ ctx ->
                    val name = ctx.popForParameter()
                    return@resolver plugin.enchantments.first { it.key.key.equals(name, ignoreCase = true) }
                }.registerValueResolver(PArtifact::class.java) resolver@{ ctx ->
                    val name = ctx.popForParameter()
                    return@resolver plugin.artifacts.first { it.key.key.equals(name, ignoreCase = true) }
                }

            handler.autoCompleter
                .registerParameterSuggestions(PEnchantment::class.java, SuggestionProvider.map(plugin::enchantments) { enchant -> enchant.key.key })
                .registerParameterSuggestions(PArtifact::class.java, SuggestionProvider.map(plugin::artifacts) { artifact -> artifact.key.key })

            handler.register(this, PlasmaEnchantsCommands())

            handler.registerBrigadier()
            handler.locale = plugin.locale

            plugin.logger.info("Loaded Command Handler")
        }
    }

    @Command("plasmaenchants", "penchants")
    @Description("The main command for managing PlasmaEnchants Features")
    @Usage("/plasmaenchants <...>")
    private class PlasmaEnchantsCommands {

        @Subcommand("enchant add", "enchantment add", "enchants add", "enchantments add")
        @CommandPermission("plasmaenchants.admin.manage_enchants")
        fun addEnchantment(p: Player, enchantment: PEnchantment, @Default("1") level: Int) {
            val item = p.inventory.itemInMainHand

            if (!enchantment.target.isValid(item.type))
                return p.sendMessage(getFailure("error.argument.item.held"))

            if (enchantment.maxLevel < level && !plugin.isIgnoreEnchantmentLevelRestriction)
                return p.sendMessage(getFailure("error.argument.level"))

            val meta = item.itemMeta!!

            if (meta.hasEnchant(enchantment))
                return p.sendMessage(getFailure("error.enchant.exists"))

            if (meta.hasConflictingEnchant(enchantment))
                return p.sendMessage(getFailure("error.enchant.conflict"))

            meta.addEnchant(enchantment, level, plugin.isIgnoreEnchantmentLevelRestriction)
            item.itemMeta = meta

            p.sendMessage(format(getSuccess("success.enchant.add"), "${ChatColor.GOLD}${enchantment.displayName} ${level.toRoman()}${ChatColor.GREEN}"))
        }

        @Subcommand("enchant remove", "enchantment remove", "enchants remove", "enchantments remove")
        @CommandPermission("plasmaenchants.admin.manage_enchants")
        fun removeEnchantment(p: Player, enchantment: PEnchantment) {
            val item = p.inventory.itemInMainHand

            if (!enchantment.target.isValid(item.type))
                return p.sendMessage(getFailure("error.argument.item.held"))

            val meta = item.itemMeta!!

            if (!meta.hasEnchant(enchantment))
                return p.sendMessage(getFailure("error.enchant.not_found"))

            meta.removeEnchant(enchantment)
            item.itemMeta = meta

            p.sendMessage(format(getSuccess("success.enchant.remove"), "${ChatColor.GOLD}${enchantment.displayName}${ChatColor.GREEN}"))
        }

        @Subcommand("enchant clear", "enchantment clear", "enchants clear", "enchantments clear")
        @CommandPermission("plasmaenchants.admin.manage_enchants")
        fun clearEnchantments(p: Player) {
            val item = p.inventory.itemInMainHand
            val meta = item.itemMeta!!

            if (!meta.hasPlasmaEnchants())
                return p.sendMessage(getFailure("error.enchant.not_found"))

            meta.clearPlasmaEnchants()
            item.itemMeta = meta

            p.sendMessage(getSuccess("success.enchant.clear"))
        }


        @Subcommand("artifact set", "artifacts set")
        @CommandPermission("plasmaenchants.admin.manage_artifacts")
        fun setArtifact(p: Player, artifact: PArtifact) {
            val item = p.inventory.itemInMainHand

            if (!artifact.target.isValid(item.type))
                return p.sendMessage(getFailure("error.argument.item.held"))

            val meta = item.itemMeta!!

            meta.artifact = artifact
            item.itemMeta = meta

            p.sendMessage(format(getSuccess("success.artifact.set"), "${ChatColor.GOLD}${artifact.displayName}"))
        }

        @Subcommand("artifact remove", "artifacts remove")
        @CommandPermission("plasmaenchants.admin.manage_artifacts")
        fun removeArtifact(p: Player) {
            val item = p.inventory.itemInMainHand
            val meta = item.itemMeta!!

            if (!meta.hasArtifact())
                return p.sendMessage(getFailure("error.artifact.not_found"))

            meta.removeArtifact()
            item.itemMeta = meta

            p.sendMessage(getSuccess("success.artifact.remove"))
        }

    }

}