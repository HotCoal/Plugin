package com.example.ghoulplugin.entities;

import com.example.ghoulplugin.GhoulPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomEntityRegistry {

    private final GhoulPlugin plugin;
    private final Map<String, EntityType> customEntities = new HashMap<>();

    public static final NamespacedKey GHOUL_KEY = new NamespacedKey("ghoulplugin", "ghoul");
    public static final NamespacedKey ELOCATOR_KEY = new NamespacedKey("ghoulplugin", "elocator");
    public static final NamespacedKey SKINWALKER_KEY = new NamespacedKey("ghoulplugin", "skinwalker");

    public CustomEntityRegistry(GhoulPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEntities() {
        try {
            plugin.getLogger().info("Custom entity system initialized");
            customEntities.put("ghoul", EntityType.ZOMBIE);
            customEntities.put("elocator", EntityType.ENDERMAN);
            customEntities.put("skinwalker", EntityType.SKELETON);
            plugin.getLogger().info("Registered: Ghoul, ELocator, Skinwalker");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize custom entities", e);
        }
    }

    public static boolean isGhoul(Zombie zombie) {
        return zombie.getPersistentDataContainer().has(GHOUL_KEY, PersistentDataType.BOOLEAN);
    }

    public static void markAsGhoul(Zombie zombie) {
        zombie.getPersistentDataContainer().set(GHOUL_KEY, PersistentDataType.BOOLEAN, true);
    }

    public static boolean isELocator(Enderman enderman) {
        return enderman.getPersistentDataContainer().has(ELOCATOR_KEY, PersistentDataType.BOOLEAN);
    }

    public static void markAsELocator(Enderman enderman) {
        enderman.getPersistentDataContainer().set(ELOCATOR_KEY, PersistentDataType.BOOLEAN, true);
    }

    public static boolean isSkinwalker(Skeleton skeleton) {
        return skeleton.getPersistentDataContainer().has(SKINWALKER_KEY, PersistentDataType.BOOLEAN);
    }

    public static void markAsSkinwalker(Skeleton skeleton) {
        skeleton.getPersistentDataContainer().set(SKINWALKER_KEY, PersistentDataType.BOOLEAN, true);
    }

    public EntityType getCustomEntityType(String name) {
        return customEntities.get(name);
    }
}