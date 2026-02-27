package com.skillplugin.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.skillplugin.SkillPlugin;
import com.skillplugin.utils.SkillManager;

import java.util.ArrayList;
import java.util.List;

public class SkillLevelMenu {

    private final SkillPlugin plugin;
    private final String skill;
    private final int LEVELS_PER_PAGE = 21;

    public SkillLevelMenu(SkillPlugin plugin, String skill) {
        this.plugin = plugin;
        this.skill = skill;
    }

    public void openMenu(Player player, int page) {
        SkillManager.PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
        int startLevel = page * LEVELS_PER_PAGE + 1;
        int endLevel = Math.min(startLevel + LEVELS_PER_PAGE - 1, 100);

        String skillName = getSkillDisplayName();
        Inventory inv = Bukkit.createInventory(null, 54, "§8" + skillName + " (" + (page + 1) + "/" + getTotalPages() + ")");

        int slot = 10;
        for (int level = startLevel; level <= endLevel; level++) {
            boolean isUnlocked = level <= getCurrentLevel(skills);
            boolean isClaimed = plugin.getRewardManager().isRewardClaimed(player, skill, level);

            inv.setItem(slot, createLevelItem(player, level, isUnlocked, isClaimed));

            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot % 9 == 0) slot += 1;
        }

        if (page > 0) {
            inv.setItem(45, createNavItem(Material.ARROW, "§c← Предыдущая страница"));
        }
        if (page < getTotalPages() - 1) {
            inv.setItem(53, createNavItem(Material.ARROW, "§a→ Следующая страница"));
        }
        inv.setItem(49, createNavItem(Material.BARRIER, "§e↩ Назад"));

        ItemStack glass = createGlassPane();
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }

    private ItemStack createLevelItem(Player player, int level, boolean unlocked, boolean claimed) {
        Material material;
        String status;
        List<String> lore = new ArrayList<>();

        double moneyReward = getMoneyReward(level);
        String formattedMoney = String.format("%,.0f", moneyReward);

        if (claimed) {
            material = Material.LIME_STAINED_GLASS_PANE;
            status = "§a✓ ПОЛУЧЕНО";
            lore.add("§7Вы уже получили награду");
            lore.add("§8▸ §6+" + formattedMoney + "⛁");
        } else if (unlocked) {
            material = Material.EXPERIENCE_BOTTLE;
            status = "§e⚡ ДОСТУПНО";
            lore.add("§7Нажмите, чтобы получить:");
            lore.add("§8▸ §fПрава: §7skillplugin." + skill + "." + level);
            lore.add("§8▸ §6Монеты: " + formattedMoney + "⛁");

            // Исправлено: используем isVaultEnabled() для проверки, но не вызываем getBalance()
            if (plugin.getRewardManager().isVaultEnabled()) {
                lore.add("§7Vault подключен");
            }

            lore.add("");
            lore.add("§e§lНАЖМИТЕ ЧТОБЫ ПОЛУЧИТЬ!");
        } else {
            material = Material.GRAY_STAINED_GLASS_PANE;
            status = "§c✗ ЗАКРЫТО";
            lore.add("§7Достигните " + level + " уровня");
            lore.add("§7чтобы получить §6" + formattedMoney + "⛁");
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Уровень " + level + " §7" + status);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private double getMoneyReward(int level) {
        String path = "rewards.levels." + level;
        if (plugin.getConfig().contains(path)) {
            return plugin.getConfig().getDouble(path);
        }
        return level * 100.0;
    }

    private ItemStack createNavItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private String getSkillDisplayName() {
        switch (skill) {
            case "combat": return "Бой";
            case "mining": return "Шахтерство";
            case "woodcutting": return "Рубка";
            default: return skill;
        }
    }

    private int getCurrentLevel(SkillManager.PlayerSkills skills) {
        switch (skill) {
            case "combat": return skills.getCombatLevel();
            case "mining": return skills.getMiningLevel();
            case "woodcutting": return skills.getWoodcuttingLevel();
            default: return 1;
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil(100.0 / LEVELS_PER_PAGE);
    }

    public void claimReward(Player player, int level) {
        plugin.getLogger().info("=== ПОПЫТКА ПОЛУЧЕНИЯ НАГРАДЫ ===");
        plugin.getLogger().info("Игрок: " + player.getName());
        plugin.getLogger().info("Навык: " + skill);
        plugin.getLogger().info("Уровень: " + level);

        if (plugin.getRewardManager().isRewardClaimed(player, skill, level)) {
            player.sendMessage("§cВы уже получили эту награду!");
            plugin.getLogger().info("Награда УЖЕ получена ранее");
            return;
        }

        SkillManager.PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
        int currentLevel = getCurrentLevel(skills);

        plugin.getLogger().info("Текущий уровень игрока: " + currentLevel);
        plugin.getLogger().info("Требуемый уровень: " + level);

        if (level > currentLevel) {
            player.sendMessage("§cВы еще не достигли этого уровня!");
            plugin.getLogger().info("Уровень игрока НЕДОСТАТОЧЕН");
            return;
        }

        plugin.getLogger().info("Вызываем giveReward...");
        plugin.getRewardManager().giveReward(player, skill, level);
    }
}