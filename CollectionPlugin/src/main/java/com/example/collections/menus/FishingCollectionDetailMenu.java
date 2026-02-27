// menus/FishingCollectionDetailMenu.java
package com.example.collections.menus;

import com.example.collections.CollectionPlugin;
import com.example.collections.data.PlayerData;
import com.example.collections.managers.CollectionManager;
import com.example.collections.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FishingCollectionDetailMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Material material;
    private final CollectionManager.CollectionEntry collection;
    private final int[] levelRequirements;

    public FishingCollectionDetailMenu(CollectionPlugin plugin, Player player, Material material) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        this.material = material;
        this.collection = plugin.getCollectionManager().getFishingCollections().get(material);
        this.levelRequirements = plugin.getCollectionManager().getLevelRequirements();
        this.inventory = Bukkit.createInventory(this, 54, Component.text("🎣 " + getCollectionName(material)));
        initializeItems();
    }

    private void initializeItems() {
        ItemStack blackPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .build();

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }

        if (collection == null || collection.isEmpty()) return;

        int currentAmount = playerData.getFishingCollectionAmount(material);
        int currentLevel = plugin.getCollectionManager().calculateLevel(currentAmount);

        inventory.setItem(4, new ItemBuilder(collection.getMaterial())
                .name(Component.text(collection.getDisplayName(), TextColor.fromHexString("#5555FF")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Поймано: §e" + currentAmount + " §7шт"),
                        Component.text("§7Текущий уровень: §e" + currentLevel + " §7/ 15")
                )
                .build());

        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(Component.text("← Назад", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("§7К списку коллекций"))
                .build());

        int[][] levelSlots = {
                {19, 20, 21, 22, 23, 24, 25},
                {34, 33, 32, 31, 30, 29, 28},
                {37, 38, 39, 40, 41, 42, 43}
        };

        int level = 1;
        for (int row = 0; row < levelSlots.length; row++) {
            for (int col = 0; col < levelSlots[row].length; col++) {
                if (level <= 15) {
                    int slot = levelSlots[row][col];
                    int requirement = levelRequirements[level - 1];
                    boolean unlocked = currentAmount >= requirement;
                    boolean isCurrentLevel = (level == currentLevel + 1 && currentLevel < 15);

                    inventory.setItem(slot, createLevelIcon(level, requirement, unlocked, isCurrentLevel, currentAmount));
                    level++;
                }
            }
        }
    }

    private ItemStack createLevelIcon(int level, int requirement, boolean unlocked, boolean isCurrentLevel, int currentAmount) {
        Material iconMaterial = unlocked ? Material.LIME_STAINED_GLASS_PANE :
                (isCurrentLevel ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

        ItemBuilder builder = new ItemBuilder(iconMaterial);

        String color = unlocked ? "§a" : (isCurrentLevel ? "§e" : "§c");
        String status = unlocked ? "✔ ПОЙМАНО" : (isCurrentLevel ? "⚡ ТЕКУЩИЙ" : "✖ НЕ ПОЙМАНО");

        builder.name(Component.text(color + "Уровень " + level, TextColor.fromHexString(unlocked ? "#55FF55" : (isCurrentLevel ? "#FFAA00" : "#FF5555")))
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Требуется: §e" + requirement + " §7шт"));
        lore.add(Component.text("§7Статус: " + color + status));

        if (isCurrentLevel) {
            int needed = requirement - currentAmount;
            lore.add(Component.text("§7Осталось: §e" + needed + " §7шт"));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§6✦ Награды:"));

        String[] rewards = getLevelRewards(level);
        for (String reward : rewards) {
            lore.add(Component.text("  §7• " + reward));
        }

        builder.lore(lore);

        if (unlocked) {
            builder.glow();
        }

        return builder.build();
    }

    private String[] getLevelRewards(int level) {
        List<String> rewards = new ArrayList<>();

        int coins = 20 * level;
        rewards.add("§e" + coins + " монет");

        if (level == 5) {
            rewards.add("§bУдочка с приманкой");
        } else if (level == 10) {
            rewards.add("§dЗачарованная удочка");
        } else if (level == 15) {
            rewards.add("§6⚡ ТИТУЛ: Мастер-рыбак");
            rewards.add("§b✨ Легендарная наживка");
        } else if (level % 3 == 0) {
            rewards.add("§aОпыт рыбалки: 1000");
        }

        return rewards.toArray(new String[0]);
    }

    private String getCollectionName(Material material) {
        return switch (material) {
            case COD -> "Треска";
            case SALMON -> "Лосось";
            case PUFFERFISH -> "Иглобрюх";
            case TROPICAL_FISH -> "Тропическая рыба";
            case NAUTILUS_SHELL -> "Раковина наутилуса";
            case LILY_PAD -> "Кувшинка";
            case INK_SAC -> "Чернильный мешок";
            case SPONGE -> "Губка";
            case WET_SPONGE -> "Мокрая губка";
            case PRISMARINE_CRYSTALS -> "Призмарин кристалл";
            case PRISMARINE_SHARD -> "Призмарин осколок";
            case CLAY_BALL -> "Глина";
            case KELP -> "Ламинария";
            case TURTLE_SCUTE -> "Черепаший щиток";
            default -> "Неизвестно";
        };
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }
}