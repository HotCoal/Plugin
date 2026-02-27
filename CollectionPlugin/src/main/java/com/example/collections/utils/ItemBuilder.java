// utils/ItemBuilder.java
package com.example.collections.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder lore(Component... lore) {
        List<Component> loreList = meta.lore();
        if (loreList == null) loreList = new ArrayList<>();
        loreList.addAll(Arrays.asList(lore));
        meta.lore(loreList);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder skullOwner(Player player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}