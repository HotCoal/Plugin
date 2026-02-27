package com.skillplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;
import com.skillplugin.SkillPlugin;
import com.skillplugin.menu.MainMenu;

public class CompassListener implements Listener {

    private final SkillPlugin plugin;

    public CompassListener(SkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveCompass(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveCompass(event.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(item -> isSkillCompass(item));
    }

    private void giveCompass(Player player) {
        if (hasCompass(player)) return;

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lМеню");
            compass.setItemMeta(meta);
        }

        player.getInventory().setItem(8, compass);
    }

    private boolean hasCompass(Player player) {
        ItemStack item = player.getInventory().getItem(8);
        return item != null && item.getType() == Material.COMPASS &&
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§6§lМеню");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            ItemMeta meta = event.getItem().getItemMeta();
            if (meta != null && meta.getDisplayName().equals("§6§lМеню")) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    new MainMenu(plugin).openMenu(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSkillCompass(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (isSkillCompass(event.getOffHandItem()) || isSkillCompass(event.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    private boolean isSkillCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getDisplayName().equals("§6§lМеню");
    }
}