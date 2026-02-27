package com.example.ghoulplugin.entities;

import com.example.ghoulplugin.GhoulPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ELocator implements Listener {

    private static final String MOB_NAME = "§5§lELocator §7[Ур. 40]";
    private static final double MAX_HEALTH = 60.0;
    private static final double DAMAGE_RADIUS = 3.0;
    private static final double SIREN_RADIUS = 10.0;
    private static final double DAMAGE_PER_SECOND = 3.0;
    private static final int BLINDNESS_DURATION = 100; // 5 секунд

    private static final Set<UUID> ACTIVE_ELOCATORS = new HashSet<>();

    public static Enderman spawn(Location location) {
        World world = location.getWorld();

        Enderman elocator = world.spawn(location, Enderman.class, entity -> {
            // Имя
            entity.customName(Component.text(MOB_NAME));
            entity.setCustomNameVisible(true);

            // Маркировка
            ACTIVE_ELOCATORS.add(entity.getUniqueId());

            // Здоровье
            var healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(MAX_HEALTH);
            }
            entity.setHealth(MAX_HEALTH);

            // Отключение AI
            entity.setAI(false);
            entity.setRemoveWhenFarAway(false);
            entity.setTarget(null);

            // Иммунитеты
            entity.setRemainingAir(Integer.MAX_VALUE);
            entity.setFireTicks(0);

            // Декоративный предмет - для эндермена можно оставить
            if (entity.getEquipment() != null) {
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.END_ROD));
                entity.getEquipment().setItemInMainHandDropChance(0); // Это для эндермена OK
            }

            // Начальный поворот
            Location loc = entity.getLocation();
            loc.setYaw(90);
            loc.setPitch(0);
            entity.teleport(loc);
        });

        // Создаем маяк над головой
        createBeacon(elocator);

        // Запускаем поведение
        new BukkitRunnable() {
            @Override
            public void run() {
                if (elocator != null && elocator.isValid() && !elocator.isDead()) {
                    startBehavior(elocator);
                }
            }
        }.runTaskLater(GhoulPlugin.getInstance(), 5L);

        return elocator;
    }

    private static void createBeacon(Enderman elocator) {
        Location beaconLoc = elocator.getLocation().clone().add(0, 3.2, 0);

        ArmorStand beaconStand = beaconLoc.getWorld().spawn(beaconLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCollidable(false);

            // Убираем установку drop chance для ArmorStand - это вызывает ошибку
            stand.getEquipment().setHelmet(new ItemStack(Material.BEACON));
            // Не вызываем setHelmetDropChance для ArmorStand!
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (elocator == null || !elocator.isValid() || elocator.isDead()) {
                    if (beaconStand != null && beaconStand.isValid()) {
                        beaconStand.remove();
                    }
                    this.cancel();
                    return;
                }

                // Обновляем позицию маяка
                Location newLoc = elocator.getLocation().clone().add(0, 3.2, 0);
                beaconStand.teleport(newLoc);

                // Частицы вокруг маяка
                newLoc.getWorld().spawnParticle(
                        Particle.PORTAL,
                        newLoc.clone().add(0, 0.5, 0),
                        5,
                        0.2, 0.2, 0.2,
                        0.1
                );
            }
        }.runTaskTimer(GhoulPlugin.getInstance(), 0L, 1L);
    }

    private static void startBehavior(Enderman elocator) {
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (elocator == null || !elocator.isValid() || elocator.isDead()) {
                    ACTIVE_ELOCATORS.remove(elocator.getUniqueId());
                    this.cancel();
                    return;
                }

                // 1. ВРАЩЕНИЕ
                Location loc = elocator.getLocation().clone();
                loc.setYaw(loc.getYaw() + 2);
                loc.setPitch(0);
                elocator.teleport(loc);

                // 2. ПОИСК ИГРОКОВ
                for (Player player : elocator.getWorld().getPlayers()) {
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;

                    double distance = player.getLocation().distance(elocator.getLocation());

                    // 3. СЛЕПОТА
                    if (distance <= DAMAGE_RADIUS) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.BLINDNESS,
                                BLINDNESS_DURATION,
                                0,
                                false,
                                false,
                                true
                        ));

                        // 4. УРОН
                        if (tick % 20 == 0) {
                            player.damage(DAMAGE_PER_SECOND, elocator);

                            player.getWorld().playSound(
                                    player.getLocation(),
                                    Sound.ENTITY_WITCH_HURT,
                                    0.5f,
                                    1.5f
                            );
                        }

                        // Визуальный эффект на игроке - ИСПРАВЛЕНО
                        player.getWorld().spawnParticle(
                                Particle.ENTITY_EFFECT,
                                player.getLocation().add(0, 1, 0),
                                10,
                                0.3, 0.3, 0.3,
                                0.5
                        );
                    }

                    // 5. СИРЕНА
                    if (distance <= SIREN_RADIUS && tick % 40 == 0) {
                        float volume = (float) (1.0f - (distance / SIREN_RADIUS) * 0.5f);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, volume, 1.0f);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, volume * 0.7f, 1.2f);
                    }
                }

                // 6. КОЛЬЦО ЧАСТИЦ - ИСПРАВЛЕНО
                Location center = elocator.getLocation().clone().add(0, 1, 0);
                for (int i = 0; i < 360; i += 30) {
                    double rad = Math.toRadians(i);
                    double x = center.getX() + DAMAGE_RADIUS * Math.cos(rad);
                    double z = center.getZ() + DAMAGE_RADIUS * Math.sin(rad);

                    Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);

                    // Фиолетовые частицы
                    center.getWorld().spawnParticle(
                            Particle.ENTITY_EFFECT,
                            ringLoc,
                            1,
                            0, 0, 0,
                            0.5
                    );

                    // Дым
                    center.getWorld().spawnParticle(
                            Particle.SMOKE,
                            ringLoc,
                            1,
                            0, 0, 0,
                            0.01
                    );
                }

                tick++;
                if (tick >= 6000) tick = 0;
            }
        }.runTaskTimer(GhoulPlugin.getInstance(), 0L, 1L);
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Enderman &&
                ACTIVE_ELOCATORS.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public static boolean isELocator(Enderman enderman) {
        return ACTIVE_ELOCATORS.contains(enderman.getUniqueId());
    }

    public static void clearAll() {
        ACTIVE_ELOCATORS.clear();
    }
}