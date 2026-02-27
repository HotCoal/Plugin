package com.example.ghoulplugin.config;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MobSpawnSettings {

    private final boolean enabled;
    private final Set<String> worlds;
    private final Set<String> biomes;
    private final int minYLevel;
    private final int maxYLevel;
    private final int spawnFrequency;
    private final double spawnChance;
    private final int chunkRadius;
    private final int maxMobsInRadius;
    private final String lightLevel;
    private final boolean canBurnInDaylight;
    private final boolean requirePlayerNearby;
    private final int minPlayersNearby;
    private final int playerDistance;
    private final boolean requireDarkness;
    private final int darknessLevel;

    // Настройки мобов
    private final double health;
    private final double damagePerSecond;
    private final double grabRange;
    private final int weaknessDuration;
    private final double normalSpeed;
    private final double carryingSpeed;

    public MobSpawnSettings(ConfigurationSection spawnConfig, ConfigurationSection mobConfig) {
        // Настройки спавна
        this.enabled = spawnConfig.getBoolean("enabled", true);
        this.worlds = Set.copyOf(spawnConfig.getStringList("worlds"));
        this.biomes = Set.copyOf(spawnConfig.getStringList("biomes"));
        this.minYLevel = spawnConfig.getInt("min-y-level", 0);
        this.maxYLevel = spawnConfig.getInt("max-y-level", 256);
        this.spawnFrequency = spawnConfig.getInt("spawn-frequency", 600);
        this.spawnChance = spawnConfig.getDouble("spawn-chance", 0.5);

        ConfigurationSection radiusSection = spawnConfig.getConfigurationSection("chunk-radius");
        if (radiusSection != null) {
            this.chunkRadius = radiusSection.getInt("radius", 4);
            this.maxMobsInRadius = radiusSection.getInt("max-mobs-in-radius", 3);
        } else {
            this.chunkRadius = 4;
            this.maxMobsInRadius = 3;
        }

        ConfigurationSection conditions = spawnConfig.getConfigurationSection("spawn-conditions");
        if (conditions != null) {
            this.lightLevel = conditions.getString("light-level", "any");
            this.canBurnInDaylight = conditions.getBoolean("can-burn-in-daylight", false);
            this.requirePlayerNearby = conditions.getBoolean("require-player-nearby", true);
            this.minPlayersNearby = conditions.getInt("min-players-nearby", 1);
            this.playerDistance = conditions.getInt("player-distance", 30);
            this.requireDarkness = conditions.getBoolean("require-darkness", false);
            this.darknessLevel = conditions.getInt("darkness-level", 7);
        } else {
            this.lightLevel = "any";
            this.canBurnInDaylight = false;
            this.requirePlayerNearby = true;
            this.minPlayersNearby = 1;
            this.playerDistance = 30;
            this.requireDarkness = false;
            this.darknessLevel = 7;
        }

        // Настройки моба
        if (mobConfig != null) {
            this.health = mobConfig.getDouble("health", 100.0);
            this.damagePerSecond = mobConfig.getDouble("damage-per-second", 4.0);
            this.grabRange = mobConfig.getDouble("grab-range", 2.0);
            this.weaknessDuration = mobConfig.getInt("weakness-duration", 600);
            this.normalSpeed = mobConfig.getDouble("speed", 0.35);
            this.carryingSpeed = mobConfig.getDouble("carrying-speed", 0.09);
        } else {
            this.health = 100.0;
            this.damagePerSecond = 4.0;
            this.grabRange = 2.0;
            this.weaknessDuration = 600;
            this.normalSpeed = 0.35;
            this.carryingSpeed = 0.09;
        }
    }

    // Геттеры для настроек спавна
    public boolean isEnabled() { return enabled; }
    public boolean isAllowedWorld(World world) { return worlds.contains(world.getName()); }
    public boolean isAllowedBiome(String biomeKey) { return biomes.contains(biomeKey); }
    public boolean isAllowedYLevel(int y) { return y >= minYLevel && y <= maxYLevel; }
    public int getSpawnFrequency() { return spawnFrequency; }
    public double getSpawnChance() { return spawnChance; }
    public int getChunkRadius() { return chunkRadius; }
    public int getMaxMobsInRadius() { return maxMobsInRadius; }

    public boolean canSpawnNow(World world, int lightLevelValue) {
        long time = world.getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        boolean isDay = !isNight;

        switch (this.lightLevel) {
            case "day": if (!isDay) return false; break;
            case "night": if (!isNight) return false; break;
        }

        if (requireDarkness && lightLevelValue > darknessLevel) return false;
        return true;
    }

    public boolean canBurnInDaylight() { return canBurnInDaylight; }
    public boolean isRequirePlayerNearby() { return requirePlayerNearby; }
    public int getMinPlayersNearby() { return minPlayersNearby; }
    public int getPlayerDistance() { return playerDistance; }
    public Set<String> getAllowedBiomes() { return biomes; }
    public List<String> getAllowedWorlds() { return worlds.stream().collect(Collectors.toList()); }

    // Геттеры для настроек моба
    public double getHealth() { return health; }
    public double getDamagePerSecond() { return damagePerSecond; }
    public double getGrabRange() { return grabRange; }
    public int getWeaknessDuration() { return weaknessDuration; }
    public double getNormalSpeed() { return normalSpeed; }
    public double getCarryingSpeed() { return carryingSpeed; }
}