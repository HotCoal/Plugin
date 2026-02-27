// managers/MenuManager.java
package com.example.collections.managers;

import com.example.collections.CollectionPlugin;
import com.example.collections.menus.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MenuManager {
    private final CollectionPlugin plugin;

    public MenuManager(CollectionPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        new MainMenu(plugin, player).open();
    }

    public void openMiningCollection(Player player) {
        new MiningCollectionMenu(plugin, player).open();
    }

    public void openFishingCollection(Player player) {
        new FishingCollectionMenu(plugin, player).open();
    }

    public void openCollectionDetail(Player player, Material material) {
        new CollectionDetailMenu(plugin, player, material).open();
    }

    public void openFishingCollectionDetail(Player player, Material material) {
        new FishingCollectionDetailMenu(plugin, player, material).open();
    }

    public void openRecipesMenu(Player player) {
        new RecipesMenu(plugin, player).open();
    }

    // Будущие методы для других коллекций
    public void openFarmingCollection(Player player) {
        player.sendMessage("§eКоллекция ферма будет доступна в следующем обновлении!");
    }

    public void openWoodcuttingCollection(Player player) {
        player.sendMessage("§eКоллекция рубка будет доступна в следующем обновлении!");
    }

    public void openHuntingCollection(Player player) {
        player.sendMessage("§eКоллекция охотника будет доступна в следующем обновлении!");
    }
}