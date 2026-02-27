package com.example.ghoulplugin.commands;

import com.example.ghoulplugin.GhoulPlugin;
import com.example.ghoulplugin.entities.CustomSpawnEgg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SpawnMenuCommand implements CommandExecutor {

    private final GhoulPlugin plugin;

    public SpawnMenuCommand(GhoulPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("ghoulplugin.spawnmenu")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        openSpawnMenu(player);
        return true;
    }

    private void openSpawnMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "✦ Spawn Eggs Menu ✦");

        // Декоративные стекла
        ItemStack purpleGlass = createGlass(Material.PURPLE_STAINED_GLASS_PANE, " ");
        ItemStack blackGlass = createGlass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack redGlass = createGlass(Material.RED_STAINED_GLASS_PANE, " ");

        // Заполняем рамки
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i == 9 || i == 17) {
                if (i == 4 || i == 22) continue;

                if (i < 9) {
                    menu.setItem(i, purpleGlass.clone());
                } else if (i >= 18) {
                    menu.setItem(i, blackGlass.clone());
                } else {
                    menu.setItem(i, redGlass.clone());
                }
            }
        }

        // Информационный предмет
        menu.setItem(4, createInfoItem());

        // Яйца мобов
        menu.setItem(11, CustomSpawnEgg.createGhoulSpawnEgg());        // Ghoul
        menu.setItem(13, CustomSpawnEgg.createELocatorSpawnEgg());     // ELocator
        menu.setItem(15, CustomSpawnEgg.createSkinwalkerSpawnEgg());   // Skinwalker

        // Информация об управлении
        menu.setItem(22, createControlsItem());

        player.openInventory(menu);
    }

    private ItemStack createGlass(Material material, String name) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(name);
        glass.setItemMeta(meta);
        return glass;
    }

    private ItemStack createInfoItem() {
        ItemStack info = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "⚡ Custom Mobs Spawn Eggs ⚡");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click on an egg to receive it",
                ChatColor.GRAY + "Right-click with egg to spawn mob",
                "",
                ChatColor.DARK_PURPLE + "Available Mobs:",
                ChatColor.RED + "  • Ghoul" + ChatColor.GRAY + " - Level 5",
                ChatColor.DARK_PURPLE + "  • ELocator" + ChatColor.GRAY + " - Level 40",
                ChatColor.RED + "  • Skinwalker" + ChatColor.GRAY + " - Level 50 (NEW!)",
                "",
                ChatColor.YELLOW + "Permissions:",
                ChatColor.GREEN + "  ghoulplugin.spawnmenu"
        ));
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack createControlsItem() {
        ItemStack controls = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = controls.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "⚙ Controls ⚙");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left Click: Get 1 egg",
                ChatColor.GRAY + "Shift + Click: Get 16 eggs",
                ChatColor.GRAY + "Right Click with egg: Spawn mob"
        ));
        controls.setItemMeta(meta);
        return controls;
    }
}