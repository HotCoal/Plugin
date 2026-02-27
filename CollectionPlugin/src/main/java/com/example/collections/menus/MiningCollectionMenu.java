// menus/MiningCollectionMenu.java
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

public class MiningCollectionMenu implements InventoryHolder {
    private final Inventory inventory;
    private final CollectionPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final int[] levelRequirements;

    public MiningCollectionMenu(CollectionPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        this.levelRequirements = plugin.getCollectionManager().getLevelRequirements();
        this.inventory = Bukkit.createInventory(this, 54, Component.text("⛏ Коллекция Майнинг"));
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
        inventory.setItem(4, new ItemBuilder(Material.GOLDEN_PICKAXE)
                .name(Component.text("⛏ Коллекция Майнинг", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text(""),
                        Component.text("§7Выберите блок для просмотра"),
                        Component.text("§7деталей и наград за уровни"),
                        Component.text(""),
                        Component.text("§8▶ Макс. коллекций: §f" + getMaxedCollections() + " §8/ " + getTotalCollections())
                )
                .build());

        // Кнопка назад в главное меню
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(Component.text("← Назад", TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("§7Вернуться в главное меню"))
                .build());

        // Отображаем все коллекции майнинга
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int index = 0;

        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry :
                plugin.getCollectionManager().getMiningCollections().entrySet()) {

            if (index >= slots.length) break;

            Material material = entry.getKey();
            CollectionManager.CollectionEntry collection = entry.getValue();
            int currentAmount = playerData.getMiningCollectionAmount(material);
            int currentLevel = calculateLevel(currentAmount);

            inventory.setItem(slots[index++], createCollectionIcon(collection, currentAmount, currentLevel));
        }
    }

// В методе createCollectionIcon добавьте проверку на пустые коллекции:

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

        builder.name(Component.text(entry.getDisplayName(), TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Уровень: §e" + currentLevel + " §7/ 15"));
        lore.add(Component.text("§7Добыто: §e" + currentAmount + " §7блоков"));

        if (currentLevel < 15) {
            int nextRequirement = levelRequirements[currentLevel];
            int remaining = nextRequirement - currentAmount;
            lore.add(Component.text("§7До уровня " + (currentLevel + 1) + ": §e" + remaining + " §7блоков"));
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
        int maxed = 0;
        for (Map.Entry<Material, CollectionManager.CollectionEntry> entry :
                plugin.getCollectionManager().getMiningCollections().entrySet()) {
            int amount = playerData.getMiningCollectionAmount(entry.getKey());
            int level = calculateLevel(amount);
            if (level >= 15) maxed++;
        }
        return maxed;
    }

    private int getTotalCollections() {
        return plugin.getCollectionManager().getMiningCollections().size();
    }

    private int calculateLevel(int amount) {
        for (int level = 1; level <= 15; level++) {
            if (amount < levelRequirements[level - 1]) {
                return level - 1;
            }
        }
        return 15;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }
}