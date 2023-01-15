package com.fabegalo.github.plugin.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeVault extends Plugin {

    @Override
    public void onEnable() {
    	ProxyServer.getInstance().getLogger().info("Ola, eu sou BungeeCoord plugin!!!!!!!!!");
    }
}