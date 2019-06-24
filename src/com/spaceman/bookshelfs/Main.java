package com.spaceman.bookshelfs;

import com.spaceman.bookshelfs.events.BlockEvents;
import com.spaceman.bookshelfs.events.InventoryEvents;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
    
    public static final int SHELF_SIZE = 18;//this number must be a multiple of 9, example: 2 rows * 9 = 18
    
    public static HashMap<Location, Pair<Inventory, String>> inventories = new HashMap<>();
    public static HashMap<UUID, Pair<Location, String>> viewers = new HashMap<>();
    
    @Override
    public void onEnable() {
        
        /*
        * changelog
        *
        * you can now open locked bookshelfs while carrying the key anywhere in your inventory,
        * even when its in a shulkerbox
        *
        * */
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockEvents(), this);
        pm.registerEvents(new InventoryEvents(), this);
        
        if (this.getConfig().contains("view")) {
            for (String uuid : this.getConfig().getConfigurationSection("view").getKeys(false)) {
                Location l = getLocation("view.l." + uuid);
                String lock = getLock("view.lock." + uuid);
                viewers.put(UUID.fromString(uuid), new Pair<>(l, lock));
            }
        }
        if (this.getConfig().contains("inv")) {
            for (String s : this.getConfig().getConfigurationSection("inv").getKeys(false)) {
                Location l = getLocation("inv." + s + ".l");
                Inventory i = getInventory("inv." + s + ".i");
                String lock = getLock("inv." + s + ".lock");
                inventories.put(l, new Pair<>(i, lock));
            }
        }
    }
    
    @Override
    public void onDisable() {
        
        this.getConfig().set("inv", null);
        this.getConfig().set("view", null);
        
        if (inventories == null || inventories.isEmpty()) {
            saveConfig();
            return;
        }
        
        for (UUID u : viewers.keySet()) {
            saveLocation("view.l." + u, viewers.get(u).getLeft());
            saveLock("view.lock." + u, viewers.get(u).getRight());
        }
        
        int i = 0;
        for (Location l : inventories.keySet()) {
            
            if (!l.getWorld().getBlockAt(l).getType().equals(Material.BOOKSHELF)) {
                Inventory inv = Main.inventories.get(l).getLeft();
                
                l.setY(l.getY() + 0.5);
                
                for (ItemStack is : inv.getContents()) {
                    if (is == null) {
                        continue;
                    }
                    l.getWorld().dropItemNaturally(l, is);
                }
            } else {
                boolean empty = !isEmpty(inventories.get(l).getLeft());
                if (empty) {
                    saveInventory("inv." + i + ".i", inventories.get(l).getLeft());
                }
                if (inventories.get(l).getRight() != null || empty) {
                    saveLock("inv." + i + ".lock", inventories.get(l).getRight());
                    saveLocation("inv." + i + ".l", l);
                }
            }
            i++;
        }
        this.saveConfig();
    }
    
    private boolean isEmpty(Inventory inventory) {
        if (inventory == null) {
            return true;
        }
        for (ItemStack is : inventory.getContents()) {
            if (is != null) {
                return false;
            }
        }
        return true;
    }
    
    public static final String name = ChatColor.DARK_GRAY + "Book Shelf";
    
    private Inventory getInventory(String path) {
        Inventory inv = Bukkit.createInventory(null, 18, name);
        
        if (!getConfig().contains(path)) {
            return inv;
        }
        
        List<?> content = getConfig().getList(path + ".content");
        if (content != null) {
            for (int i = 0; i < content.size(); i++) {
                Object o = content.get(i);
                if (o != null) {
                    if (o instanceof ItemStack) {
                        ItemStack item = (ItemStack) o;
                        inv.setItem(i, item);
                    }
                }
            }
        }
        return inv;
    }
    
    private String getLock(String path) {
        if (!getConfig().contains(path)) {
            return null;
        }
        return getConfig().getString(path);
    }
    private void saveLock(String path, String lock) {
        getConfig().set(path, lock);
    }
    
    private void saveInventory(String path, Inventory inventory) {
        getConfig().set(path + ".content", inventory.getContents());
    }
    
    private void saveLocation(String path, Location location) {
        getConfig().set(path + ".world", location.getWorld().getName());
        getConfig().set(path + ".x", location.getX());
        getConfig().set(path + ".y", location.getY());
        getConfig().set(path + ".z", location.getZ());
    }
    
    private Location getLocation(String path) {
        
        if (!getConfig().contains(path)) {
            return null;
        }
        World world = Bukkit.getWorld(getConfig().getString(path + ".world"));
        if (world == null) {
            return null;
        }
        double x = getConfig().getDouble(path + ".x");
        double y = getConfig().getDouble(path + ".y");
        double z = getConfig().getDouble(path + ".z");
        
        return new Location(world, x, y, z);
    }
}
