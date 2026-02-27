// managers/CollectionManager.java
package com.example.collections.managers;

import com.example.collections.CollectionPlugin;
import com.example.collections.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;

public class CollectionManager {
    private final CollectionPlugin plugin;
    private final Map<String, CollectionCategory> categories;
    private final Map<Material, CollectionEntry> miningCollections;
    private final Map<Material, CollectionEntry> fishingCollections;
    private final Map<Material, Material> deepslateToNormalMap;

    // Требования для уровней в стиле Hypixel SkyBlock
    private final int[] levelRequirements = {
            50, 100, 250, 500, 1000, 2500, 5000, 10000, 15000, 25000,
            35000, 50000, 75000, 100000, 150000
    };

    public CollectionManager(CollectionPlugin plugin) {
        this.plugin = plugin;
        this.categories = new HashMap<>();
        this.miningCollections = new LinkedHashMap<>();
        this.fishingCollections = new LinkedHashMap<>();
        this.deepslateToNormalMap = new HashMap<>();
        initializeDeepslateMapping();
        initializeMiningCollections();
        initializeFishingCollections();
    }

    private void initializeDeepslateMapping() {
        deepslateToNormalMap.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD_ORE);
        deepslateToNormalMap.put(Material.DEEPSLATE_COAL_ORE, Material.COAL_ORE);

