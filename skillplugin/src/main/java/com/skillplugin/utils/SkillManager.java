/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 */
package com.skillplugin.utils;

import com.skillplugin.SkillPlugin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SkillManager {
    private final SkillPlugin plugin;
    private final Map<UUID, PlayerSkills> playerSkills;
    private File dataFile;
    private YamlConfiguration dataConfig;

    public SkillManager(SkillPlugin plugin) {
        this.plugin = plugin;
        this.playerSkills = new HashMap<UUID, PlayerSkills>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void loadPlayerData() {
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration((File)this.dataFile);
    }

    public void savePlayerData() {
        for (Map.Entry<UUID, PlayerSkills> entry : this.playerSkills.entrySet()) {
            String path = entry.getKey().toString();
            PlayerSkills skills = entry.getValue();
            this.dataConfig.set(path + ".combat", (Object)skills.getCombatLevel());
            this.dataConfig.set(path + ".combat-xp", (Object)skills.getCombatXP());
            this.dataConfig.set(path + ".mining", (Object)skills.getMiningLevel());
            this.dataConfig.set(path + ".mining-xp", (Object)skills.getMiningXP());
            this.dataConfig.set(path + ".woodcutting", (Object)skills.getWoodcuttingLevel());
            this.dataConfig.set(path + ".woodcutting-xp", (Object)skills.getWoodcuttingXP());
        }
        try {
            this.dataConfig.save(this.dataFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerSkills getPlayerSkills(Player player) {
        return this.playerSkills.computeIfAbsent(player.getUniqueId(), k -> {
            String path = player.getUniqueId().toString();
            return new PlayerSkills(this.dataConfig.getInt(path + ".combat", 1), this.dataConfig.getInt(path + ".combat-xp", 0), this.dataConfig.getInt(path + ".mining", 1), this.dataConfig.getInt(path + ".mining-xp", 0), this.dataConfig.getInt(path + ".woodcutting", 1), this.dataConfig.getInt(path + ".woodcutting-xp", 0));
        });
    }

    public void addCombatXP(Player player, int amount) {
        PlayerSkills skills = this.getPlayerSkills(player);
        int currentXP = skills.getCombatXP();
        int currentLevel = skills.getCombatLevel();
        int newXP = currentXP + amount;
        int newLevel = this.calculateLevel(newXP);
        if (newLevel > currentLevel) {
            player.sendMessage("\u00a7a\u2694 \u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0431\u043e\u044f \u043f\u043e\u0432\u044b\u0448\u0435\u043d! \u00a77\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c: \u00a7e" + newLevel);
        }
        skills.setCombatLevel(newLevel);
        skills.setCombatXP(newXP);
    }

    public void addMiningXP(Player player, int amount) {
        PlayerSkills skills = this.getPlayerSkills(player);
        int currentXP = skills.getMiningXP();
        int currentLevel = skills.getMiningLevel();
        int newXP = currentXP + amount;
        int newLevel = this.calculateLevel(newXP);
        if (newLevel > currentLevel) {
            player.sendMessage("\u00a7a\u26cf \u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0448\u0430\u0445\u0442\u0435\u0440\u0441\u0442\u0432\u0430 \u043f\u043e\u0432\u044b\u0448\u0435\u043d! \u00a77\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c: \u00a7e" + newLevel);
        }
        skills.setMiningLevel(newLevel);
        skills.setMiningXP(newXP);
    }

    public void addWoodcuttingXP(Player player, int amount) {
        PlayerSkills skills = this.getPlayerSkills(player);
        int currentXP = skills.getWoodcuttingXP();
        int currentLevel = skills.getWoodcuttingLevel();
        int newXP = currentXP + amount;
        int newLevel = this.calculateLevel(newXP);
        if (newLevel > currentLevel) {
            player.sendMessage("\u00a7a\ud83e\ude93 \u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0440\u0443\u0431\u043a\u0438 \u043f\u043e\u0432\u044b\u0448\u0435\u043d! \u00a77\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c: \u00a7e" + newLevel);
        }
        skills.setWoodcuttingLevel(newLevel);
        skills.setWoodcuttingXP(newXP);
    }

    private int calculateLevel(int xp) {
        return Math.min(xp / 100 + 1, 100);
    }

    public int getXPForNextLevel(int currentLevel) {
        return currentLevel * 100;
    }

    public static class PlayerSkills {
        private int combatLevel;
        private int combatXP;
        private int miningLevel;
        private int miningXP;
        private int woodcuttingLevel;
        private int woodcuttingXP;

        public PlayerSkills(int combatLevel, int combatXP, int miningLevel, int miningXP, int woodcuttingLevel, int woodcuttingXP) {
            this.combatLevel = combatLevel;
            this.combatXP = combatXP;
            this.miningLevel = miningLevel;
            this.miningXP = miningXP;
            this.woodcuttingLevel = woodcuttingLevel;
            this.woodcuttingXP = woodcuttingXP;
        }

        public int getCombatLevel() {
            return this.combatLevel;
        }

        public void setCombatLevel(int level) {
            this.combatLevel = level;
        }

        public int getCombatXP() {
            return this.combatXP;
        }

        public void setCombatXP(int xp) {
            this.combatXP = xp;
        }

        public int getMiningLevel() {
            return this.miningLevel;
        }

        public void setMiningLevel(int level) {
            this.miningLevel = level;
        }

        public int getMiningXP() {
            return this.miningXP;
        }

        public void setMiningXP(int xp) {
            this.miningXP = xp;
        }

        public int getWoodcuttingLevel() {
            return this.woodcuttingLevel;
        }

        public void setWoodcuttingLevel(int level) {
            this.woodcuttingLevel = level;
        }

        public int getWoodcuttingXP() {
            return this.woodcuttingXP;
        }

        public void setWoodcuttingXP(int xp) {
            this.woodcuttingXP = xp;
        }
    }
}

