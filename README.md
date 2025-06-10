# bungee-vault

BungeeVault is a plugin that synchronizes player balances across every server in a Bungee network.
It forwards deposits and withdrawals through a plugin messaging channel to a central
BungeeCord plugin which stores balances in `balances.yml`. This allows changes to
a player's money even while they are offline.
