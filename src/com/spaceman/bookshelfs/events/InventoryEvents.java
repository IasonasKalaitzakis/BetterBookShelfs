package com.spaceman.bookshelfs.events;

import com.spaceman.bookshelfs.Main;
import com.spaceman.bookshelfs.Pair;
import com.spaceman.bookshelfs.State;
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
import org.bukkit.inventory.meta.ItemMeta;

import static com.spaceman.bookshelfs.Main.*;

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
        Pair<Location, Pair<State, String>> pair = Main.viewers.remove(player.getUniqueId());
        Main.inventories.put(pair.getLeft(), new Pair<>(inventory, pair.getRight()));
        
    }
    
    public static <T> T getOrDefault(T object, T def) {
        return object != null ? object : def;
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void event(InventoryCloseEvent e) {
        
        if (e.getView().getTitle().equals(NAME)) {
            saveInventory(e.getInventory(), (Player) e.getPlayer());
            viewers.remove(e.getPlayer().getUniqueId());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void event(InventoryClickEvent e) {
        
        if (e.getView().getTitle().equals(NAME)) {
            if (e.getRawSlot() == -999) {
                return;
            }
            
            if (!Main.viewers.containsKey(e.getWhoClicked().getUniqueId())) {
                return;
            }
            Location l = Main.viewers.get(e.getWhoClicked().getUniqueId()).getLeft();
            String lock = Main.viewers.get(e.getWhoClicked().getUniqueId()).getRight().getRight();
            State state = Main.viewers.get(e.getWhoClicked().getUniqueId()).getRight().getLeft();
            Main.inventories.put(l, new Pair<>(e.getInventory(), new Pair<>(state, lock)));
            
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
            
        } else if (e.getView().getTitle().equals(NAME + " (view only)")) {
            
            switch (e.getAction()) {
                case NOTHING:
                case DROP_ONE_CURSOR:
                case DROP_ALL_CURSOR:
                case SWAP_WITH_CURSOR:
                case PLACE_ONE:
                case PLACE_SOME:
                case UNKNOWN:
                case CLONE_STACK:
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD:
                case DROP_ONE_SLOT:
                case DROP_ALL_SLOT:
                case PLACE_ALL:
                case PICKUP_ONE:
                case PICKUP_HALF:
                case PICKUP_SOME:
                    if (e.getRawSlot() < SHELF_SIZE) {
                        e.setCancelled(true);
                    }
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                case COLLECT_TO_CURSOR:
                    e.setCancelled(true);
                case PICKUP_ALL:
                    
                    if (e.getRawSlot() < SHELF_SIZE) {
                        ItemStack is = e.getCurrentItem();
                        if (is != null) {
                            if (is.getType().equals(Material.WRITTEN_BOOK)) {
                                if (is.hasItemMeta()) {
                                    ItemMeta im = is.getItemMeta();
                                    if (im.hasLore()) {
                                        if (im.getLore().contains("Click to copy book")) {
                                            e.setCursor(is);
                                        }
                                    }
                                }
                            }
                        }
                        e.setCancelled(true);
                    }
                    break;
            }
            
            
        }
    }
}
