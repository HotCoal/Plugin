package com.skillplugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.skillplugin.commands.MenuCommand;
import com.skillplugin.listeners.CompassListener;
import com.skillplugin.listeners.SkillListener;
import com.skillplugin.listeners.MenuListener;
import com.skillplugin.utils.SkillManager;
import com.skillplugin.utils.RewardManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class SkillPlugin extends JavaPlugin {

    private static SkillPlugin instance;
    private SkillManager skillManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveConfigFile("config.yml");

        skillManager = new SkillManager(this);
        skillManager.loadPlayerData();

        rewardManager = new RewardManager(this);

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // Регистрация команд
        getCommand("menu").setExecutor(new MenuCommand(this));

        getLogger().info("SkillPlugin успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (skillManager != null) {
            skillManager.savePlayerData();
        }
        if (rewardManager != null) {
            rewardManager.saveRewards();
        }
        getLogger().info("SkillPlugin выключен!");
    }

    private void saveConfigFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (in != null) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Файл " + fileName + " создан!");
                } else {
                    getLogger().warning("Не найден " + fileName + " в JAR!");
                }
            } catch (Exception e) {
                getLogger().warning("Ошибка при создании " + fileName + ": " + e.getMessage());
            }
        }
    }

    public static SkillPlugin getInstance() {
        return instance;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}