package com.example.ghoulplugin.listeners;

import com.example.ghoulplugin.GhoulPlugin;
import com.example.ghoulplugin.entities.CustomEntityRegistry;
import com.example.ghoulplugin.entities.GhoulEntity;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EntityListeners implements Listener {

    private final GhoulPlugin plugin;
    private final Random random = new Random();

    public EntityListeners(GhoulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGhoulAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Zombie zombie &&
                CustomEntityRegistry.isGhoul(zombie)) {

            if (event.getEntity() instanceof Player player) {
                // Apply effects using our GhoulEntity class
                GhoulEntity.applyEffects(player);
            }
        }
    }

    @EventHandler
    public void onGhoulDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Zombie zombie &&
                CustomEntityRegistry.isGhoul(zombie)) {

            // Clear default drops
            event.getDrops().clear();

            // Add custom drops from config
            ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops.ghoul");
            if (drops != null) {
                for (String key : drops.getKeys(false)) {
                    String material = drops.getString(key + ".material");
                    int minAmount = drops.getInt(key + ".min-amount");
                    int maxAmount = drops.getInt(key + ".max-amount");
                    double chance = drops.getDouble(key + ".chance");

                    if (random.nextDouble() <= chance) {
                        int amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
                        try {
                            Material mat = Material.valueOf(material);
                            event.getDrops().add(new ItemStack(mat, amount));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material in config: " + material);
                        }
                    }
                }
            }

            // Add experience
            event.setDroppedExp(10 + random.nextInt(11)); // 10-20 exp
        }
    }
}