package com.spaceman.bookshelfs.events;

import com.spaceman.bookshelfs.Main;
import com.spaceman.bookshelfs.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.spaceman.bookshelfs.Main.SHELF_SIZE;
import static com.spaceman.bookshelfs.Main.name;

public class InventoryEvents implements Listener {
    
    static void saveInventory(Inventory inventory, Player player) {
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack is = inventory.getItem(i);
            
            if (is == null)
                continue;
            
            if (!is.getType().equals(Material.BOOK) &&
                    !is.getType().equals(Material.WRITTEN_BOOK) &&
                    !is.getType().equals(Material.WRITABLE_BOOK) &&
                    !is.getType().equals(Material.KNOWLEDGE_BOOK) &&
                    !is.getType().equals(Material.PAPER) &&
                    !is.getType().equals(Material.MAP) &&
                    !is.getType().equals(Material.FILLED_MAP) &&
                    !is.getType().equals(Material.ENCHANTED_BOOK)) {
                
                inventory.setItem(i, null);
                
                for (ItemStack item : player.getInventory().addItem(is).values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
        Pair<Location, String> pair = Main.viewers.remove(player.getUniqueId());
        Main.inventories.put(pair.getLeft(), new Pair<>(inventory, pair.getRight()));
        
    }
    
    public static <T> T getOrDefault(T object, T def) {
        return object != null ? object : def;
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void event(InventoryCloseEvent e) {
        
        if (e.getView().getTitle().equals(name)) {
            saveInventory(e.getInventory(), (Player) e.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void event(InventoryClickEvent e) {
        
        if (e.getView().getTitle().equals(name)) {
            if (e.getRawSlot() == -999) {
                return;
            }
            
            if (!Main.viewers.containsKey(e.getWhoClicked().getUniqueId())) {
                return;
            }
            Location l = Main.viewers.get(e.getWhoClicked().getUniqueId()).getLeft();
            String lock = Main.viewers.get(e.getWhoClicked().getUniqueId()).getRight();
            Main.inventories.put(l, new Pair<>(e.getInventory(), lock));
            
            if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && e.getRawSlot() >= SHELF_SIZE) {
                if (!getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.WRITTEN_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.KNOWLEDGE_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.WRITABLE_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.PAPER) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.MAP) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.FILLED_MAP) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.ENCHANTED_BOOK)) {
                    
                    e.setCancelled(true);
                    return;
                }
            }
            
            if (e.getRawSlot() < SHELF_SIZE) {
                
                if (!Material.BOOK.equals(getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType()) && !Material.BOOK.equals(getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType()) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.WRITTEN_BOOK) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.WRITTEN_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.WRITABLE_BOOK) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.WRITABLE_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.KNOWLEDGE_BOOK) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.KNOWLEDGE_BOOK) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.PAPER) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.PAPER) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.MAP) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.MAP) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.FILLED_MAP) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.FILLED_MAP) &&
                        !getOrDefault(e.getCurrentItem(), new ItemStack(Material.AIR)).getType().equals(Material.ENCHANTED_BOOK) && !getOrDefault(e.getCursor(), new ItemStack(Material.AIR)).getType().equals(Material.ENCHANTED_BOOK)) {
                    
                    e.setCancelled(true);
                }
            }
            
        }
    }
}
