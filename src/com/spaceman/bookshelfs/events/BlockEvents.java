package com.spaceman.bookshelfs.events;

import com.spaceman.bookshelfs.Main;
import com.spaceman.bookshelfs.Pair;
import com.spaceman.bookshelfs.State;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.spaceman.bookshelfs.Main.*;

public class BlockEvents implements Listener {
    
    public boolean hasLock(String lock, Player player) {
        
        if (lock == null) {
            return true;
        }
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            if (seekShulkerBox(lock, item)) {
                return true;
            }
            
            if (!item.hasItemMeta()) {
                continue;
            }
            ItemMeta im = item.getItemMeta();
            if (im == null || !im.hasDisplayName()) {
                continue;
            }
            
            if (lock.equals(im.getDisplayName())) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean seekShulkerBox(String lock, ItemStack shulkerBox) {
        
        if (shulkerBox.getType().name().equals("SHULKER_BOX") || shulkerBox.getType().name().endsWith("SHULKER_BOX")) {
            
            if (shulkerBox.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta) shulkerBox.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                    for (ItemStack is : shulker.getInventory().getContents()) {
                        if (is == null) {
                            continue;
                        }
                        if (!is.hasItemMeta()) {
                            continue;
                        }
                        ItemMeta itemMeta = is.getItemMeta();
                        if (itemMeta == null || !itemMeta.hasDisplayName()) {
                            continue;
                        }
                        
                        if (lock.equals(itemMeta.getDisplayName())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void Event(PlayerInteractEvent e) {
        
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Material.BOOKSHELF.equals(e.getClickedBlock().getType()) && EquipmentSlot.HAND.equals(e.getHand())) {
            
            if (e.getPlayer().isSneaking()) {
                
                if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TRIPWIRE_HOOK)) {
                    
                    ItemMeta im = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    if (im != null && im.hasDisplayName()) {
                        
                        e.setCancelled(true);
                        if (Main.viewers.values().stream().noneMatch(p -> p.getLeft().equals(e.getClickedBlock().getLocation()))) {
                            
                            if (Main.inventories.getOrDefault(e.getClickedBlock().getLocation(),
                                    new Pair<>(null, new Pair<>(State.OPEN, null))).getRight().getLeft() == State.VIEW) {
                                
                                String lock = Main.inventories.get(e.getClickedBlock().getLocation()).getRight().getRight();
                                if (lock.equals(im.getDisplayName())) {
                                    Pair<Inventory, Pair<State, String>> p =
                                            Main.inventories.getOrDefault(e.getClickedBlock().getLocation(), new Pair<>(null, new Pair<>(State.OPEN, null)));
                                    
                                    p.getRight().setRight(null);
                                    p.getRight().setLeft(State.OPEN);
                                    Main.inventories.put(e.getClickedBlock().getLocation(), p);
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "State has been set to " + ChatColor.BLUE + "OPEN" +
                                            ChatColor.DARK_AQUA + ", and lock has been removed");
                                } else {
                                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                                    e.getPlayer().sendMessage(ChatColor.RED + "Can't unlock with this current password");
                                }
                                
                            } else if (Main.inventories.getOrDefault(e.getClickedBlock().getLocation(),
                                    new Pair<>(null, new Pair<>(State.OPEN, null))).getRight().getLeft() == State.OPEN) {
                                
                                Pair<Inventory, Pair<State, String>> p =
                                        Main.inventories.getOrDefault(e.getClickedBlock().getLocation(), new Pair<>(null, new Pair<>(State.OPEN, null)));
                                
                                p.getRight().setRight(im.getDisplayName());
                                p.getRight().setLeft(State.LOCKED);
                                
                                Main.inventories.put(e.getClickedBlock().getLocation(), p);
                                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                                e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "State has been set to " + ChatColor.BLUE + "LOCKED" +
                                        ChatColor.DARK_AQUA + ", and lock has been set to " + ChatColor.BLUE + im.getDisplayName());
                            } else if (Main.inventories.getOrDefault(e.getClickedBlock().getLocation(),
                                    new Pair<>(null, new Pair<>(State.OPEN, null))).getRight().getLeft() == State.LOCKED) {
                                
                                String lock = Main.inventories.get(e.getClickedBlock().getLocation()).getRight().getRight();
                                if (lock.equals(im.getDisplayName())) {
                                    Pair<Inventory, Pair<State, String>> p =
                                            Main.inventories.getOrDefault(e.getClickedBlock().getLocation(), new Pair<>(null, new Pair<>(State.LOCKED, null)));
                                    
                                    p.getRight().setLeft(State.VIEW);
                                    Main.inventories.put(e.getClickedBlock().getLocation(), p);
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "State has been set to " + ChatColor.BLUE + "VIEW");
                                    
                                    Inventory inv = p.getLeft();
                                    for (ItemStack is : inv) {
                                        if (is != null && is.getType().equals(Material.WRITTEN_BOOK) && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
                                            System.out.println("test");
                                            ItemMeta tmpMeta = is.getItemMeta();
                                            if (tmpMeta.getDisplayName().endsWith("<copy>")) {
                                                tmpMeta.setDisplayName(tmpMeta.getDisplayName().replace("<copy>", ""));
                                                List<String> lore = tmpMeta.getLore();
                                                if (lore == null) {
                                                    lore = new ArrayList<>();
                                                }
                                                lore.add("Click to copy book");
                                                tmpMeta.setLore(lore);
                                                is.setItemMeta(tmpMeta);
                                            } else if (tmpMeta.getDisplayName().endsWith("<uncopy>")) {
                                                tmpMeta.setDisplayName(tmpMeta.getDisplayName().replace("<uncopy>", ""));
                                                List<String> lore = tmpMeta.getLore();
                                                if (lore != null) {
                                                    lore.remove("Click to copy book");
                                                }
                                                tmpMeta.setLore(lore);
                                                is.setItemMeta(tmpMeta);
                                            }
                                        }
                                    }
                                } else {
                                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                                    e.getPlayer().sendMessage(ChatColor.RED + "Can't unlock with this current password");
                                }
                            } else {
                                e.getPlayer().sendMessage(ChatColor.RED + "Illegal state!");
                            }
                            
                        } else {
                            e.getPlayer().sendMessage(ChatColor.RED + "Can't change the state when somebody is looking in the bookshelf");
                        }
                    }
                }
            } else {
                
                if (Main.viewers.values().stream().noneMatch(p -> p.getLeft().equals(e.getClickedBlock().getLocation()))) {
                    
                    if (Main.inventories.getOrDefault(e.getClickedBlock().getLocation(), new Pair<>(null, new Pair<>(State.OPEN, null))).getRight().getRight() != null) {
                        Pair<State, String> lockState = Main.inventories.get(e.getClickedBlock().getLocation()).getRight();
                        if (!hasLock(lockState.getRight(), e.getPlayer())) {
                            if (lockState.getLeft() == State.VIEW) {
                                
                                if (Main.inventories.containsKey(e.getClickedBlock().getLocation())) {
                                    Inventory inv = Main.inventories.get(e.getClickedBlock().getLocation()).getLeft();
                                    Inventory viewInv = Bukkit.createInventory(null, SHELF_SIZE, Main.NAME + " (view only)");
                                    viewInv.setContents(inv.getContents());
                                    e.getPlayer().openInventory(viewInv);
                                } else {
                                    Inventory inv = Bukkit.createInventory(null, SHELF_SIZE, Main.NAME);
                                    e.getPlayer().openInventory(inv);
                                }
                                return;
                                
                            }
                            e.getPlayer().sendMessage(ChatColor.RED + "This bookshelf has a lock");
                            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                            return;
                        }
                    }
                    
                    Pair<Location, Pair<State, String>> pair = new Pair<>(
                            e.getClickedBlock().getLocation(),
                            
                            Main.inventories.getOrDefault(
                                    e.getClickedBlock().getLocation(),
                                    new Pair<>(null, new Pair<>(State.OPEN, null))
                            ).getRight()
                    );
                    e.setCancelled(true);
                    Main.viewers.put(e.getPlayer().getUniqueId(), pair);
                    
                    openBookshelf(e.getPlayer(), e.getClickedBlock().getLocation());
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "Only 1 player can view a bookshelf");
                }
            }
        }
    }
    
    public static void openBookshelf(Player player, Location location) {
        if (Main.inventories.containsKey(location)) {
            player.openInventory(Main.inventories.get(location).getLeft());
        } else {
            Inventory inv = Bukkit.createInventory(null, SHELF_SIZE, Main.NAME);
            player.openInventory(inv);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void event(BlockPlaceEvent e) {
        if (e.getBlockAgainst().getType().equals(Material.BOOKSHELF) && !e.getPlayer().isSneaking()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void event(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.BOOKSHELF)) {
            
            if (Main.inventories.containsKey(e.getBlock().getLocation())) {
                
                Location l = e.getBlock().getLocation();
                
                for (UUID uuid : viewers.keySet()) {
                    if (viewers.get(uuid).getLeft().equals(l)) {
                        Player viewer = Bukkit.getPlayer(uuid);
                        if (viewer != null) {
                            viewer.closeInventory();
                        }
                        viewers.remove(uuid);
                        break;
                    }
                }
                
//                if (Main.inventories.get(l).getRight().getRight() != null) {
//                    ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
//                    if (meta != null) {
//                        if (meta.hasDisplayName()) {
//                            String name = meta.getDisplayName();
//                            if (!Main.inventories.get(l).getRight().getRight().equals(name)) {
//                                return;
//                            }
//                        } else {
//                            return;
//                        }
//                    } else {
//                        return;
//                    }
//                }
                
                Inventory inv = Main.inventories.get(l).getLeft();
                Main.inventories.remove(l);
                
                l.setY(l.getY() + 0.5);
                
                for (ItemStack is : inv.getContents()) {
                    if (is == null) {
                        continue;
                    }
                    e.getPlayer().getWorld().dropItemNaturally(l, is);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void extend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType().equals(Material.BOOKSHELF)) {
                e.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void retract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType().equals(Material.BOOKSHELF)) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
