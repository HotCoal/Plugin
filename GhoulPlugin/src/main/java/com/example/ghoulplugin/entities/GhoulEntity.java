package com.example.ghoulplugin.entities;

import com.example.ghoulplugin.GhoulPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhoulEntity {

    public static Zombie spawn(Location location) {
        Zombie zombie = location.getWorld().spawn(location, Zombie.class, ghoul -> {
            // Set custom name
            ghoul.setCustomName(ChatColor.RED + "Ghoul");
            ghoul.setCustomNameVisible(true);

            // Mark as ghoul in persistent data
            CustomEntityRegistry.markAsGhoul(ghoul);

            // Set attributes
// Устанавливаем максимальное здоровье через атрибуты с проверкой
            var maxHealthAttr = ghoul.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(30.0);
            }
            ghoul.setHealth(30.0);

            // Set equipment
            var equipment = ghoul.getEquipment();
            if (equipment != null) {
                // Chainmail armor
                equipment.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                equipment.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

                // Wooden sword
                equipment.setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));

                // Set drop chances to 0
                equipment.setHelmetDropChance(0);
                equipment.setChestplateDropChance(0);
                equipment.setLeggingsDropChance(0);
                equipment.setBootsDropChance(0);
                equipment.setItemInMainHandDropChance(0);
            }

            // Make baby zombie? (optional)
            // ghoul.setBaby(false);
        });

        return zombie;
    }

    public static void applyEffects(Player player) {
        // Poison level 1 for 10 seconds (200 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

        // Nausea level 1 for 15 seconds (300 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 300, 0));
    }
}