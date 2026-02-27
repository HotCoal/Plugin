package com.skillplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import com.skillplugin.SkillPlugin;
import com.skillplugin.menu.MainMenu;
import com.skillplugin.menu.SkillsMenu;
import com.skillplugin.menu.SkillLevelMenu;

public class MenuListener implements Listener {

    private final SkillPlugin plugin;

    public MenuListener(SkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        // Главное меню
        if (title.equals("§8Главное меню")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            int slot = event.getSlot();

            switch (slot) {
                case 11: // Навыки
                    new SkillsMenu(plugin).openMenu(player);
                    break;

                case 13: // Коллекции - открывает другой плагин
                    player.closeInventory();
                    // Выполняем команду для открытия другого плагина
                    // ЗАМЕНИТЕ ЭТУ КОМАНДУ НА ВАШУ!
                    Bukkit.dispatchCommand(player, "collection");
                    // Пример: Bukkit.dispatchCommand(player, "ec");
                    // Пример: Bukkit.dispatchCommand(player, "menu collections");
                    break;
            }
        }

        // Меню навыков
        else if (title.equals("§8Навыки")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            int slot = event.getSlot();

            if (slot == 11) {
                new SkillLevelMenu(plugin, "combat").openMenu(player, 0);
            } else if (slot == 13) {
                new SkillLevelMenu(plugin, "mining").openMenu(player, 0);
            } else if (slot == 15) {
                new SkillLevelMenu(plugin, "woodcutting").openMenu(player, 0);
            }
        }

        // Меню уровней навыков
        else if (title.startsWith("§8Бой") || title.startsWith("§8Шахтерство") || title.startsWith("§8Рубка")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String displayName = clicked.getItemMeta().getDisplayName();

                // Навигация по страницам
                if (displayName.equals("§a→ Следующая страница")) {
                    String skill = getSkillFromTitle(title);
                    int currentPage = extractPageFromTitle(title);
                    new SkillLevelMenu(plugin, skill).openMenu(player, currentPage + 1);
                }
                else if (displayName.equals("§c← Предыдущая страница")) {
                    String skill = getSkillFromTitle(title);
                    int currentPage = extractPageFromTitle(title);
                    new SkillLevelMenu(plugin, skill).openMenu(player, currentPage - 1);
                }
                else if (displayName.equals("§e↩ Назад")) {
                    new SkillsMenu(plugin).openMenu(player);
                }
                // ПОЛУЧЕНИЕ НАГРАДЫ
                else if (displayName.contains("⚡ ДОСТУПНО")) {
                    String skill = getSkillFromTitle(title);
                    int level = extractLevelFromDisplayName(displayName);

                    if (level > 0) {
                        SkillLevelMenu menu = new SkillLevelMenu(plugin, skill);
                        menu.claimReward(player, level);

                        int currentPage = extractPageFromTitle(title);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            menu.openMenu(player, currentPage);
                        }, 5L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        if (title.equals("§8Главное меню") || title.equals("§8Навыки") ||
                title.startsWith("§8Бой") || title.startsWith("§8Шахтерство") ||
                title.startsWith("§8Рубка")) {
            event.setCancelled(true);
        }
    }

    private String getSkillFromTitle(String title) {
        if (title.startsWith("§8Бой")) return "combat";
        if (title.startsWith("§8Шахтерство")) return "mining";
        if (title.startsWith("§8Рубка")) return "woodcutting";
        return "combat";
    }

    private int extractPageFromTitle(String title) {
        try {
            if (title.contains("(") && title.contains("/")) {
                int start = title.indexOf("(") + 1;
                int end = title.indexOf("/");
                String pagePart = title.substring(start, end).trim();
                return Integer.parseInt(pagePart) - 1;
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    private int extractLevelFromDisplayName(String displayName) {
        try {
            String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
            String[] parts = cleanName.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("Уровень") && i + 1 < parts.length) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
}