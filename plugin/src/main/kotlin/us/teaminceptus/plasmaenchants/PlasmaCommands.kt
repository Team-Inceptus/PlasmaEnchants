package us.teaminceptus.plasmaenchants

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import revxrsal.commands.annotation.*
import revxrsal.commands.annotation.Optional
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.bukkit.annotation.CommandPermission
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import java.lang.String.format
import java.util.*

@Suppress("deprecation")
internal class PlasmaCommands(private val plugin: PlasmaEnchants) {

    companion object {
        @JvmStatic
        lateinit var handler: BukkitCommandHandler

        @JvmStatic
        val plugin: PlasmaEnchants = PlasmaConfig.plugin as PlasmaEnchants

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

        @JvmStatic
        val cancelKey = NamespacedKey(plugin, "cancel")

        @JvmStatic
        val urlKey = NamespacedKey(plugin, "url")

        @JvmStatic
        private val GUI_BACKGROUND: ItemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta!!.apply {
                setDisplayName(" ")

                persistentDataContainer[cancelKey, PersistentDataType.BYTE] = 1.toByte()
            }
        }

        @JvmStatic
        private fun getHead(head: String): ItemStack {
            val item = ItemStack(Material.PLAYER_HEAD)
            val meta = item.itemMeta as SkullMeta

            val p = Properties()
            p.load(PlasmaConfig.plugin.javaClass.getResourceAsStream("/util/heads.properties"))

            val profile = GameProfile(UUID.randomUUID(), null)
            profile.properties.put("textures", Property("textures", p.getProperty(head)))

            val setP = meta.javaClass.getDeclaredMethod("setProfile", GameProfile::class.java)
            setP.isAccessible = true
            setP.invoke(meta, profile)

            item.itemMeta = meta
            return item
        }

        @JvmStatic
        private fun reloadConfig(sender: CommandSender) {
            try {
                PlasmaEnchants.passiveTask.cancel()
                PlasmaEnchants.passiveTask.runTaskTimer(plugin, 0, 1)
            } catch (ignored: IllegalStateException) {}

            plugin.reloadConfig()
            PlasmaConfig.loadConfig()

            sender.sendMessage(getSuccess("command.reloaded"))
            if (sender is Player)
                sender.playSound(sender.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F)
        }
    }

    init {
        run {
            if (hasHandler()) return@run

            handler = BukkitCommandHandler.create(plugin)

            handler
                .registerValueResolver(PEnchantment::class.java) resolver@{ ctx ->
                    val name = ctx.popForParameter()
                    return@resolver plugin.getEnchantment(name)
                }.registerValueResolver(PArtifact::class.java) resolver@{ ctx ->
                    val name = ctx.popForParameter()
                    return@resolver plugin.getArtifact(name)
                }

            handler.autoCompleter
                .registerParameterSuggestions(PEnchantment::class.java, SuggestionProvider.map(plugin::enchantments) { enchant ->
                    if (enchant.key.namespace == plugin.name.lowercase()) enchant.key.key else enchant.key.toString()
                })
                .registerParameterSuggestions(PArtifact::class.java, SuggestionProvider.map(plugin::artifacts) { artifact ->
                    if (artifact.key.namespace == plugin.name.lowercase()) artifact.key.key else artifact.key.toString()
                })

            handler.register(this, PlasmaEnchantsCommands())

            handler.registerBrigadier()
            handler.locale = plugin.locale

            plugin.logger.info("Loaded Command Handler")
        }
    }

    @Command("plasmareload", "preload", "plasmar")
    @Description("Reloads the plugin")
    @CommandPermission("plasmaenchants.admin.reload")
    fun reload(sender: CommandSender) {
        reloadConfig(sender)
    }

    @Command("plasmaenchants", "penchants")
    @Description("The main command for managing PlasmaEnchants Features")
    @Usage("/plasmaenchants <...>")
    private class PlasmaEnchantsCommands {

        @Subcommand("reload")
        @CommandPermission("plasmaenchants.admin.reload")
        fun reload(sender: CommandSender) {
            reloadConfig(sender)
        }

        @Subcommand("info")
        @CommandPermission("plasmaenchants.user.info")
        fun pluginInfo(p: Player) {
            val inv = Bukkit.createInventory(null, 27, "${get("plugin.prefix").replace("[\\[\\]]".toRegex(), "")}${ChatColor.DARK_PURPLE}v${plugin.description.version}")

            for (i in 0..8) inv.setItem(i, GUI_BACKGROUND)
            for (i in inv.size - 9 until inv.size) inv.setItem(i, GUI_BACKGROUND)
            inv.setItem(9, GUI_BACKGROUND)
            inv.setItem(17, GUI_BACKGROUND)

            inv.addItem(ItemStack(Material.PLAYER_HEAD).apply {
                itemMeta = (itemMeta as SkullMeta).apply {
                    setDisplayName("${ChatColor.GREEN}GamerCoder")

                    owner = "GamerCoder"
                    lore = listOf("${ChatColor.GRAY}${get("constants.author")}")
                    persistentDataContainer[cancelKey, PersistentDataType.BYTE] = 1.toByte()
                }
            })

            inv.addItem(getHead("discord").apply {
                itemMeta = (itemMeta as SkullMeta).apply {
                    setDisplayName("${ChatColor.BLUE}Discord")

                    persistentDataContainer[cancelKey, PersistentDataType.BYTE] = 1.toByte()
                    persistentDataContainer[urlKey, PersistentDataType.STRING] = "https://discord.gg/WVFNWEvuqX"
                }
            })

            inv.addItem(getHead("github").apply {
                itemMeta = (itemMeta as SkullMeta).apply {
                    setDisplayName("${ChatColor.DARK_GRAY}GitHub")

                    persistentDataContainer[cancelKey, PersistentDataType.BYTE] = 1.toByte()
                    persistentDataContainer[urlKey, PersistentDataType.STRING] = "https://github.com/Team-Inceptus/PlasmaEnchants"
                }
            })

            inv.addItem(ItemStack(Material.MAP).apply {
                itemMeta = itemMeta!!.apply {
                    setDisplayName("${ChatColor.GOLD}${get("constants.statistics")}")
                    lore = listOf(
                        String.format(get("constants.enchants"), "${ChatColor.AQUA}${String.format(plugin.locale, "${plugin.enchantments.size}", "%,d")}"),
                        String.format(get("constants.artifacts"), "${ChatColor.GREEN}${String.format(plugin.locale, "${plugin.artifacts.size}", "%,d")}")
                    )

                    persistentDataContainer[cancelKey, PersistentDataType.BYTE] = 1.toByte()
                }
            })

            while (inv.firstEmpty() != -1)
                inv.setItem(inv.firstEmpty(), GUI_BACKGROUND)

            p.openInventory(inv)
            p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 2F)
        }

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

        @Subcommand("item book")
        @CommandPermission("plasmaenchants.admin.items")
        fun giveBook(p: Player, enchantment: PEnchantment, @Default("1") level: Int) {
            if (enchantment.maxLevel < level && !plugin.isIgnoreEnchantmentLevelRestriction)
                return p.sendMessage(getFailure("error.argument.level"))

            p.inventory.addItem(enchantment.generateBook(level))
            p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 2F)
        }

        @Subcommand("item artifact")
        @CommandPermission("plasmaenchants.admin.items")
        fun giveArtifact(p: Player, @Optional artifact: PArtifact?) {
            p.inventory.addItem(artifact?.item ?: PArtifact.RAW_ARTIFACT)
            p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 2F)
        }

    }

}