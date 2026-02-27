// menus/RecipesMenu.java
package com.example.collections.menus;

import com.example.collections.CollectionPlugin;
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
import java.util.Map;

public class RecipesMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final int[] levelRequirements;

    public RecipesMenu(CollectionPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.levelRequirements = plugin.getCollectionManager().getLevelRequirements();
        this.inventory = Bukkit.createInventory(this, 54, Component.text("📚 Рецепты"));
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

        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(Component.text("📚 Доступные рецепты", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Здесь отображаются все рецепты,"),
                        Component.text("§7которые вы открыли в коллекциях")
                )
                .build());

        // Кнопка назад
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(Component.text("← Назад", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("§7Вернуться в главное меню"))
                .build());

        // Отображаем доступные рецепты
        displayRecipes();
    }

    private void displayRecipes() {
        List<ItemStack> recipes = getUnlockedRecipes();

        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

        for (int i = 0; i < Math.min(recipes.size(), slots.length); i++) {
            inventory.setItem(slots[i], recipes.get(i));
        }

        // Если нет рецептов, показываем заглушку
        if (recipes.isEmpty()) {
            inventory.setItem(31, new ItemBuilder(Material.BARRIER)
                    .name(Component.text("§cНет доступных рецептов"))
                    .lore(
                            Component.text(""),
                            Component.text("§7Прокачивайте коллекции,"),
                            Component.text("§7чтобы открывать новые рецепты!")
                    )
                    .build());
        }
    }

    private List<ItemStack> getUnlockedRecipes() {
        List<ItemStack> recipes = new ArrayList<>();

        // Проверяем все коллекции и уровни
        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry :
                plugin.getCollectionManager().getMiningCollections().entrySet()) {

            Material material = entry.getKey();
            int amount = plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                    .getMiningCollectionAmount(material);

            for (int level = 1; level <= 15; level++) {
                int requirement = levelRequirements[level - 1];

                if (amount >= requirement) {
                    // Рецепт открыт
                    recipes.add(createRecipeIcon(material, level));
                }
            }
        }

        return recipes;
    }

    private ItemStack createRecipeIcon(Material material, int level) {
        String recipeName = plugin.getRewardManager().getRecipeForLevel(material, level);

        return new ItemBuilder(material)
                .name(Component.text("§a" + recipeName))
                .lore(
                        Component.text(""),
                        Component.text("§7Коллекция: §f" + getCollectionDisplayName(material)),
                        Component.text("§7Уровень: §e" + level),
                        Component.text(""),
                        Component.text("§7Ингредиенты:"),
                        Component.text("§8- §7" + getIngredients(material, level)),
                        Component.text(""),
                        Component.text("§7Крафт в верстаке")
                )
                .build();
    }

    private String getCollectionDisplayName(Material material) {
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

    private String getIngredients(Material material, int level) {
        return switch (material) {
            case COBBLESTONE -> "8 булыжника";
            case IRON_ORE, DEEPSLATE_IRON_ORE -> "5 железных слитков + палка";
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> "5 золотых слитков + палка";
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> "3 алмаза + 2 палки";
            case ANCIENT_DEBRIS -> "4 древних обломка + 4 золотых слитка";
            default -> "Разные материалы";
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