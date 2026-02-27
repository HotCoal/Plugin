// listeners/CollectionListener.java
package com.example.collections.listeners;

import com.example.collections.CollectionPlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class CollectionListener implements Listener {
    private final CollectionPlugin plugin;
    private final PlainTextComponentSerializer textSerializer;

    public CollectionListener(CollectionPlugin plugin) {
        this.plugin = plugin;
        this.textSerializer = PlainTextComponentSerializer.plainText();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (block.hasMetadata("player_placed")) {
            return;
        }

        if (plugin.getCollectionManager().hasMiningCollection(blockType)) {
            plugin.getCollectionManager().handleBlockBreak(player, blockType);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.isCancelled()) return;

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught() instanceof org.bukkit.entity.Item) {
                org.bukkit.entity.Item item = (org.bukkit.entity.Item) event.getCaught();
                Player player = event.getPlayer();
                Material itemType = item.getItemStack().getType();

                // Проверяем, относится ли предмет к коллекциям рыбалки
                if (plugin.getCollectionManager().hasFishingCollection(itemType)) {
                    plugin.getCollectionManager().handleFishCatch(player, itemType);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();

        // Проверяем каждый выпавший предмет
        for (ItemStack drop : event.getDrops()) {
            Material itemType = drop.getType();

            // Проверяем, относится ли предмет к коллекциям рыбалки
            // (некоторые предметы можно получить и из мобов)
            if (plugin.getCollectionManager().hasFishingCollection(itemType)) {
                // Добавляем количество предметов в коллекцию
                for (int i = 0; i < drop.getAmount(); i++) {
                    plugin.getCollectionManager().handleFishCatch(player, itemType);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        event.getBlock().setMetadata("player_placed", new FixedMetadataValue(plugin, true));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().title() == null) return;

        String title = textSerializer.serialize(event.getView().title());

        if (title.contains("✦ Коллекции ✦") ||
                title.contains("⛏ Коллекция Майнинг") ||
                title.contains("⛏ ") ||
                title.contains("🎣 Коллекция Рыбалка") ||
                title.contains("🎣 ") ||
                title.contains("📚 Рецепты")) {

            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();

            // Главное меню
            if (title.contains("✦ Коллекции ✦")) {
                switch (event.getSlot()) {
                    case 22: // Майнинг
                        plugin.getMenuManager().openMiningCollection(player);
                        break;
                    case 20: // Ферма
                        plugin.getMenuManager().openFarmingCollection(player);
                        break;
                    case 24: // Рубка
                        plugin.getMenuManager().openWoodcuttingCollection(player);
                        break;
                    case 30: // Рыбалка
                        plugin.getMenuManager().openFishingCollection(player);
                        break;
                    case 32: // Охотник
                        plugin.getMenuManager().openHuntingCollection(player);
                        break;
                    case 49: // Книга рецептов
                        plugin.getMenuManager().openRecipesMenu(player);
                        break;
                }
            }
            // Меню майнинга
            else if (title.contains("⛏ Коллекция Майнинг")) {
                if (event.getSlot() == 49) {
                    plugin.getMenuManager().openMainMenu(player);
                } else {
                    int[] collectionSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
                    for (int slot : collectionSlots) {
                        if (event.getSlot() == slot && event.getCurrentItem() != null) {
                            Material material = event.getCurrentItem().getType();
                            plugin.getMenuManager().openCollectionDetail(player, material);
                            break;
                        }
                    }
                }
            }
            // Меню рыбалки
            else if (title.contains("🎣 Коллекция Рыбалка")) {
                if (event.getSlot() == 49) {
                    plugin.getMenuManager().openMainMenu(player);
                } else {
                    int[] collectionSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
                    for (int slot : collectionSlots) {
                        if (event.getSlot() == slot && event.getCurrentItem() != null) {
                            Material material = event.getCurrentItem().getType();
                            plugin.getMenuManager().openFishingCollectionDetail(player, material);
                            break;
                        }
                    }
                }
            }
            // Детальное меню коллекции
            else if (title.contains("⛏ ") || title.contains("🎣 ")) {
                if (event.getSlot() == 49) {
                    if (title.contains("⛏ ")) {
                        plugin.getMenuManager().openMiningCollection(player);
                    } else {
                        plugin.getMenuManager().openFishingCollection(player);
                    }
                }
            }
            // Меню рецептов
            else if (title.contains("📚 Рецепты")) {
                if (event.getSlot() == 49) {
                    plugin.getMenuManager().openMainMenu(player);
                }
            }
        }
    }
}