// menus/FishingCollectionMenu.java
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
import java.util.Map;

public class FishingCollectionMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final int[] levelRequirements;

    public FishingCollectionMenu(CollectionPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        this.levelRequirements = plugin.getCollectionManager().getLevelRequirements();
        this.inventory = Bukkit.createInventory(this, 54, Component.text("🎣 Коллекция Рыбалка"));
        initializeItems();
    }

    private void initializeItems() {
        ItemStack blackPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .build();

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }

        inventory.setItem(4, new ItemBuilder(Material.FISHING_ROD)
                .name(Component.text("🎣 Коллекция Рыбалка", TextColor.fromHexString("#5555FF")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Ловите рыбу и сокровища"),
                        Component.text("§7чтобы прокачивать коллекцию!"),
                        Component.text(""),
                        Component.text("§8▶ Макс. коллекций: §e" + getMaxedCollections() + " §8/ §e" + getTotalCollections())
                )
                .build());

        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(Component.text("← Назад", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("§7Вернуться в главное меню"))
                .build());

        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int index = 0;

        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry :
                plugin.getCollectionManager().getFishingCollections().entrySet()) {

            if (index >= slots.length) break;

            Material material = entry.getKey();
            CollectionManager.CollectionEntry collection = entry.getValue();
            int currentAmount = playerData.getFishingCollectionAmount(material);
            int currentLevel = plugin.getCollectionManager().calculateLevel(currentAmount);

            inventory.setItem(slots[index++], createCollectionIcon(collection, currentAmount, currentLevel));
        }
    }

    private ItemStack createCollectionIcon(CollectionManager.CollectionEntry entry, int currentAmount, int currentLevel) {
        if (entry.isEmpty()) {
            return new ItemBuilder(Material.BARRIER)
                    .name(Component.text("§7" + entry.getDisplayName()))
                    .lore(
                            Component.text(""),
                            Component.text("§8Здесь появится новая"),
                            Component.text("§8коллекция в будущем!")
                    )
                    .build();
        }

        ItemBuilder builder = new ItemBuilder(entry.getMaterial());

        builder.name(Component.text(entry.getDisplayName(), TextColor.fromHexString("#5555FF")).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Уровень: §e" + currentLevel + " §7/ 15"));
        lore.add(Component.text("§7Поймано: §e" + currentAmount + " §7шт"));

        if (currentLevel < 15) {
            int nextRequirement = levelRequirements[currentLevel];
            int remaining = nextRequirement - currentAmount;
            lore.add(Component.text("§7До уровня " + (currentLevel + 1) + ": §e" + remaining + " §7шт"));
        }

        if (currentLevel >= 15) {
            lore.add(Component.text(""));
            lore.add(Component.text("§a✔ МАКСИМАЛЬНЫЙ УРОВЕНЬ!"));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§e▸ Нажмите для деталей"));

        builder.lore(lore);

        if (currentLevel >= 15) {
            builder.glow();
        }

        return builder.build();
    }

    private int getMaxedCollections() {
        return plugin.getCollectionManager().getMaxedFishingCollectionsCount(playerData);
    }

    private int getTotalCollections() {
        int count = 0;
        for (CollectionManager.CollectionEntry entry : plugin.getCollectionManager().getFishingCollections().values()) {
            if (!entry.isEmpty()) count++;
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