package com.example.ghoulplugin.managers;

import com.example.ghoulplugin.GhoulPlugin;
import com.example.ghoulplugin.config.MobSpawnSettings;
import com.example.ghoulplugin.entities.CustomEntityRegistry;
import com.example.ghoulplugin.entities.ELocator;
import com.example.ghoulplugin.entities.GhoulEntity;
import com.example.ghoulplugin.entities.Skinwalker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawnManager {

    private final GhoulPlugin plugin;
    private final Random random = ThreadLocalRandom.current();
    private final Map<String, BukkitTask> spawnTasks = new HashMap<>();
    private final Map<String, MobSpawnSettings> mobSettings = new HashMap<>();

    public MobSpawnManager(GhoulPlugin plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    private void loadSettings() {
        var spawnConfig = plugin.getConfig().getConfigurationSection("mob-spawn-settings");
        var mobConfig = plugin.getConfig().getConfigurationSection("mob-settings");

        if (spawnConfig == null) return;

        for (String mobName : spawnConfig.getKeys(false)) {
            var spawnSection = spawnConfig.getConfigurationSection(mobName);
            var mobSection = mobConfig != null ? mobConfig.getConfigurationSection(mobName) : null;

            if (spawnSection != null) {
                mobSettings.put(mobName, new MobSpawnSettings(spawnSection, mobSection));
            }
        }

        plugin.getLogger().info("Loaded spawn settings for mobs: " + String.join(", ", mobSettings.keySet()));
    }

    public void startSpawning() {
        for (Map.Entry<String, MobSpawnSettings> entry : mobSettings.entrySet()) {
            String mobName = entry.getKey();
            MobSpawnSettings settings = entry.getValue();

            if (!settings.isEnabled()) {
                plugin.getLogger().info("Spawn for " + mobName + " is disabled");
                continue;
            }

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    spawnMobsForAllPlayers(mobName, settings);
                }
            }.runTaskTimer(plugin, random.nextInt(settings.getSpawnFrequency()), settings.getSpawnFrequency());

            spawnTasks.put(mobName, task);
            plugin.getLogger().info("Started spawn task for " + mobName + " (every " + settings.getSpawnFrequency() / 20 + " seconds)");
        }
    }

    private void spawnMobsForAllPlayers(String mobName, MobSpawnSettings settings) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;

            if (!settings.isAllowedWorld(player.getWorld())) continue;

            if (random.nextDouble() > settings.getSpawnChance()) continue;

            int mobsInRadius = countMobsInChunkRadius(mobName, player.getLocation(), settings.getChunkRadius());
            if (mobsInRadius >= settings.getMaxMobsInRadius()) continue;

            trySpawnMobNearPlayer(mobName, settings, player);
        }
    }

    private void trySpawnMobNearPlayer(String mobName, MobSpawnSettings settings, Player player) {
        Location playerLoc = player.getLocation();
        int chunkRadius = settings.getChunkRadius();

        int chunkX = playerLoc.getChunk().getX() + random.nextInt(chunkRadius * 2 + 1) - chunkRadius;
        int chunkZ = playerLoc.getChunk().getZ() + random.nextInt(chunkRadius * 2 + 1) - chunkRadius;

        Chunk targetChunk = player.getWorld().getChunkAt(chunkX, chunkZ);

        Location spawnLoc = generateLocationInChunk(targetChunk, settings, player);
        if (spawnLoc == null) return;

        int lightLevel = spawnLoc.getBlock().getLightLevel();
        if (!settings.canSpawnNow(player.getWorld(), lightLevel)) return;

        spawnMob(mobName, spawnLoc, settings);
    }

    private Location generateLocationInChunk(Chunk chunk, MobSpawnSettings settings, Player player) {
        int attempts = 0;
        int maxAttempts = 10;

        while (attempts < maxAttempts) {
            int x = (chunk.getX() << 4) + random.nextInt(16);
            int z = (chunk.getZ() << 4) + random.nextInt(16);
            int y = chunk.getWorld().getHighestBlockYAt(x, z);

            if (!settings.isAllowedYLevel(y)) {
                attempts++;
                continue;
            }

            Location loc = new Location(chunk.getWorld(), x + 0.5, y, z + 0.5);

            String biomeKey = loc.getBlock().getBiome().getKey().toString();
            if (!settings.isAllowedBiome(biomeKey)) {
                attempts++;
                continue;
            }

            if (!isSafeLocation(loc)) {
                attempts++;
                continue;
            }

            return loc;
        }

        return null;
    }

    private boolean isSafeLocation(Location loc) {
        var below = loc.clone().subtract(0, 1, 0).getBlock();
        if (below.isLiquid() || below.isEmpty()) return false;

        var head = loc.clone().add(0, 1, 0).getBlock();
        if (!head.isEmpty() && !head.isLiquid()) return false;

        return true;
    }

    private int countMobsInChunkRadius(String mobName, Location center, int radius) {
        int count = 0;
        World world = center.getWorld();
        int centerChunkX = center.getChunk().getX();
        int centerChunkZ = center.getChunk().getZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = world.getChunkAt(centerChunkX + dx, centerChunkZ + dz);
                count += getMobsInChunk(mobName, chunk);
            }
        }

        return count;
    }

    private int getMobsInChunk(String mobName, Chunk chunk) {
        int count = 0;

        for (Entity entity : chunk.getEntities()) {
            switch (mobName.toLowerCase()) {
                case "ghoul":
                    if (entity instanceof Zombie && CustomEntityRegistry.isGhoul((Zombie) entity)) {
                        count++;
                    }
                    break;
                case "elocator":
                    if (entity instanceof Enderman && ELocator.isELocator((Enderman) entity)) {
                        count++;
                    }
                    break;
                case "skinwalker":
                    if (entity instanceof Skeleton && Skinwalker.isSkinwalker((Skeleton) entity)) {
                        count++;
                    }
                    break;
            }
        }

        return count;
    }

    private void spawnMob(String mobName, Location loc, MobSpawnSettings settings) {
        switch (mobName.toLowerCase()) {
            case "ghoul":
                GhoulEntity.spawn(loc);
                plugin.getLogger().fine("Spawned Ghoul at " + loc.getWorld().getName() + " " +
                        loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
                break;
            case "elocator":
                ELocator.spawn(loc);
                plugin.getLogger().fine("Spawned ELocator at " + loc.getWorld().getName() + " " +
                        loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
                break;
            case "skinwalker":
                Skinwalker.spawn(loc, settings); // Передаем настройки
                break;
            default:
                plugin.getLogger().warning("Unknown mob type: " + mobName);
        }
    }

    public void stopSpawning() {
        for (BukkitTask task : spawnTasks.values()) {
            task.cancel();
        }
        spawnTasks.clear();
    }

    public void reloadSettings() {
        stopSpawning();
        plugin.reloadConfig();
        mobSettings.clear();
        loadSettings();
        startSpawning();
    }
}