package us.teaminceptus.plasmaenchants

import revxrsal.commands.bukkit.BukkitCommandHandler

internal class PlasmaCommands(private val plugin: PlasmaEnchants) {

    companion object {
        @JvmStatic
        lateinit var handler: BukkitCommandHandler

        @JvmStatic
        private fun hasHandler(): Boolean = ::handler.isInitialized
    }

    init {
        run {
            if (hasHandler()) return@run

            handler = BukkitCommandHandler.create(plugin)

            handler.registerBrigadier()
            handler.locale = plugin.getLocale()
        }
    }

}