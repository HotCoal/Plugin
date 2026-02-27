package com.skillplugin.utils;

import org.bukkit.Bukkit;
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
    private net.milkbowl.vault.economy.Economy economy;
    private boolean vaultEnabled = false;

    public RewardManager(SkillPlugin plugin) {
        this.plugin = plugin;
        this.claimedRewards = new HashMap<>();
        this.rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");

        setupEconomy();
        loadRewards();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault не найден! Монеты выдаваться не будут.");
            return;
        }

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (rsp == null) {
            plugin.getLogger().warning("Economy провайдер не найден! Монеты выдаваться не будут.");
            return;
        }

        economy = rsp.getProvider();
        vaultEnabled = true;
        plugin.getLogger().info("Vault успешно подключен! Монеты будут выдаваться через " + economy.getName());
    }

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

    public void giveReward(Player player, String skill, int level) {
        String permission = "skillplugin." + skill + "." + level;

        // 1. ВЫДАЧА ПРАВ через LuckPerms
        boolean permissionGiven = false;
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "lp user " + player.getName() + " permission set " + permission + " true");
            permissionGiven = true;
            plugin.getLogger().info("Право " + permission + " выдано игроку " + player.getName() + " через LuckPerms");
        } else {
            plugin.getLogger().warning("LuckPerms не найден! Права не выданы.");
        }

        // 2. ВЫДАЧА МОНЕТ через Vault/Essentials
        double moneyAmount = getMoneyReward(level);
        boolean moneyGiven = false;

        if (vaultEnabled && economy != null) {
            try {
                economy.depositPlayer(player, moneyAmount);
                moneyGiven = true;
                plugin.getLogger().info("Выдано " + moneyAmount + " монет игроку " + player.getName() +
                        " через " + economy.getName());

                // Проверяем баланс после выдачи
                double newBalance = economy.getBalance(player);
                plugin.getLogger().info("Новый баланс игрока " + player.getName() + ": " + newBalance);

            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при выдаче монет: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("Vault не доступен, монеты не выданы игроку " + player.getName());
        }

        // 3. ОТПРАВКА СООБЩЕНИЯ
        sendRewardMessage(player, skill, level, permission, moneyAmount, moneyGiven, permissionGiven);

        // 4. СОХРАНЕНИЕ СТАТУСА
        setRewardClaimed(player, skill, level);
    }

    private double getMoneyReward(int level) {
        String path = "rewards.levels." + level;
        if (plugin.getConfig().contains(path)) {
            return plugin.getConfig().getDouble(path);
        }

        String formula = plugin.getConfig().getString("rewards.formula", "level * 100");
        String evaluated = formula.replace("level", String.valueOf(level));

        try {
            return evaluateSimpleExpression(evaluated);
        } catch (Exception e) {
            return level * 100.0;
        }
    }

    private double evaluateSimpleExpression(String expression) {
        String[] parts = expression.split(" ");
        if (parts.length == 3) {
            double left = Double.parseDouble(parts[0]);
            double right = Double.parseDouble(parts[2]);
            switch (parts[1]) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/": return left / right;
            }
        }
        return Double.parseDouble(expression);
    }

    private void sendRewardMessage(Player player, String skill, int level, String permission,
                                   double money, boolean moneyGiven, boolean permissionGiven) {
        String skillName = getSkillDisplayName(skill);
        List<String> messageLines = plugin.getConfig().getStringList("rewards.message");

        if (messageLines.isEmpty()) {
            // Сообщение по умолчанию, если в конфиге ничего нет
            player.sendMessage("§a§lПОЛУЧЕНА НАГРАДА!");
            player.sendMessage("§7Вы получили награду за " + skillName + " " + level + " уровень!");
            if (permissionGiven) {
                player.sendMessage("§8▸ §fПрава: §7" + permission);
            }
            if (moneyGiven) {
                player.sendMessage("§8▸ §6Монеты: " + (int) money + "⛁");
            } else {
                player.sendMessage("§8▸ §6Монеты: " + (int) money + "⛁ §c(Vault не работает!)");
            }
            return;
        }

        for (String line : messageLines) {
            String formatted = line
                    .replace("&", "§")
                    .replace("{skill}", skillName)
                    .replace("{level}", String.valueOf(level))
                    .replace("{permission}", permission)
                    .replace("{money}", String.valueOf((int) money));

            if (!moneyGiven && line.contains("{money}")) {
                formatted += " §c(Vault не найден!)";
            }
            if (!permissionGiven && line.contains("{permission}")) {
                formatted += " §c(LuckPerms не найден!)";
            }

            player.sendMessage(formatted);
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

    public double getBalance(Player player) {
        if (!vaultEnabled || economy == null) return 0;

        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            return 0;
        }
    }
}