package us.teaminceptus.plasmaenchants

import org.bukkit.entity.Player
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.bukkit.annotation.CommandPermission
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment

internal class PlasmaCommands(private val plugin: PlasmaEnchants) {

    companion object {
        @JvmStatic
        lateinit var handler: BukkitCommandHandler

        @JvmStatic
        private fun hasHandler(): Boolean = ::handler.isInitialized

        @JvmStatic
        private fun get(key: String): String = PlasmaConfig.getConfig().get(key) ?: "Unknown Value"

        @JvmStatic
        private fun getMessage(key: String): String = PlasmaConfig.getConfig().getMessage(key) ?: "Unknown Value"

        @JvmStatic
        private fun format(str: String, vararg args: Any): String = String.format(PlasmaConfig.getConfig().getLocale(), str, args)
    }

    init {
        run {
            if (hasHandler()) return@run

            handler = BukkitCommandHandler.create(plugin)

            handler.registerBrigadier()
            handler.locale = plugin.getLocale()

            plugin.logger.info("Loaded Command Handler")
        }
    }

    @Command("plasmaenchants", "penchants")
    @Description("The main command for managing PlasmaEnchants Features")
    @Usage("/plasmaenchants <...>")
    @CommandPermission("plasmaenchants.admin")
    private class PlasmaCommands {

        @Subcommand("enchant add", "enchantment add", "enchants add", "enchantments add")
        fun addEnchantment(p: Player, enchantment: PEnchantment, @Default("1") level: Int) {
            val item = p.inventory.itemInMainHand

            if (!enchantment.getTarget().isValid(item.type))
                return p.sendMessage(getMessage("error.argument.item.held"))

            if (enchantment.getMaxLevel() < level)
                return p.sendMessage(getMessage("error.argument.level"))

            val meta = item.itemMeta!!

            if (meta.hasConflictingEnchant(enchantment))
                return p.sendMessage(getMessage("error.enchant.conflict"))

            meta.addEnchant(enchantment, level)
            item.itemMeta = meta

            p.sendMessage(format(getMessage("success.enchant.add"), enchantment.getName(), level))
        }

        @Subcommand("enchant remove", "enchantment remove", "enchants remove", "enchantments remove")
        fun removeEnchantment(p: Player, enchantment: PEnchantment) {
            val item = p.inventory.itemInMainHand

            if (!enchantment.getTarget().isValid(item.type))
                return p.sendMessage(getMessage("error.argument.item.held"))

            val meta = item.itemMeta!!

            if (!meta.hasEnchant(enchantment))
                return p.sendMessage(getMessage("error.enchant.not_found"))

            meta.removeEnchant(enchantment)
            item.itemMeta = meta

            p.sendMessage(format(getMessage("success.enchant.remove"), enchantment.getName()))
        }

        @Subcommand("enchant clear", "enchantment clear", "enchants clear", "enchantments clear")
        fun clearEnchantments(p: Player) {
            val item = p.inventory.itemInMainHand
            val meta = item.itemMeta!!

            if (!meta.hasPlasmaEnchants())
                return p.sendMessage(getMessage("error.enchant.not_found"))

            meta.clearPlasmaEnchants()
            item.itemMeta = meta

            p.sendMessage(getMessage("success.enchant.clear"))
        }


        @Subcommand("artifact set", "artifacts set")
        fun setArtifact(p: Player, artifact: PArtifact) {
            val item = p.inventory.itemInMainHand

            if (!artifact.getTarget().isValid(item.type))
                return p.sendMessage(getMessage("error.argument.item.held"))

            val meta = item.itemMeta!!

            meta.setArtifact(artifact)
            item.itemMeta = meta

            p.sendMessage(format(getMessage("success.artifact.set"), artifact.getName()))
        }

        @Subcommand("artifact remove", "artifacts remove")
        fun removeArtifact(p: Player) {
            val item = p.inventory.itemInMainHand
            val meta = item.itemMeta!!

            if (!meta.hasArtifact())
                return p.sendMessage(getMessage("error.artifact.not_found"))

            meta.removeArtifact()
            item.itemMeta = meta

            p.sendMessage(getMessage("success.artifact.remove"))
        }

    }

}