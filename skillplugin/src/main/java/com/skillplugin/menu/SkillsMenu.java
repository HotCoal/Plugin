/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.skillplugin.menu;

import com.skillplugin.SkillPlugin;
import com.skillplugin.utils.SkillManager;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkillsMenu {
    private final SkillPlugin plugin;

    public SkillsMenu(SkillPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, (int)27, (String)"\u00a78\u041d\u0430\u0432\u044b\u043a\u0438");
        SkillManager.PlayerSkills skills = this.plugin.getSkillManager().getPlayerSkills(player);
        inv.setItem(11, this.createSkillItem(Material.IRON_SWORD, "\u00a7c\u2694 \u0411\u043e\u0439", skills.getCombatLevel(), skills.getCombatXP()));
        inv.setItem(13, this.createSkillItem(Material.DIAMOND_PICKAXE, "\u00a7e\u26cf \u0428\u0430\u0445\u0442\u0435\u0440\u0441\u0442\u0432\u043e", skills.getMiningLevel(), skills.getMiningXP()));
        inv.setItem(15, this.createSkillItem(Material.IRON_AXE, "\u00a7a\ud83e\ude93 \u0420\u0443\u0431\u043a\u0430", skills.getWoodcuttingLevel(), skills.getWoodcuttingXP()));
        ItemStack glass = this.createGlassPane();
        for (int i = 0; i < 27; ++i) {
            if (inv.getItem(i) != null) continue;
            inv.setItem(i, glass);
        }
        player.openInventory(inv);
    }

    private ItemStack createSkillItem(Material material, String name, int level, int xp) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList("\u00a77\u0423\u0440\u043e\u0432\u0435\u043d\u044c: \u00a7e" + level + "\u00a77/100", "\u00a77\u041f\u0440\u043e\u0433\u0440\u0435\u0441\u0441: \u00a7a" + this.getProgressBar(xp, level), "\u00a77\u041e\u043f\u044b\u0442: \u00a7e" + xp + "\u00a77/" + level * 100, "", "\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c", "\u00a77\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043d\u0430\u0433\u0440\u0430\u0434\u044b!"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private String getProgressBar(int currentXP, int level) {
        int progress = currentXP % 100 / 10;
        StringBuilder bar = new StringBuilder("\u00a7a");
        for (int i = 0; i < 10; ++i) {
            bar.append(i < progress ? "\u25a0" : "\u00a77\u25a0");
        }
        return bar.toString();
    }
}

