// managers/DatabaseManager.java
package com.example.collections.managers;

import com.example.collections.CollectionPlugin;
import com.example.collections.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private final CollectionPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataFolder;

    public DatabaseManager(CollectionPlugin plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "player-data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> {
            PlayerData data = loadPlayerData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
            }
            return data;
        });
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    private PlayerData loadPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");

        if (!playerFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData data = new PlayerData(uuid);

        // Загрузка данных майнинга
        if (config.contains("mining")) {
            Map<Material, Integer> miningData = new HashMap<>();
            for (String key : config.getConfigurationSection("mining").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int amount = config.getInt("mining." + key);
                    miningData.put(material, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал: " + key);
                }
            }
            data.loadMiningCollectionData(miningData);
        }

        // Загрузка данных рыбалки
        if (config.contains("fishing")) {
            Map<Material, Integer> fishingData = new HashMap<>();
            for (String key : config.getConfigurationSection("fishing").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int amount = config.getInt("fishing." + key);
                    fishingData.put(material, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал: " + key);
                }
            }
            data.loadFishingCollectionData(fishingData);
        }

        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Сохранение данных майнинга
        for (Map.Entry<Material, Integer> entry : data.getMiningCollectionData().entrySet()) {
            config.set("mining." + entry.getKey().name(), entry.getValue());
        }

        // Сохранение данных рыбалки
        for (Map.Entry<Material, Integer> entry : data.getFishingCollectionData().entrySet()) {
            config.set("fishing." + entry.getKey().name(), entry.getValue());
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить данные игрока " + uuid);
        }
    }

    public void saveAllData() {
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void loadAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getPlayerData(player.getUniqueId());
        }
    }
}