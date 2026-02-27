/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.entity.EntityDeathEvent
 */
package com.skillplugin.listeners;

import com.skillplugin.SkillPlugin;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class SkillListener
implements Listener {
    private final SkillPlugin plugin;
    private final Set<Material> ores;
    private final Set<Material> logs;

    public SkillListener(SkillPlugin plugin) {
        this.plugin = plugin;
        this.ores = new HashSet<Material>();
        this.ores.add(Material.COAL_ORE);
        this.ores.add(Material.DEEPSLATE_COAL_ORE);
        this.ores.add(Material.IRON_ORE);
        this.ores.add(Material.DEEPSLATE_IRON_ORE);
        this.ores.add(Material.COPPER_ORE);
        this.ores.add(Material.DEEPSLATE_COPPER_ORE);
        this.ores.add(Material.GOLD_ORE);
        this.ores.add(Material.DEEPSLATE_GOLD_ORE);
        this.ores.add(Material.REDSTONE_ORE);
        this.ores.add(Material.DEEPSLATE_REDSTONE_ORE);
        this.ores.add(Material.LAPIS_ORE);
        this.ores.add(Material.DEEPSLATE_LAPIS_ORE);
        this.ores.add(Material.DIAMOND_ORE);
        this.ores.add(Material.DEEPSLATE_DIAMOND_ORE);
        this.ores.add(Material.EMERALD_ORE);
        this.ores.add(Material.DEEPSLATE_EMERALD_ORE);
        this.ores.add(Material.NETHER_QUARTZ_ORE);
        this.ores.add(Material.NETHER_GOLD_ORE);
        this.ores.add(Material.ANCIENT_DEBRIS);
        this.logs = new HashSet<Material>();
        this.logs.add(Material.OAK_LOG);
        this.logs.add(Material.SPRUCE_LOG);
        this.logs.add(Material.BIRCH_LOG);
        this.logs.add(Material.JUNGLE_LOG);
        this.logs.add(Material.ACACIA_LOG);
        this.logs.add(Material.DARK_OAK_LOG);
        this.logs.add(Material.MANGROVE_LOG);
        this.logs.add(Material.CHERRY_LOG);
        this.logs.add(Material.CRIMSON_STEM);
        this.logs.add(Material.WARPED_STEM);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        if (this.ores.contains(blockType)) {
            this.plugin.getSkillManager().addMiningXP(player, 10);
        } else if (this.logs.contains(blockType)) {
            this.plugin.getSkillManager().addWoodcuttingXP(player, 10);
        } else if (!blockType.isAir() && blockType.isBlock()) {
            this.plugin.getSkillManager().addMiningXP(player, 1);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntityType();
            int xp = this.getCombatXPForEntity(entityType);
            if (xp > 0) {
                this.plugin.getSkillManager().addCombatXP(player, xp);
            }
        }
    }

    private int getCombatXPForEntity(EntityType type) {
        switch (type) {
            case ZOMBIE: 
            case SKELETON: 
            case SPIDER: 
            case CAVE_SPIDER: {
                return 5;
            }
            case CREEPER: {
                return 8;
            }
            case ENDERMAN: {
                return 15;
            }
            case BLAZE: {
                return 20;
            }
            case WITHER_SKELETON: {
                return 25;
            }
        }
        return 2;
    }
}

