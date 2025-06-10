package com.fabegalo.github.plugin.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BungeeVault extends Plugin implements Listener {
    private static final String CHANNEL = "BungeeVault:Main";
    private final Map<UUID, Double> balances = new HashMap<>();
    private Configuration config;
    private File configFile;

    @Override
    public void onEnable() {
        ProxyServer.getInstance().registerChannel(CHANNEL);
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        load();
    }

    @Override
    public void onDisable() {
        save();
    }

    private void load() {
        try {
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) return;
            configFile = new File(getDataFolder(), "balances.yml");
            if (!configFile.exists()) configFile.createNewFile();
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            for (String key : config.getKeys()) {
                balances.put(UUID.fromString(key), config.getDouble(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        if (config == null) return;
        for (Map.Entry<UUID, Double> e : balances.entrySet()) {
            config.set(e.getKey().toString(), e.getValue());
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL)) return;
        if (event.getSender() instanceof PendingConnection) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
            String sub = in.readUTF();
            UUID id = UUID.fromString(in.readUTF());
            double amount = in.readDouble();
            double bal = balances.getOrDefault(id, 0.0);
            if ("DEPOSIT".equals(sub)) {
                bal += amount;
            } else if ("WITHDRAW".equals(sub)) {
                bal -= amount;
            } else if ("GET".equals(sub)) {
                // no change
            }
            balances.put(id, bal);
            broadcastBalance(id, bal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastBalance(UUID id, double bal) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("BALANCE");
            out.writeUTF(id.toString());
            out.writeDouble(bal);
            byte[] data = b.toByteArray();
            for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
                info.sendData(CHANNEL, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
