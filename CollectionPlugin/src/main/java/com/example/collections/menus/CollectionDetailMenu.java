// menus/CollectionDetailMenu.java
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
import java.util.Arrays;
import java.util.List;

public class CollectionDetailMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Material material;
    private final CollectionManager.CollectionEntry collection;

    // Требования для уровней в стиле Hypixel SkyBlock
    private final int[] levelRequirements = {
            50, 100, 250, 500, 1000, 2500, 5000, 10000, 15000, 25000,
            35000, 50000, 75000, 100000, 150000
    };

    public CollectionDetailMenu(CollectionPlugin plugin, Player player, Material material) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        this.material = material;
        this.collection = plugin.getCollectionManager().getMiningCollections().get(material);
        this.inventory = Bukkit.createInventory(this, 54, Component.text("⛏ " + getCollectionName(material)));
        initializeItems();
    }

    private void initializeItems() {
        // Черная стеклянная панель по краям
        ItemStack blackPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .build();

        // Заполняем все слоты черным стеклом
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }

        if (collection == null) return;

        int currentAmount = playerData.getMiningCollectionAmount(material);
        int currentLevel = calculateCurrentLevel(currentAmount);

        // Информация о коллекции (в центре верха)
        inventory.setItem(4, new ItemBuilder(material)
                .name(Component.text(collection.getDisplayName(), TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Собрано: §e" + currentAmount + " §7/" + levelRequirements[14]),
                        Component.text("§7Текущий уровень: §e" + currentLevel + " §7/ 15"),
                        Component.text(""),
                        Component.text("§7Прогресс до следующего уровня:")
                )
                .build());

        // Кнопка назад
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(Component.text("← Назад", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("§7К списку коллекций"))
                .build());

        // Отображаем уровни в виде стеклянных панелей (ряд 9-17, 18-26, 27-35, 36-44)
        // Используем 4 ряда по 9 слотов для 15 уровней
        int[] glassSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37};

        for (int i = 0; i < 15; i++) {
            int level = i + 1;
            int requirement = getRequirementForLevel(level);
            boolean unlocked = currentAmount >= requirement;
            boolean inProgress = !unlocked && (i == 0 ? true : currentAmount >= getRequirementForLevel(level - 1));

            inventory.setItem(glassSlots[i], createGlassPaneForLevel(level, requirement, unlocked, inProgress, currentAmount));
        }

        // Добавляем информационные панели о наградах в правой части
        displayRewardsInfo(currentLevel);
    }

    private ItemStack createGlassPaneForLevel(int level, int requirement, boolean unlocked, boolean inProgress, int currentAmount) {
        Material glassType;
        String color;
        String status;

        if (unlocked) {
            glassType = Material.LIME_STAINED_GLASS_PANE; // Зеленый - открыт
            color = "§a";
            status = "ПОЛУЧЕНО";
        } else if (inProgress) {
            glassType = Material.YELLOW_STAINED_GLASS_PANE; // Желтый - в процессе
            color = "§e";
            status = "В ПРОЦЕССЕ";
        } else {
            glassType = Material.RED_STAINED_GLASS_PANE; // Красный - не открыт
            color = "§c";
            status = "НЕ ОТКРЫТ";
        }

        ItemBuilder builder = new ItemBuilder(glassType);

        builder.name(Component.text(color + "Уровень " + level, TextColor.fromHexString(unlocked ? "#55FF55" : (inProgress ? "#FFAA00" : "#FF5555")))
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Требуется: §e" + requirement + " §7блоков"));

        if (unlocked) {
            lore.add(Component.text("§7Прогресс: §a" + currentAmount + "§7/" + requirement + " ✔"));
        } else if (inProgress) {
            int needed = requirement - currentAmount;
            lore.add(Component.text("§7Осталось: §e" + needed + " §7блоков"));

            // Полоса прогресса
            int prevRequirement = level > 1 ? getRequirementForLevel(level - 1) : 0;
            int progress = currentAmount - prevRequirement;
            int totalNeeded = requirement - prevRequirement;
            String progressBar = createProgressBar(progress, totalNeeded, 10);
            lore.add(Component.text("§8[" + progressBar + "§8]"));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§6✦ Награды:"));

        String[] rewards = getLevelRewards(level);
        for (String reward : rewards) {
            lore.add(Component.text("  §7• " + reward));
        }

        if (unlocked) {
            lore.add(Component.text(""));
            lore.add(Component.text("§a✓ Награды получены"));
        } else {
            lore.add(Component.text(""));
            lore.add(Component.text("§e▼ Нажмите для предпросмотра"));
        }

        builder.lore(lore);

        return builder.build();
    }

    private void displayRewardsInfo(int currentLevel) {
        // Отображаем информацию о последних полученных наградах справа
        int[] infoSlots = {41, 42, 43};

        for (int i = 0; i < 3; i++) {
            int level = currentLevel - i;
            if (level >= 1) {
                inventory.setItem(infoSlots[i], createRewardInfoIcon(level));
            }
        }
    }

    private ItemStack createRewardInfoIcon(int level) {
        String[] rewards = getLevelRewards(level);

        return new ItemBuilder(Material.MAP)
                .name(Component.text("§aУровень " + level + " награды"))
                .lore(
                        Component.text(""),
                        Component.text("§7Получено:"),
                        Component.text("  §7• " + rewards[0]),
                        Component.text("  §7• " + rewards[1]),
                        rewards.length > 2 ? Component.text("  §7• " + rewards[2]) : Component.text(""),
                        Component.text(""),
                        Component.text("§8✓ Разблокировано")
                )
                .build();
    }

    private String createProgressBar(int current, int max, int length) {
        int progress = (int) ((double) current / max * length);
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i < progress) {
                bar.append("§a■");
            } else {
                bar.append("§7■");
            }
        }

        return bar.toString();
    }

    private int getRequirementForLevel(int level) {
        if (level < 1) return 0;
        if (level > 15) return levelRequirements[14];
        return levelRequirements[level - 1];
    }

    private int calculateCurrentLevel(int amount) {
        for (int i = 0; i < 15; i++) {
            if (amount < levelRequirements[i]) {
                return i;
            }
        }
        return 15;
    }

    private String[] getLevelRewards(int level) {
        String baseRecipe = plugin.getRewardManager().getRecipeForLevel(material, level);
        int coins = level * 25;

        List<String> rewards = new ArrayList<>();
        rewards.add("§e" + coins + " монет");
        rewards.add("§fРецепт: " + baseRecipe);

        // Особые награды за определенные уровни
        if (level == 5) {
            rewards.add("§d✦ Особый предмет");
        } else if (level == 10) {
            rewards.add("§5✨ Редкий свиток");
        } else if (level == 15) {
            rewards.add("§6⚡ ТИТУЛ: Мастер-горняк");
            rewards.add("§b✨ Легендарный ключ");
        } else if (level % 3 == 0) {
            rewards.add("§a✧ Опыт: 500");
        }

        return rewards.toArray(new String[0]);
    }

    private String getCollectionName(Material material) {
        return switch (material) {
            case DEEPSLATE -> "Глубинный сланец";
            case COBBLESTONE -> "Булыжник";
            case IRON_ORE -> "Железная руда";
            case DEEPSLATE_IRON_ORE -> "Глубинная железная руда";
            case COPPER_ORE -> "Медная руда";
            case DEEPSLATE_COPPER_ORE -> "Глубинная медная руда";
            case GOLD_ORE -> "Золотая руда";
            case DEEPSLATE_GOLD_ORE -> "Глубинная золотая руда";
            case LAPIS_ORE -> "Лазуритовая руда";
            case DEEPSLATE_LAPIS_ORE -> "Глубинная лазуритовая руда";
            case REDSTONE_ORE -> "Редстоун руда";
            case DEEPSLATE_REDSTONE_ORE -> "Глубинная редстоун руда";
            case DIAMOND_ORE -> "Алмазная руда";
            case DEEPSLATE_DIAMOND_ORE -> "Глубинная алмазная руда";
            case EMERALD_ORE -> "Изумрудная руда";
            case DEEPSLATE_EMERALD_ORE -> "Глубинная изумрудная руда";
            case ANCIENT_DEBRIS -> "Древний обломок";
            case NETHERRACK -> "Незерак";
            case END_STONE -> "Эндстоун";
            case MAGMA_BLOCK -> "Магма блок";
            case GLOWSTONE -> "Глоустоун";
            case AMETHYST_BLOCK -> "Аметист";
            case AMETHYST_CLUSTER -> "Аметистовая друза";
            case OBSIDIAN -> "Обсидиан";
            case SAND -> "Песок";
            case ICE -> "Лёд";
            case PACKED_ICE -> "Плотный лёд";
            case BLUE_ICE -> "Голубой лёд";
            case GRAVEL -> "Гравий";
            case NETHER_QUARTZ_ORE -> "Незер кварц";
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