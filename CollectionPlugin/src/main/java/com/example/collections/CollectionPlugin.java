// CollectionPlugin.java
package com.example.collections;

import com.example.collections.commands.CollectionCommand;
import com.example.collections.listeners.CollectionListener;
import com.example.collections.managers.CollectionManager;
import com.example.collections.managers.DatabaseManager;
import com.example.collections.managers.MenuManager;
import com.example.collections.managers.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectionPlugin extends JavaPlugin {
    private static CollectionPlugin instance;
    private CollectionManager collectionManager;
    private MenuManager menuManager;
    private RewardManager rewardManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Сохраняем конфиг
        saveDefaultConfig();

        // Инициализация менеджеров
        this.databaseManager = new DatabaseManager(this);
        this.collectionManager = new CollectionManager(this);
        this.menuManager = new MenuManager(this);
        this.rewardManager = new RewardManager(this);

        // Регистрация команд и слушателей
        getCommand("collections").setExecutor(new CollectionCommand(this));
        getServer().getPluginManager().registerEvents(new CollectionListener(this), this);

        // Загружаем данные для онлайн игроков
        databaseManager.loadAllPlayerData();

        // Автосохранение каждые 5 минут (6000 тиков = 5 минут)
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().info("Автосохранение данных игроков...");
            databaseManager.saveAllData();
        }, 6000L, 6000L);

        getLogger().info("Плагин Collections успешно запущен!");
        getLogger().info("Загружено коллекций: " + collectionManager.getMiningCollections().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("Сохранение данных перед выключением...");
        if (databaseManager != null) {
            databaseManager.saveAllData();
        }
        getLogger().info("Плагин Collections выключен.");
    }

    public static CollectionPlugin getInstance() {
        return instance;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}