package com.skillplugin.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.skillplugin.SkillPlugin;

import java.util.Arrays;

public class MainMenu {

    private final SkillPlugin plugin;

    public MainMenu(SkillPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Главное меню");

        // Навыки (слот 11)
        ItemStack skills = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta skillsMeta = skills.getItemMeta();
        skillsMeta.setDisplayName("§a⚔ Навыки");
        skillsMeta.setLore(Arrays.asList(
                "§7Прогрессия навыков",
                "§7Награды за уровни",
                "",
                "§7Нажмите, чтобы открыть"
        ));
        skills.setItemMeta(skillsMeta);
        inv.setItem(11, skills);

        // Коллекции (слот 13) - открывает другой плагин через команду
        ItemStack collections = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta collectionsMeta = collections.getItemMeta();
        collectionsMeta.setDisplayName("§e⛏ Коллекции");
        collectionsMeta.setLore(Arrays.asList(
                "§7Собирайте ресурсы и открывайте",
                "§7новые рецепты и возможности",
                "",
                "§7Блоки: §fкамень, руды, незерит и др.",
                "§7Прогрессия: §e15 уровней",
                "",
                "§e§lНАЖМИТЕ ЧТОБЫ ОТКРЫТЬ!"
        ));
        collections.setItemMeta(collectionsMeta);
        inv.setItem(13, collections);

        // Декоративные стекла
        ItemStack glass = createGlassPane();
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
}