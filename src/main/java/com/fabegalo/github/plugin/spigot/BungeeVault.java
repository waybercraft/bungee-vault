package com.fabegalo.github.plugin.spigot;

import com.fabegalo.github.plugin.spigot.economy.SharedEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeVault extends JavaPlugin {
    private SharedEconomy economy;

    @Override
    public void onEnable() {
        economy = new SharedEconomy(this);
        Bukkit.getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
        Bukkit.getServer().getLogger().info("BungeeVault enabled");
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
    }
}
