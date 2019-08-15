package com.spaceman.bookshelfs;

import com.spaceman.bookshelfs.events.BlockEvents;
import com.spaceman.bookshelfs.events.InventoryEvents;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.spaceman.bookshelfs.events.BlockEvents.openBookshelf;

public class Main extends JavaPlugin {
    
    public static final int SHELF_SIZE = 18;//this number must be a multiple of 9, example: 2 rows * 9 = 18
    public static final String NAME = ChatColor.DARK_GRAY + "Book Shelf";
    //location of shelf, inventory of shelf, state of shelf, lock of shelf
    public static HashMap<Location, Pair<Inventory, Pair<State, String>>> inventories = new HashMap<>();
    //viewer UUID, location of shelf, state of shelf, lock of shelf
    public static HashMap<UUID, Pair<Location, Pair<State, String>>> viewers = new HashMap<>();
    
    @Override
    public void onEnable() {
        
        /*
         * changelog
         *
         *  removed test log
         * */
        
        /*
         * todo
         *
         *
         * */
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockEvents(), this);
        pm.registerEvents(new InventoryEvents(), this);
    
        if (this.getConfig().contains("inv")) {
            for (String s : this.getConfig().getConfigurationSection("inv").getKeys(false)) {
                Location l = getLocation("inv." + s + ".l");
                Inventory i = getInventory("inv." + s + ".i");
                String lock = getLock("inv." + s + ".lock");
                State state = getState("inv." + s + ".state");
                inventories.put(l, new Pair<>(i, new Pair<>(state, lock)));
            }
        }
        if (this.getConfig().contains("view")) {
            for (String uuid : this.getConfig().getConfigurationSection("view").getKeys(false)) {
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                if (player != null && player.isOnline()) {
                    Location l = getLocation("view." + uuid + ".l");
                    String lock = getLock("view." + uuid + ".lock");
                    State state = getState("view." + uuid + ".state");
                    viewers.put(UUID.fromString(uuid), new Pair<>(l, new Pair<>(state, lock)));
                
                    openBookshelf(player, l);
                }
            }
        }
    }
    
    @Override
    public void onDisable() {
        
        this.getConfig().set("inv", null);
        this.getConfig().set("view", null);
        
        for (UUID u : viewers.keySet()) {
            saveLocation("view." + u + ".l", viewers.get(u).getLeft());
            saveLock("view." + u + ".lock", viewers.get(u).getRight().getRight());
            saveState("view." + u + ".state", viewers.get(u).getRight().getLeft());
            Player player = Bukkit.getPlayer(u);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
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
                boolean saveL = false;
                
                if (!isEmpty(inventories.get(l).getLeft())) {
                    saveInventory("inv." + i + ".i", inventories.get(l).getLeft());
                    saveL = true;
                }
                if (inventories.get(l).getRight().getRight() != null) {
                    saveLock("inv." + i + ".lock", inventories.get(l).getRight().getRight());
                    saveL = true;
                }
                if (inventories.get(l).getRight().getLeft() != State.OPEN) {
                    saveState("inv." + i + ".state", inventories.get(l).getRight().getLeft());
                    saveL = true;
                }
                
                if (saveL) {
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
    
    private Inventory getInventory(String path) {
        Inventory inv = Bukkit.createInventory(null, 18, NAME);
        
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
    
    private State getState(String path) {
        if (!getConfig().contains(path)) {
            return State.OPEN;
        }
        return State.valueOf(getConfig().getString(path));
    }
    
    private void saveState(String path, State state) {
        getConfig().set(path, state.name());
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
        World world = Bukkit.getWorld(Objects.requireNonNull(getConfig().getString(path + ".world")));
        if (world == null) {
            return null;
        }
        double x = getConfig().getDouble(path + ".x");
        double y = getConfig().getDouble(path + ".y");
        double z = getConfig().getDouble(path + ".z");
        
        return new Location(world, x, y, z);
    }
}
