package us.teaminceptus.plasmaenchants.api;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

interface PlasmaConfig {

    fun getPlugin() : Plugin {
        return Bukkit.getPluginManager().getPlugin("PlasmaEnchants")
    }

}