        deepslateToNormalMap.put(Material.IRON_ORE, Material.IRON_ORE);
        deepslateToNormalMap.put(Material.COPPER_ORE, Material.COPPER_ORE);
        deepslateToNormalMap.put(Material.GOLD_ORE, Material.GOLD_ORE);
        deepslateToNormalMap.put(Material.LAPIS_ORE, Material.LAPIS_ORE);
        deepslateToNormalMap.put(Material.REDSTONE_ORE, Material.REDSTONE_ORE);
        deepslateToNormalMap.put(Material.DIAMOND_ORE, Material.DIAMOND_ORE);
        deepslateToNormalMap.put(Material.EMERALD_ORE, Material.EMERALD_ORE);
        deepslateToNormalMap.put(Material.COAL_ORE, Material.COAL_ORE);
    }

    private void initializeMiningCollections() {
        // Первая строка коллекций майнинга
        addMiningCollection(Material.DEEPSLATE, "Глубинный сланец");
        addMiningCollection(Material.COBBLESTONE, "Булыжник");
        addMiningCollection(Material.IRON_ORE, "Железная руда");
        addMiningCollection(Material.COPPER_ORE, "Медная руда");
        addMiningCollection(Material.GOLD_ORE, "Золотая руда");
        addMiningCollection(Material.LAPIS_ORE, "Лазуритовая руда");
        addMiningCollection(Material.REDSTONE_ORE, "Редстоун руда");

        // Вторая строка коллекций майнинга
        addMiningCollection(Material.DIAMOND_ORE, "Алмазная руда");
        addMiningCollection(Material.EMERALD_ORE, "Изумрудная руда");
        addMiningCollection(Material.ANCIENT_DEBRIS, "Древний обломок");
        addMiningCollection(Material.NETHERRACK, "Незерак");
        addMiningCollection(Material.END_STONE, "Эндстоун");
        addMiningCollection(Material.MAGMA_BLOCK, "Магма блок");
        addMiningCollection(Material.GLOWSTONE, "Глоустоун");

        // Третья строка коллекций майнинга
        addMiningCollection(Material.AMETHYST_BLOCK, "Аметист");
        addMiningCollection(Material.OBSIDIAN, "Обсидиан");
        addMiningCollection(Material.SAND, "Песок");
        addMiningCollection(Material.ICE, "Лёд");
        addMiningCollection(Material.PACKED_ICE, "Плотный лёд");
        addMiningCollection(Material.GRAVEL, "Гравий");
        addMiningCollection(Material.NETHER_QUARTZ_ORE, "Незер кварц");

        plugin.getLogger().info("Загружено коллекций майнинга: " + miningCollections.size());
    }

    private void initializeFishingCollections() {
        // Первая строка коллекций рыбалки
        addFishingCollection(Material.COD, "Треска");
        addFishingCollection(Material.SALMON, "Лосось");
        addFishingCollection(Material.PUFFERFISH, "Иглобрюх");
        addFishingCollection(Material.TROPICAL_FISH, "Тропическая рыба");
        addFishingCollection(Material.NAUTILUS_SHELL, "Раковина наутилуса");
        addFishingCollection(Material.LILY_PAD, "Кувшинка");
        addFishingCollection(Material.INK_SAC, "Чернильный мешок");

        // Вторая строка коллекций рыбалки
        addFishingCollection(Material.SPONGE, "Губка");
        addFishingCollection(Material.WET_SPONGE, "Мокрая губка");
        addFishingCollection(Material.PRISMARINE_CRYSTALS, "Призмарин кристалл");
        addFishingCollection(Material.PRISMARINE_SHARD, "Призмарин осколок");
        addFishingCollection(Material.CLAY_BALL, "Глина");
        addFishingCollection(Material.KELP, "Ламинария");
        addFishingCollection(Material.TURTLE_SCUTE, "Черепаший щиток");

        // Третья строка - пустые слоты для будущих коллекций
        addEmptyFishingCollection("⚡ Скоро будет", 7);

        plugin.getLogger().info("Загружено коллекций рыбалки: " + fishingCollections.size());
    }

    private void addMiningCollection(Material material, String displayName) {
        miningCollections.put(material, new CollectionEntry(material, displayName, false, "mining"));
    }

    private void addFishingCollection(Material material, String displayName) {
        fishingCollections.put(material, new CollectionEntry(material, displayName, false, "fishing"));
    }

    private void addEmptyFishingCollection(String displayName, int count) {
        for (int i = 0; i < count; i++) {
            fishingCollections.put(Material.AIR, new CollectionEntry(null, displayName, true, "fishing"));
        }
    }

    public boolean hasMiningCollection(Material material) {
        if (deepslateToNormalMap.containsKey(material)) {
            material = deepslateToNormalMap.get(material);
        }
        return miningCollections.containsKey(material);
    }

    public boolean hasFishingCollection(Material material) {
        return fishingCollections.containsKey(material);
    }

    public void handleBlockBreak(Player player, Material material) {
        if (deepslateToNormalMap.containsKey(material)) {
            material = deepslateToNormalMap.get(material);
        }

        if (!miningCollections.containsKey(material)) {
            return;
        }

        CollectionEntry entry = miningCollections.get(material);
        if (entry.isEmpty()) return;

        PlayerData data = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        int currentAmount = data.getMiningCollectionAmount(material);
        int newAmount = currentAmount + 1;

        data.setMiningCollectionAmount(material, newAmount);
        plugin.getDatabaseManager().savePlayerData(player.getUniqueId());

        int oldLevel = calculateLevel(currentAmount);
        int newLevel = calculateLevel(newAmount);

        if (newLevel > oldLevel) {
            plugin.getRewardManager().giveRewards(player, material, newLevel, "mining");
            player.sendMessage("§a✦ Вы достигли " + newLevel + " уровня в коллекции майнинга " + entry.getDisplayName() + "!");
        }
    }

    public void handleFishCatch(Player player, Material material) {
        if (!fishingCollections.containsKey(material)) {
            return;
        }

        CollectionEntry entry = fishingCollections.get(material);
        if (entry.isEmpty()) return;

        PlayerData data = plugin.getDatabaseManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        int currentAmount = data.getFishingCollectionAmount(material);
        int newAmount = currentAmount + 1;

        data.setFishingCollectionAmount(material, newAmount);
        plugin.getDatabaseManager().savePlayerData(player.getUniqueId());

        int oldLevel = calculateLevel(currentAmount);
        int newLevel = calculateLevel(newAmount);

        if (newLevel > oldLevel) {
            plugin.getRewardManager().giveRewards(player, material, newLevel, "fishing");
            player.sendMessage("§a✦ Вы достигли " + newLevel + " уровня в коллекции рыбалки " + entry.getDisplayName() + "!");
        }
    }

    public int calculateLevel(int amount) {
        for (int level = 1; level <= 15; level++) {
            if (amount < levelRequirements[level - 1]) {
                return level - 1;
            }
        }
        return 15;
    }

    public int getRequirementForLevel(int level) {
        if (level < 1) return 0;
        if (level > 15) return levelRequirements[14];
        return levelRequirements[level - 1];
    }

    public Map<Material, CollectionEntry> getMiningCollections() {
        return miningCollections;
    }

    public Map<Material, CollectionEntry> getFishingCollections() {
        return fishingCollections;
    }

    public int[] getLevelRequirements() {
        return levelRequirements.clone();
    }

    public int getMaxedMiningCollectionsCount(PlayerData playerData) {
        int maxed = 0;
        for (Map.Entry<Material, CollectionEntry> entry : miningCollections.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            int amount = playerData.getMiningCollectionAmount(entry.getKey());
            int level = calculateLevel(amount);
            if (level >= 15) maxed++;
        }
        return maxed;
    }

    public int getMaxedFishingCollectionsCount(PlayerData playerData) {
        int maxed = 0;
        for (Map.Entry<Material, CollectionEntry> entry : fishingCollections.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            int amount = playerData.getFishingCollectionAmount(entry.getKey());
            int level = calculateLevel(amount);
            if (level >= 15) maxed++;
        }
        return maxed;
    }

    public static class CollectionCategory {
        private final String name;
        private final Material icon;
        private final String displayName;

        public CollectionCategory(String name, Material icon, String displayName) {
            this.name = name;
            this.icon = icon;
            this.displayName = displayName;
        }

        public String getName() { return name; }
        public Material getIcon() { return icon; }
        public String getDisplayName() { return displayName; }
    }

    public static class CollectionEntry {
        private final Material material;
        private final String displayName;
        private final boolean isEmpty;
        private final String category;

        public CollectionEntry(Material material, String displayName, boolean isEmpty, String category) {
            this.material = material;
            this.displayName = displayName;
            this.isEmpty = isEmpty;
            this.category = category;
        }

        public Material getMaterial() {
            return material != null ? material : Material.BARRIER;
        }

        public String getDisplayName() { return displayName; }
        public boolean isEmpty() { return isEmpty; }
        public String getCategory() { return category; }
    }
}