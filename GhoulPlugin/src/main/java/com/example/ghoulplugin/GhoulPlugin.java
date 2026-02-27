package com.example.ghoulplugin;

import com.example.ghoulplugin.commands.SpawnMenuCommand;
import com.example.ghoulplugin.config.ConfigManager;
import com.example.ghoulplugin.entities.CustomEntityRegistry;
import com.example.ghoulplugin.entities.ELocator;
import com.example.ghoulplugin.entities.Skinwalker;
import com.example.ghoulplugin.listeners.EntityListeners;
import com.example.ghoulplugin.listeners.SpawnEggListener;
import com.example.ghoulplugin.managers.MobSpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GhoulPlugin extends JavaPlugin {

    private static GhoulPlugin instance;
    private ConfigManager configManager;
    private MobSpawnManager spawnManager;
    private CustomEntityRegistry entityRegistry;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("=================================");
        getLogger().info("GhoulPlugin is starting...");
        getLogger().info("=================================");

        try {
            // Save default config
            getLogger().info("Saving default config...");
            saveDefaultConfig();
            getLogger().info("Config saved");

            // Initialize managers
            getLogger().info("Initializing ConfigManager...");
            configManager = new ConfigManager(this);
            getLogger().info("ConfigManager initialized");

            getLogger().info("Initializing MobSpawnManager...");
            spawnManager = new MobSpawnManager(this);
            getLogger().info("MobSpawnManager initialized");

            // Register custom entities
            getLogger().info("Initializing CustomEntityRegistry...");
            entityRegistry = new CustomEntityRegistry(this);
            entityRegistry.registerEntities();
            getLogger().info("CustomEntityRegistry initialized");

            // Register listeners
            getLogger().info("Registering listeners...");
            getServer().getPluginManager().registerEvents(new EntityListeners(this), this);
            getServer().getPluginManager().registerEvents(new SpawnEggListener(), this);
            getServer().getPluginManager().registerEvents(new ELocator(), this);
            getServer().getPluginManager().registerEvents(new Skinwalker(), this);
            getLogger().info("Listeners registered");

            // Register commands
            getLogger().info("Registering commands...");

            var spawnMenuCommand = getCommand("spawnmenu");
            if (spawnMenuCommand != null) {
                spawnMenuCommand.setExecutor(new SpawnMenuCommand(this));
                getLogger().info("SpawnMenuCommand registered");
            } else {
                getLogger().warning("Command 'spawnmenu' not found in plugin.yml!");
            }

            // Start spawning task
            getLogger().info("Starting spawning tasks...");
            spawnManager.startSpawning();
            getLogger().info("Spawning tasks started");

            getLogger().info("=================================");
            getLogger().info("GhoulPlugin has been enabled successfully!");
            getLogger().info("=================================");

        } catch (Exception e) {
            getLogger().severe("=================================");
            getLogger().severe("ERROR ENABLING GHOULPLUGIN!");
            getLogger().severe("=================================");
            getLogger().severe("Error message: " + e.getMessage());
            e.printStackTrace();
            getLogger().severe("=================================");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("=================================");
        getLogger().info("GhoulPlugin is disabling...");

        try {
            if (spawnManager != null) {
                spawnManager.stopSpawning();
                getLogger().info("Spawning stopped");
            }

            // Очищаем данные
            ELocator.clearAll();
            Skinwalker.clearAll();
            getLogger().info("Entity data cleared");

            getLogger().info("GhoulPlugin has been disabled!");

        } catch (Exception e) {
            getLogger().severe("Error disabling plugin: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("=================================");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ghoulreload")) {
            if (sender.hasPermission("ghoulplugin.reload")) {
                spawnManager.reloadSettings();
                sender.sendMessage("§aGhoulPlugin config reloaded!");
                return true;
            }
        }
        return false;
    }

    public static GhoulPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MobSpawnManager getSpawnManager() {
        return spawnManager;
    }

    public CustomEntityRegistry getEntityRegistry() {
        return entityRegistry;
    }
}