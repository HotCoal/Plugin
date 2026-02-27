package com.example.ghoulplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            player.sendMessage(ChatColor.GREEN + "✓ GhoulPlugin is working correctly!");
            player.sendMessage(ChatColor.GRAY + "Plugin version: 1.0.0");
        } else {
            sender.sendMessage("✓ GhoulPlugin is working correctly!");
        }
        return true;
    }
}