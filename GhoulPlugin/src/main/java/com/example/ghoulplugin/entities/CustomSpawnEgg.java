package com.example.ghoulplugin.entities;

import com.example.ghoulplugin.GhoulPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class CustomSpawnEgg {

    private static final NamespacedKey ENTITY_TYPE_KEY = new NamespacedKey("ghoulplugin", "entity_type");

    public static ItemStack createGhoulSpawnEgg() {
        ItemStack egg = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();

        Component name = Component.text()
                .content("⚔ Ghoul Spawn Egg ⚔")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .build();
        meta.displayName(name);

        List<Component> lore = Arrays.asList(
                Component.text("Spawns a custom Ghoul").color(NamedTextColor.GRAY),
                Component.text("Level 5 with chainmail armor").color(NamedTextColor.GRAY),
                Component.text(""),
                Component.text("❖ Abilities:").color(NamedTextColor.DARK_RED),
                Component.text("  • Poison I (10s)").color(NamedTextColor.RED),
                Component.text("  • Nausea I (15s)").color(NamedTextColor.RED),
                Component.text(""),
                Component.text("Health: ").color(NamedTextColor.GRAY)
                        .append(Component.text("30 HP").color(NamedTextColor.GREEN)),
                Component.text("Right-click to spawn").color(NamedTextColor.YELLOW)
        );
        meta.lore(lore);

        meta.getPersistentDataContainer().set(ENTITY_TYPE_KEY, PersistentDataType.STRING, "ghoul");
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        egg.setItemMeta(meta);

        return egg;
    }

    public static ItemStack createELocatorSpawnEgg() {
        ItemStack egg = new ItemStack(Material.ENDERMAN_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();

        Component name = Component.text()
                .content("✦ ELocator Spawn Egg ✦")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .build();
        meta.displayName(name);

        List<Component> lore = Arrays.asList(
                Component.text("Spawns an ELocator").color(NamedTextColor.GRAY),
                Component.text("Level 40 - Dimensional Watcher").color(NamedTextColor.GRAY),
                Component.text(""),
                Component.text("❖ Abilities:").color(NamedTextColor.DARK_PURPLE),
                Component.text("  • Blindness in 3-block radius").color(NamedTextColor.DARK_PURPLE),
                Component.text("  • Magic Damage (3❤/s)").color(NamedTextColor.DARK_PURPLE),
                Component.text("  • Siren sound in 10-block radius").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("❖ Features:").color(NamedTextColor.LIGHT_PURPLE),
                Component.text("  • Floating beacon above head").color(NamedTextColor.LIGHT_PURPLE),
                Component.text("  • Black damage ring").color(NamedTextColor.DARK_GRAY),
                Component.text("  • Immune to water, rain & fire").color(NamedTextColor.GOLD),
                Component.text(""),
                Component.text("Health: ").color(NamedTextColor.GRAY)
                        .append(Component.text("60 HP").color(NamedTextColor.GREEN)),
                Component.text("Right-click to spawn").color(NamedTextColor.YELLOW)
        );
        meta.lore(lore);

        meta.getPersistentDataContainer().set(ENTITY_TYPE_KEY, PersistentDataType.STRING, "elocator");
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        egg.setItemMeta(meta);

        return egg;
    }

    public static ItemStack createSkinwalkerSpawnEgg() {
        ItemStack egg = new ItemStack(Material.SKELETON_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();

        Component name = Component.text()
                .content("⚡ Skinwalker Spawn Egg ⚡")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .build();
        meta.displayName(name);

        List<Component> lore = Arrays.asList(
                Component.text("Spawns a Skinwalker").color(NamedTextColor.GRAY),
                Component.text("Level 50 - Soul Flayer").color(NamedTextColor.GRAY),
                Component.text(""),
                Component.text("❖ Abilities:").color(NamedTextColor.DARK_RED),
                Component.text("  • Speed II (constant)").color(NamedTextColor.RED),
                Component.text("  • Lifts players in the air").color(NamedTextColor.RED),
                Component.text("  • 5❤/sec damage").color(NamedTextColor.RED),
                Component.text(""),
                Component.text("❖ Features:").color(NamedTextColor.GOLD),
                Component.text("  • Steve head").color(NamedTextColor.GOLD),
                Component.text("  • Blood particles").color(NamedTextColor.RED),
                Component.text(""),
                Component.text("Health: ").color(NamedTextColor.GRAY)
                        .append(Component.text("100 HP").color(NamedTextColor.GREEN)),
                Component.text("Drops: ").color(NamedTextColor.GRAY)
                        .append(Component.text("20 Bones").color(NamedTextColor.WHITE)),
                Component.text("Right-click to spawn").color(NamedTextColor.YELLOW)
        );
        meta.lore(lore);

        meta.getPersistentDataContainer().set(ENTITY_TYPE_KEY, PersistentDataType.STRING, "skinwalker");
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        egg.setItemMeta(meta);

        return egg;
    }

    public static boolean isGhoulSpawnEgg(ItemStack item) {
        if (item == null || item.getType() != Material.ZOMBIE_SPAWN_EGG || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(ENTITY_TYPE_KEY, PersistentDataType.STRING);
        return "ghoul".equals(type);
    }

    public static boolean isELocatorSpawnEgg(ItemStack item) {
        if (item == null || item.getType() != Material.ENDERMAN_SPAWN_EGG || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(ENTITY_TYPE_KEY, PersistentDataType.STRING);
        return "elocator".equals(type);
    }

    public static boolean isSkinwalkerSpawnEgg(ItemStack item) {
        if (item == null || item.getType() != Material.SKELETON_SPAWN_EGG || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(ENTITY_TYPE_KEY, PersistentDataType.STRING);
        return "skinwalker".equals(type);
    }

    public static void spawnGhoulFromEgg(Player player) {
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        GhoulEntity.spawn(spawnLoc);

        spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
        spawnLoc.getWorld().spawnParticle(Particle.ENTITY_EFFECT, spawnLoc.clone().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0);
    }

    public static void spawnELocatorFromEgg(Player player) {
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));

        Enderman elocator = ELocator.spawn(spawnLoc);

        if (elocator != null) {
            spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            spawnLoc.getWorld().spawnParticle(Particle.PORTAL, spawnLoc.clone().add(0, 1, 0), 100, 0.5, 1.0, 0.5, 0.5);
            spawnLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, spawnLoc.clone().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
            spawnLoc.getWorld().spawnParticle(Particle.ENTITY_EFFECT, spawnLoc.clone().add(0, 1, 0), 80, 0.7, 0.7, 0.7, 0);
        }
    }

    public static void spawnSkinwalkerFromEgg(Player player) {
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));

        Skeleton skinwalker = Skinwalker.spawn(spawnLoc);

        if (skinwalker != null) {
            spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            // ИСПРАВЛЕНО: REDSTONE -> DUST
            spawnLoc.getWorld().spawnParticle(Particle.DUST, spawnLoc.clone().add(0, 1, 0), 50, 0.5, 0.5, 0.5, new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 0, 0), 1));
            spawnLoc.getWorld().spawnParticle(Particle.SMOKE, spawnLoc.clone().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.01);
        }
    }
}