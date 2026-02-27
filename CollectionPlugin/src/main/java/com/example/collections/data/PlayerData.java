// data/PlayerData.java
package com.example.collections.data;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final Map<Material, Integer> miningCollection;
    private final Map<Material, Integer> fishingCollection;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.miningCollection = new HashMap<>();
        this.fishingCollection = new HashMap<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    // Mining collections
    public int getMiningCollectionAmount(Material material) {
        return miningCollection.getOrDefault(material, 0);
    }

    public void setMiningCollectionAmount(Material material, int amount) {
        miningCollection.put(material, amount);
    }

    public Map<Material, Integer> getMiningCollectionData() {
        return new HashMap<>(miningCollection);
    }

    public void loadMiningCollectionData(Map<Material, Integer> data) {
        miningCollection.clear();
        miningCollection.putAll(data);
    }

    // Fishing collections
    public int getFishingCollectionAmount(Material material) {
        return fishingCollection.getOrDefault(material, 0);
    }

    public void setFishingCollectionAmount(Material material, int amount) {
        fishingCollection.put(material, amount);
    }

    public Map<Material, Integer> getFishingCollectionData() {
        return new HashMap<>(fishingCollection);
    }

    public void loadFishingCollectionData(Map<Material, Integer> data) {
        fishingCollection.clear();
        fishingCollection.putAll(data);
    }
}