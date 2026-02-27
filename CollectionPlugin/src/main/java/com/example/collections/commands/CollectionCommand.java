// commands/CollectionCommand.java
package com.example.collections.commands;

import com.example.collections.CollectionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CollectionCommand implements CommandExecutor {
    private final CollectionPlugin plugin;

    public CollectionCommand(CollectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        if (args.length == 0) {
            plugin.getMenuManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "mining":
                plugin.getMenuManager().openMiningCollection(player);
                break;
            case "reload":
                if (player.hasPermission("collections.admin")) {
                    plugin.reloadConfig();
                    player.sendMessage("§aКонфигурация перезагружена!");
                } else {
                    player.sendMessage("§cУ вас нет прав!");
                }
                break;
            default:
                player.sendMessage("§cИспользование: /collections [mining/reload]");
                break;
        }

        return true;
    }
}