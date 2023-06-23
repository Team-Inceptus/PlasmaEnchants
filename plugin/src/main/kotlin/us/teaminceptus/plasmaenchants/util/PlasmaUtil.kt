package us.teaminceptus.plasmaenchants.util

import org.bukkit.scheduler.BukkitRunnable
import us.teaminceptus.plasmaenchants.api.PlasmaConfig

object PlasmaUtil {

    @JvmStatic
    fun sync(action: () -> Unit, delay: Long = 1) {
        if (delay <= 1) {
            object : BukkitRunnable() {
                override fun run() = action()
            }.runTask(PlasmaConfig.plugin)
            return
        }

        object : BukkitRunnable() {
            override fun run() = action()
        }.runTaskLater(PlasmaConfig.plugin, delay)
    }

}