// managers/RewardManager.java
package com.example.collections.managers;

import com.example.collections.CollectionPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RewardManager {
    private final CollectionPlugin plugin;

    public RewardManager(CollectionPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveRewards(Player player, Material material, int level, String category) {
        int coins = calculateCoins(material, level, category);
        giveCoins(player, coins);

        givePermission(player, material, level, category);

        player.sendMessage("§a✦ Вы получили награды за " + level + " уровень:");
        player.sendMessage("  §7- §e" + coins + " монет");
        player.sendMessage("  §7- §f" + getRewardDescription(material, level, category));

        if (level % 5 == 0) {
            giveSpecialReward(player, material, level, category);
        }
    }

    private int calculateCoins(Material material, int level, String category) {
        int baseCoins = category.equals("mining") ? 15 : 20;

        int rarityBonus = switch (material) {
            case DIAMOND_ORE, EMERALD_ORE, ANCIENT_DEBRIS -> 40;
            case NAUTILUS_SHELL, PRISMARINE_CRYSTALS, PRISMARINE_SHARD -> 30;
            case TROPICAL_FISH, PUFFERFISH -> 25;
            default -> 0;
        };

        return (baseCoins * level) + rarityBonus;
    }

    private void giveCoins(Player player, int amount) {
        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "eco give " + player.getName() + " " + amount
        );
    }

    private void givePermission(Player player, Material material, int level, String category) {
        String permission = "collections." + category + ".recipe." + material.name().toLowerCase() + "." + level;

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "lp user " + player.getName() + " permission set " + permission + " true"
        );
    }

    private String getRewardDescription(Material material, int level, String category) {
        if (category.equals("mining")) {
            return switch (material) {
                case DIAMOND_ORE -> "Алмазная кирка";
                case EMERALD_ORE -> "Изумрудный блок";
                case ANCIENT_DEBRIS -> "Незеритовый слиток";
                default -> "Рецепт #" + level;
            };
        } else {
            return switch (material) {
                case COD -> "Удочка с приманкой";
                case SALMON -> "Блесна";
                case PUFFERFISH -> "Ядовитая наживка";
                case TROPICAL_FISH -> "Экзотическая приманка";
                case NAUTILUS_SHELL -> "Раковина проводника";
                case PRISMARINE_CRYSTALS -> "Кристальная удочка";
                default -> "Рыбацкий секрет #" + level;
            };
        }
    }

    private void giveSpecialReward(Player player, Material material, int level, String category) {
        if (category.equals("fishing")) {
            if (level == 5) {
                player.sendMessage("  §7- §b✓ Получена: Удочка с приманкой");
            } else if (level == 10) {
                player.sendMessage("  §7- §d✓ Получена: Зачарованная удочка");
            } else if (level == 15) {
                player.sendMessage("  §7- §6✓ Получен титул: Мастер-рыбак");
            }
        }
    }

    public String getRecipeForLevel(Material material, int level) {
        return getRewardDescription(material, level,
                material.name().contains("ORE") || material == Material.ANCIENT_DEBRIS ? "mining" : "fishing");
    }
}