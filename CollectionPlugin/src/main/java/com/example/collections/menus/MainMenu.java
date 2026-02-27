// menus/MainMenu.java
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

import java.util.Map;

public class MainMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final PlayerData playerData;

    public MainMenu(CollectionPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        this.inventory = Bukkit.createInventory(this, 54, Component.text("✦ Коллекции ✦"));
        initializeItems();
    }

    private void initializeItems() {
        ItemStack blackPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .build();

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }

        inventory.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .name(Component.text("✦ КОЛЛЕКЦИИ ✦", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Выберите коллекцию для прокачки"),
                        Component.text("§7и получения уникальных наград!")
                )
                .build());

        inventory.setItem(49, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(Component.text("📚 Рецепты", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Все доступные рецепты"),
                        Component.text("§7из ваших коллекций"),
                        Component.text(""),
                        Component.text("§e▸ Нажмите, чтобы открыть")
                )
                .build());

        // Майнинг коллекция
        inventory.setItem(22, new ItemBuilder(Material.GOLDEN_PICKAXE)
                .name(Component.text("⛏ Майнинг", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Добывай руды и блоки"),
                        Component.text("§7чтобы прокачивать коллекцию!"),
                        Component.text(""),
                        Component.text("§8▶ Макс. коллекций: §e" + getMaxedMiningCollections() + " §8/ §e" + getTotalMiningCollections()),
                        Component.text("§e▸ Нажмите, чтобы открыть")
                )
                .build());

        // Ферма
        inventory.setItem(20, new ItemBuilder(Material.WHEAT)
                .name(Component.text("🌾 Ферма", TextColor.fromHexString("#55FF55")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Будет доступно в будущем обновлении!"),
                        Component.text(""),
                        Component.text("§e▸ Скоро...")
                )
                .build());

        // Рубка
        inventory.setItem(24, new ItemBuilder(Material.OAK_LOG)
                .name(Component.text("🪓 Рубка", TextColor.fromHexString("#AA5500")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Будет доступно в будущем обновлении!"),
                        Component.text(""),
                        Component.text("§e▸ Скоро...")
                )
                .build());

        // Рыбалка
        inventory.setItem(30, new ItemBuilder(Material.FISHING_ROD)
                .name(Component.text("🎣 Рыбалка", TextColor.fromHexString("#5555FF")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Ловите рыбу и сокровища!"),
                        Component.text("§7Чтобы прокачивать коллекцию!"),
                        Component.text(""),
                        Component.text("§8▶ Макс. коллекций: §e" + getMaxedFishingCollections() + " §8/ §e" + getTotalFishingCollections()),
                        Component.text("§e▸ Нажмите, чтобы открыть")
                )
                .build());

        // Охотник
        inventory.setItem(32, new ItemBuilder(Material.BOW)
                .name(Component.text("🏹 Охотник", TextColor.fromHexString("#FF5555")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Будет доступно в будущем обновлении!"),
                        Component.text(""),
                        Component.text("§e▸ Скоро...")
                )
                .build());

        inventory.setItem(40, new ItemBuilder(Material.MAP)
                .name(Component.text("📊 Ваш прогресс", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Игрок: §f" + player.getName()),
                        Component.text("§7Макс. майнинг: §e" + getMaxedMiningCollections() + " §7/ " + getTotalMiningCollections()),
                        Component.text("§7Макс. рыбалка: §e" + getMaxedFishingCollections() + " §7/ " + getTotalFishingCollections()),
                        Component.text(""),
                        Component.text("§7Прокачивай коллекции до 15 уровня,"),
                        Component.text("§7чтобы получать уникальные награды!")
                )
                .build());
    }

    private int getMaxedMiningCollections() {
        return plugin.getCollectionManager().getMaxedMiningCollectionsCount(playerData);
    }

    private int getTotalMiningCollections() {
        int count = 0;
        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry : plugin.getCollectionManager().getMiningCollections().entrySet()) {
            if (!entry.getValue().isEmpty()) count++;
        }
        return count;
    }

    private int getMaxedFishingCollections() {
        return plugin.getCollectionManager().getMaxedFishingCollectionsCount(playerData);
    }

    private int getTotalFishingCollections() {
        int count = 0;
        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry : plugin.getCollectionManager().getFishingCollections().entrySet()) {
            if (!entry.getValue().isEmpty()) count++;
        }
        return count;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }
}