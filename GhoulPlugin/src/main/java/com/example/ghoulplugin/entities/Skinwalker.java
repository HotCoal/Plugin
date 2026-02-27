package com.example.ghoulplugin.entities;

import com.example.ghoulplugin.GhoulPlugin;
import com.example.ghoulplugin.config.MobSpawnSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Skinwalker implements Listener {

    private static final String MOB_NAME = "§c§lSkinwalker §7[Ур. 50]";

    // Настройки по умолчанию (будут перезаписаны из конфига)
    private static double MAX_HEALTH = 100.0;
    private static double DAMAGE_PER_SECOND = 4.0;
    private static double GRAB_RANGE = 2.0;
    private static int WEAKNESS_DURATION = 600;
    private static double NORMAL_SPEED = 0.35;
    private static double CARRYING_SPEED = 0.09;
    private static double AGGRO_RADIUS = 50.0;

    private static double MAGIC_DAMAGE_PER_TICK = DAMAGE_PER_SECOND / 20.0;

    private static final Set<UUID> ACTIVE_SKINWALKERS = new HashSet<>();
    private static final Map<UUID, UUID> CARRIED_PLAYERS = new HashMap<>(); // Скелет -> Игрок
    private static final Map<UUID, UUID> CAPTURED_BY = new HashMap<>(); // Игрок -> Скелет
    private static final Map<UUID, Integer> DAMAGE_TASKS = new HashMap<>();
    private static GhoulPlugin plugin = GhoulPlugin.getInstance();

    // Перегруженный метод для спавна без настроек (для яиц)
    public static Skeleton spawn(Location location) {
        // Создаем настройки по умолчанию
        MobSpawnSettings defaultSettings = new MobSpawnSettings(null, null);
        return spawn(location, defaultSettings);
    }

    // Основной метод спавна с настройками
    public static Skeleton spawn(Location location, MobSpawnSettings settings) {
        World world = location.getWorld();

        // Загружаем настройки из конфига
        MAX_HEALTH = settings.getHealth();
        DAMAGE_PER_SECOND = settings.getDamagePerSecond();
        GRAB_RANGE = settings.getGrabRange();
        WEAKNESS_DURATION = settings.getWeaknessDuration();
        NORMAL_SPEED = settings.getNormalSpeed();
        CARRYING_SPEED = settings.getCarryingSpeed();
        MAGIC_DAMAGE_PER_TICK = DAMAGE_PER_SECOND / 20.0;

        Skeleton skinwalker = world.spawn(location, Skeleton.class, entity -> {
            entity.customName(Component.text(MOB_NAME));
            entity.setCustomNameVisible(true);
            ACTIVE_SKINWALKERS.add(entity.getUniqueId());

            var healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(MAX_HEALTH);
            }
            entity.setHealth(MAX_HEALTH);

            var speedAttr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(NORMAL_SPEED);
            }

            entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    PotionEffect.INFINITE_DURATION,
                    1,
                    false,
                    false,
                    true
            ));

            if (entity.getEquipment() != null) {
                entity.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
                entity.getEquipment().setHelmetDropChance(0);
                entity.getEquipment().setItemInMainHand(null);
                entity.getEquipment().setItemInMainHandDropChance(0);
            }

            entity.setRemoveWhenFarAway(false);
        });

        // Запускаем поведение
        startBehavior(skinwalker);

        plugin.getLogger().info("Skinwalker spawned at " + location.getWorld().getName() +
                " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() +
                " with damage: " + DAMAGE_PER_SECOND + " HP/s");

        return skinwalker;
    }

    private static void startBehavior(Skeleton skinwalker) {
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (skinwalker == null || !skinwalker.isValid() || skinwalker.isDead()) {
                    ACTIVE_SKINWALKERS.remove(skinwalker.getUniqueId());
                    this.cancel();
                    return;
                }

                // Поиск ближайшего игрока
                Player nearestPlayer = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Player player : skinwalker.getWorld().getPlayers()) {
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;

                    double distance = player.getLocation().distance(skinwalker.getLocation());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestPlayer = player;
                    }
                }

                // Если игрок найден и скинволкер никого не несет
                if (nearestPlayer != null && !CARRIED_PLAYERS.containsKey(skinwalker.getUniqueId())) {
                    // Проверяем дистанцию для захвата
                    if (nearestDistance <= GRAB_RANGE) {
                        // Захватываем игрока
                        carryPlayer(skinwalker, nearestPlayer);
                    }
                }

                // Если скинволкер несет игрока, наносим урон
                if (CARRIED_PLAYERS.containsKey(skinwalker.getUniqueId())) {
                    UUID playerId = CARRIED_PLAYERS.get(skinwalker.getUniqueId());
                    Player carriedPlayer = (Player) getEntityById(playerId);

                    if (carriedPlayer != null && carriedPlayer.isValid() && !carriedPlayer.isDead()) {

                        // Получаем позицию скелета
                        Location skeleLoc = skinwalker.getLocation();

                        // Рассчитываем позицию игрока ПЕРЕД лицом скелета
                        Vector direction = skeleLoc.getDirection().normalize();
                        Location carryLoc = skeleLoc.clone().add(direction.multiply(1.5)).add(0, 1.0, 0);

                        // Вычисляем направление от игрока к скелету
                        Vector lookAt = skeleLoc.toVector().subtract(carryLoc.toVector()).normalize();

                        // Устанавливаем направление взгляда игрока на скелета
                        carryLoc.setDirection(lookAt);

                        // Телепортируем игрока
                        carriedPlayer.teleport(carryLoc);

                        // НАНОСИМ МАГИЧЕСКИЙ УРОН (каждый тик)
                        carriedPlayer.damage(MAGIC_DAMAGE_PER_TICK);

                        // КРАСНЫЕ ЧАСТИЦЫ
                        if (tick % 2 == 0) {
                            carriedPlayer.getWorld().spawnParticle(
                                    Particle.DUST,
                                    carriedPlayer.getLocation().add(0, 1, 0),
                                    15,
                                    0.4, 0.4, 0.4,
                                    new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 0, 0), 1.5f)
                            );

                            carriedPlayer.getWorld().spawnParticle(
                                    Particle.DUST,
                                    carriedPlayer.getLocation().add(0, 0.5, 0),
                                    10,
                                    0.5, 0.3, 0.5,
                                    new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 0, 0), 1)
                            );
                        }

                        // Частицы крови
                        if (tick % 5 == 0) {
                            carriedPlayer.getWorld().spawnParticle(
                                    Particle.BLOCK_CRUMBLE,
                                    carriedPlayer.getLocation().add(0, 1, 0),
                                    8,
                                    0.3, 0.3, 0.3,
                                    Material.REDSTONE_BLOCK.createBlockData()
                            );
                        }

                        // ЗВУКИ ПОЖИРАНИЯ
                        if (tick % 10 == 0) {
                            carriedPlayer.getWorld().playSound(
                                    carriedPlayer.getLocation(),
                                    Sound.ENTITY_GENERIC_EAT,
                                    0.7f,
                                    0.8f
                            );
                        }

                        if (tick % 20 == 0) {
                            carriedPlayer.getWorld().playSound(
                                    carriedPlayer.getLocation(),
                                    Sound.ENTITY_PLAYER_BURP,
                                    0.5f,
                                    1.2f
                            );

                            carriedPlayer.getWorld().playSound(
                                    carriedPlayer.getLocation(),
                                    Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                                    0.3f,
                                    1.5f
                            );

                            // Обновляем слабость
                            carriedPlayer.addPotionEffect(new PotionEffect(
                                    PotionEffectType.WEAKNESS,
                                    WEAKNESS_DURATION,
                                    0,
                                    false,
                                    false,
                                    true
                            ));
                        }

                        // Отладка - выводим урон раз в секунду
                        if (tick % 20 == 0) {
                            plugin.getLogger().info("Skinwalker deals " + DAMAGE_PER_SECOND +
                                    " damage to " + carriedPlayer.getName() +
                                    " (health: " + String.format("%.1f", carriedPlayer.getHealth()) + ")");
                        }

                    } else {
                        releasePlayer(skinwalker.getUniqueId(), playerId);
                    }
                }

                tick++;
                if (tick >= 6000) tick = 0;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void carryPlayer(Skeleton skinwalker, Player player) {
        UUID skinwalkerId = skinwalker.getUniqueId();
        UUID playerId = player.getUniqueId();

        CARRIED_PLAYERS.put(skinwalkerId, playerId);
        CAPTURED_BY.put(playerId, skinwalkerId);

        AttributeInstance speedAttr = skinwalker.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(CARRYING_SPEED);
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS,
                WEAKNESS_DURATION,
                0,
                false,
                false,
                true
        ));

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                1.0f,
                0.7f
        );

        player.getWorld().spawnParticle(
                Particle.DUST,
                player.getLocation().add(0, 1, 0),
                40,
                0.6, 0.6, 0.6,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 0, 0), 2)
        );

        plugin.getLogger().info("Skinwalker captured " + player.getName());
    }

    private static void releasePlayer(UUID skinwalkerId, UUID playerId) {
        CARRIED_PLAYERS.remove(skinwalkerId);
        CAPTURED_BY.remove(playerId);

        Skeleton skinwalker = (Skeleton) getEntityById(skinwalkerId);
        if (skinwalker != null && skinwalker.isValid()) {
            AttributeInstance speedAttr = skinwalker.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(NORMAL_SPEED);
            }
        }

        Player player = (Player) getEntityById(playerId);
        if (player != null && player.isValid()) {
            plugin.getLogger().info("Skinwalker released " + player.getName());
        }
    }

    private static LivingEntity getEntityById(UUID uuid) {
        for (World world : plugin.getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Skeleton &&
                ACTIVE_SKINWALKERS.contains(event.getEntity().getUniqueId())) {

            Skeleton skinwalker = (Skeleton) event.getEntity();
            UUID skinwalkerId = skinwalker.getUniqueId();

            if (event.getTarget() instanceof Player) {
                Player player = (Player) event.getTarget();
                UUID playerId = player.getUniqueId();

                double distance = player.getLocation().distance(skinwalker.getLocation());

                if (CAPTURED_BY.containsKey(playerId) && !CAPTURED_BY.get(playerId).equals(skinwalkerId)) {
                    event.setCancelled(true);
                    return;
                }

                if (distance > AGGRO_RADIUS) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Декоративные физические атаки
        if (event.getDamager() instanceof Skeleton &&
                ACTIVE_SKINWALKERS.contains(event.getDamager().getUniqueId()) &&
                event.getEntity() instanceof Player) {

            event.setCancelled(true);

            Player player = (Player) event.getEntity();

            player.getWorld().spawnParticle(
                    Particle.DUST,
                    player.getLocation().add(0, 1, 0),
                    20,
                    0.5, 0.5, 0.5,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 0, 0), 1)
            );

            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,
                    0.5f,
                    0.9f
            );
        }

        // Разрешаем игрокам атаковать скелета
        if (event.getDamager() instanceof Player &&
                event.getEntity() instanceof Skeleton &&
                ACTIVE_SKINWALKERS.contains(event.getEntity().getUniqueId())) {

            Player player = (Player) event.getDamager();
            Skeleton skinwalker = (Skeleton) event.getEntity();

            if (CAPTURED_BY.containsKey(player.getUniqueId()) &&
                    CAPTURED_BY.get(player.getUniqueId()).equals(skinwalker.getUniqueId())) {
                // Разрешаем урон
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Skeleton &&
                ACTIVE_SKINWALKERS.contains(event.getEntity().getUniqueId())) {

            Skeleton skinwalker = (Skeleton) event.getEntity();
            UUID skinwalkerId = skinwalker.getUniqueId();

            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.BONE, 20));
            event.setDroppedExp(50);

            if (CARRIED_PLAYERS.containsKey(skinwalkerId)) {
                UUID playerId = CARRIED_PLAYERS.get(skinwalkerId);
                releasePlayer(skinwalkerId, playerId);
            }

            ACTIVE_SKINWALKERS.remove(skinwalkerId);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (CAPTURED_BY.containsKey(playerId)) {
            UUID skinwalkerId = CAPTURED_BY.get(playerId);
            releasePlayer(skinwalkerId, playerId);
        }
    }

    public static boolean isSkinwalker(Skeleton skeleton) {
        return ACTIVE_SKINWALKERS.contains(skeleton.getUniqueId());
    }

    public static void clearAll() {
        for (Map.Entry<UUID, Integer> entry : DAMAGE_TASKS.entrySet()) {
            plugin.getServer().getScheduler().cancelTask(entry.getValue());
        }

        ACTIVE_SKINWALKERS.clear();
        CARRIED_PLAYERS.clear();
        CAPTURED_BY.clear();
        DAMAGE_TASKS.clear();
    }
}