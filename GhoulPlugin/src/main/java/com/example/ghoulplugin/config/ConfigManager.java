package com.example.ghoulplugin.config;

import com.example.ghoulplugin.GhoulPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final GhoulPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(GhoulPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public int getMaxGhouls() {
        return config.getInt("spawn-settings.max-ghouls", 50);
    }

    public int getSpawnFrequency() {
        return config.getInt("spawn-settings.spawn-frequency", 600);
    }

    public int getSpawnRadius() {
        return config.getInt("spawn-settings.spawn-radius", 30);
    }

    public int getMinYLevel() {
        return config.getInt("spawn-settings.min-y-level", 0);
    }

    public int getMaxYLevel() {
        return config.getInt("spawn-settings.max-y-level", 256);
    }
}