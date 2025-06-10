package com.fabegalo.github.plugin.spigot.economy;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedEconomy extends AbstractEconomy {
    private static final String CHANNEL = "BungeeVault:Main";
    private final Plugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();

    public SharedEconomy(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, (ch, player, msg) -> {
            if (!CHANNEL.equals(ch)) return;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(msg))) {
                String sub = in.readUTF();
                if ("BALANCE".equals(sub)) {
                    UUID id = UUID.fromString(in.readUTF());
                    double amount = in.readDouble();
                    balances.put(id, amount);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void send(String sub, UUID uuid, double amount) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF(sub);
            out.writeUTF(uuid.toString());
            out.writeDouble(amount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getServer().sendPluginMessage(plugin, CHANNEL, b.toByteArray());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "BungeeVault";
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        send("WITHDRAW", player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        send("DEPOSIT", player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return EconomyResponse.notImplemented();
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return EconomyResponse.notImplemented();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        send("GET", player.getUniqueId(), 0);
        return true;
    }
}
