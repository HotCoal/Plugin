package com.skillplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.skillplugin.SkillPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardManager {

    private final SkillPlugin plugin;
    private final Map<UUID, Map<String, Boolean>> claimedRewards;
    private File rewardsFile;
    private YamlConfiguration rewardsConfig;
    private Object economy;
    private boolean vaultEnabled = false;
    private boolean useOfflinePlayer = false;

    public RewardManager(SkillPlugin plugin) {
        this.plugin = plugin;
        this.claimedRewards = new HashMap<>();
        this.rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");

        checkVault();
        loadRewards();

        if (vaultEnabled) {
            plugin.getLogger().info("✓ Vault подключен успешно!");
        } else {
            plugin.getLogger().warning("✗ Vault НЕ подключен! Монеты выдаваться не будут.");
        }
    }

    private void checkVault() {
        try {
            plugin.getLogger().info("Проверка Vault...");

            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                plugin.getLogger().warning("Плагин Vault не найден на сервере!");
                return;
            }
            plugin.getLogger().info("Плагин Vault найден");

            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(economyClass);

            if (rsp == null) {
                plugin.getLogger().warning("Экономический провайдер не найден!");
                return;
            }

            economy = rsp.getProvider();

            // Проверяем, какие методы доступны
            try {
                economy.getClass().getMethod("getBalance", Player.class);
                plugin.getLogger().info("Метод getBalance(Player) доступен");
                useOfflinePlayer = false;
            } catch (NoSuchMethodException e) {
                try {
                    economy.getClass().getMethod("getBalance", OfflinePlayer.class);
                    plugin.getLogger().info("Метод getBalance(OfflinePlayer) доступен");
                    useOfflinePlayer = true;
                } catch (NoSuchMethodException e2) {
                    plugin.getLogger().warning("Не найден подходящий метод getBalance!");
                    return;
                }
            }

            vaultEnabled = true;
            String providerName = economy.getClass().getSimpleName();
            plugin.getLogger().info("✓ Vault успешно подключен! Провайдер: " + providerName);

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при подключении Vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void giveReward(Player player, String skill, int level) {
        plugin.getLogger().info("=== НАЧАЛО ВЫДАЧИ НАГРАДЫ ===");
        plugin.getLogger().info("Игрок: " + player.getName());
        plugin.getLogger().info("Навык: " + skill);
        plugin.getLogger().info("Уровень: " + level);

        String permission = "skillplugin." + skill + "." + level;

        // 1. ВЫДАЧА ПРАВ через LuckPerms
        boolean permissionGiven = false;
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            String command = "lp user " + player.getName() + " permission set " + permission + " true";
            plugin.getLogger().info("Выполняю команду: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            permissionGiven = true;
            plugin.getLogger().info("✓ Право " + permission + " выдано через LuckPerms");
        } else {
            plugin.getLogger().warning("✗ LuckPerms не найден! Права не выданы.");
        }

        // 2. ВЫДАЧА МОНЕТ через Vault
        double moneyAmount = getMoneyReward(level);
        plugin.getLogger().info("Сумма монет для выдачи: " + moneyAmount);

        boolean moneyGiven = false;

        if (!vaultEnabled) {
            plugin.getLogger().warning("✗ Vault не подключен (vaultEnabled = false)");
        } else if (economy == null) {
            plugin.getLogger().warning("✗ economy объект равен null");
        } else {
            try {
                plugin.getLogger().info("Пытаюсь выдать монеты через Vault...");

                // Получаем баланс ДО выдачи
                double balanceBefore = getBalance(player);
                plugin.getLogger().info("Баланс до выдачи: " + balanceBefore);

                // Выдаем монеты - используем depositPlayer с Player
                try {
                    economy.getClass().getMethod("depositPlayer", Player.class, double.class)
                            .invoke(economy, player, moneyAmount);
                    plugin.getLogger().info("Метод depositPlayer(Player, double) вызван");
                } catch (NoSuchMethodException e) {
                    // Пробуем с OfflinePlayer
                    economy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class)
                            .invoke(economy, player, moneyAmount);
                    plugin.getLogger().info("Метод depositPlayer(OfflinePlayer, double) вызван");
                }

                // Получаем баланс ПОСЛЕ выдачи
                double balanceAfter = getBalance(player);
                plugin.getLogger().info("Баланс после выдачи: " + balanceAfter);

                if (balanceAfter > balanceBefore) {
                    moneyGiven = true;
                    plugin.getLogger().info("✓ Монеты успешно выданы! Сумма: " + (balanceAfter - balanceBefore));
                } else {
                    plugin.getLogger().warning("✗ Баланс не изменился после выдачи!");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("✗ Ошибка при выдаче монет: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 3. ОТПРАВКА СООБЩЕНИЯ
        sendRewardMessage(player, skill, level, permission, moneyAmount, moneyGiven, permissionGiven);

        // 4. СОХРАНЕНИЕ СТАТУСА
        setRewardClaimed(player, skill, level);
        plugin.getLogger().info("Статус награды сохранен");
        plugin.getLogger().info("=== КОНЕЦ ВЫДАЧИ НАГРАДЫ ===");
    }

    private double getBalance(Player player) {
        if (!vaultEnabled || economy == null) return 0;

        try {
            if (useOfflinePlayer) {
                Object result = economy.getClass().getMethod("getBalance", OfflinePlayer.class)
                        .invoke(economy, player);
                return ((Number) result).doubleValue();
            } else {
                Object result = economy.getClass().getMethod("getBalance", Player.class)
                        .invoke(economy, player);
                return ((Number) result).doubleValue();
            }
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("Метод getBalance не найден: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при получении баланса: " + e.getMessage());
            return 0;
        }
    }

    private double getMoneyReward(int level) {
        String path = "rewards.levels." + level;
        if (plugin.getConfig().contains(path)) {
            double value = plugin.getConfig().getDouble(path);
            plugin.getLogger().info("Награда из конфига: уровень " + level + " = " + value);
            return value;
        }

        double defaultValue = level * 100.0;
        plugin.getLogger().info("Награда по умолчанию: уровень " + level + " = " + defaultValue);
        return defaultValue;
    }

    private void sendRewardMessage(Player player, String skill, int level, String permission,
                                   double money, boolean moneyGiven, boolean permissionGiven) {
        String skillName = getSkillDisplayName(skill);

        player.sendMessage("§a§lПОЛУЧЕНА НАГРАДА!");
        player.sendMessage("§7Вы получили награду за " + skillName + " " + level + " уровень!");

        if (permissionGiven) {
            player.sendMessage("§8▸ §fПрава: §7" + permission);
        }

        if (moneyGiven) {
            player.sendMessage("§8▸ §6Монеты: " + (int) money + "⛁");
        } else {
            player.sendMessage("§8▸ §6Монеты: " + (int) money + "⛁ §c(Ошибка выдачи)");
        }
    }

    private String getSkillDisplayName(String skill) {
        switch (skill) {
            case "combat": return "§cБой";
            case "mining": return "§eШахтерство";
            case "woodcutting": return "§aРубку";
            default: return skill;
        }
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    // Остальные методы (loadRewards, saveRewards, isRewardClaimed, setRewardClaimed) остаются без изменений

    public void loadRewards() {
        if (!rewardsFile.exists()) {
            try {
                rewardsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

        for (String uuid : rewardsConfig.getKeys(false)) {
            Map<String, Boolean> playerRewards = new HashMap<>();
            if (rewardsConfig.getConfigurationSection(uuid) != null) {
                for (String key : rewardsConfig.getConfigurationSection(uuid).getKeys(false)) {
                    playerRewards.put(key, rewardsConfig.getBoolean(uuid + "." + key));
                }
            }
            claimedRewards.put(UUID.fromString(uuid), playerRewards);
        }
        plugin.getLogger().info("Загружено наград: " + claimedRewards.size());
    }

    public void saveRewards() {
        for (Map.Entry<UUID, Map<String, Boolean>> entry : claimedRewards.entrySet()) {
            String path = entry.getKey().toString();
            for (Map.Entry<String, Boolean> reward : entry.getValue().entrySet()) {
                rewardsConfig.set(path + "." + reward.getKey(), reward.getValue());
            }
        }
        try {
            rewardsConfig.save(rewardsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRewardClaimed(Player player, String skill, int level) {
        String key = skill + "_" + level;
        return claimedRewards
                .getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(key, false);
    }

    public void setRewardClaimed(Player player, String skill, int level) {
        String key = skill + "_" + level;
        claimedRewards
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(key, true);
    }
}