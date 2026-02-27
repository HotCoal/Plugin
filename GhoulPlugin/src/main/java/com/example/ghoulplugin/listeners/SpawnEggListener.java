package com.example.ghoulplugin.listeners;

import com.example.ghoulplugin.entities.CustomSpawnEgg;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SpawnEggListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();

        if (CustomSpawnEgg.isGhoulSpawnEgg(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            CustomSpawnEgg.spawnGhoulFromEgg(player);
            player.sendMessage(ChatColor.GREEN + "Spawned a Ghoul!");
        }
        else if (CustomSpawnEgg.isELocatorSpawnEgg(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            CustomSpawnEgg.spawnELocatorFromEgg(player);
            player.sendMessage(ChatColor.DARK_PURPLE + "Spawned an ELocator!");
        }
        else if (CustomSpawnEgg.isSkinwalkerSpawnEgg(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            CustomSpawnEgg.spawnSkinwalkerFromEgg(player);
            player.sendMessage(ChatColor.RED + "Spawned a Skinwalker!");
        }
    }
}