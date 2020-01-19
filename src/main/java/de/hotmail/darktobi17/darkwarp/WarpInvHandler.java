package de.hotmail.darktobi17.darkwarp;

//Bukkit / Spigot Imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class WarpInvHandler implements InventoryHolder, Listener {
    private DarkWarp MainInstance;
    private FileConfiguration cfg;
    private final Inventory inv;
    private boolean WarpsChg = false;
    private String[] CompleteWarpList;
    int Slots;

    public String[] GetWarpList() {
        //Get all current Warps
        String AllWarps = String.join(", ", Objects.requireNonNull(cfg.getConfigurationSection("warps")).getKeys(false));
        String[] aAllWarps = AllWarps.split(", ");

        //Sort Warps if SlotID is given
        int CurSlotID;
        String sCurSlotID;
        boolean CurSlotVis;
        String[] WarpList = new String[Slots];
        String CurWarpName;
        for (int n = 0; n < aAllWarps.length; n++) {
            CurWarpName =  aAllWarps[n];
            //Load SlotID as String to prevent error due to file corruption
            try {
                sCurSlotID = cfg.getString("warps." + CurWarpName + ".Slot");
                CurSlotID = Integer.parseInt((sCurSlotID == null) ? "501" : sCurSlotID);
            } catch (Exception ex) {
                CurSlotID = 501;
                MainInstance.getLogger().info("SlotID of " + CurWarpName + " is corrupted!");
            }
            //Get String instead of Boolean if Config file is corrupted / got messed up
            CurSlotVis = Boolean.parseBoolean(cfg.getString("warps." + CurWarpName + ".GUI"));

            //Ignore SlotID 501 (default value for unsorted) and ignore if Warp should not be displayed
            if (CurSlotID != 501 && CurSlotVis) {
                //Item has a specified Slot
                if (CurSlotID <= Slots) {
                    if (WarpList[CurSlotID] == null) {
                        WarpList[CurSlotID] = CurWarpName;
                        aAllWarps[n] = null;
                    } else {
                        MainInstance.getLogger().info("SlotID duplicate found! Slot(" + CurSlotID + ") is set multiple times.");
                    }
                } else {
                    MainInstance.getLogger().info("SlotID error! " + CurWarpName + "'s given Slot(" + CurSlotID + ") is greater than 27.");
                }
            }
        }

        //Fill Warplist
        String CurWarpVis;
        for (int n = 0; n < WarpList.length; n++) {
            //If Current Slot is empty
            if (WarpList[n] == null) {
                //Find an Item that has no Slot
                for (int i = 0; i < aAllWarps.length; i++) {
                    //If an Warp has been found replace current warp position with the Warp
                    CurWarpVis = cfg.getString("warps." + aAllWarps[i] + ".GUI");
                    if (aAllWarps[i] != null && Boolean.parseBoolean(CurWarpVis == null ? "true" : CurWarpVis)) {
                        WarpList[n] = aAllWarps[i];
                        aAllWarps[i] = null;
                        break;
                    }
                }
                //If all Warps have been sorted stop
                if (WarpList[n] == null) {
                    break;
                }
            }
        }
        return WarpList;
    }

    public WarpInvHandler(DarkWarp Main) {
        //Setup references
        MainInstance = Main;
        this.cfg = Main.getCfg();
        this.Slots = Main.getSlots();

        //Setup WarpList
        CompleteWarpList = GetWarpList();

        //Create Inventory
        String GUITitle = cfg.getString("opt.GUITitle");
        inv = Bukkit.createInventory(this, Slots, (GUITitle == null) ? "Warp menu" : GUITitle);
        InitItems();
    }

    //Setter
    public void SetWarpsChg() {
        WarpsChg = true;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    //Fill up the Warp GUI
    public void InitItems() {
        //Add Warps to Warp Menu
        int CurSlot = 0;
        for (String warp: CompleteWarpList) {
            if (warp != null) {
                //Get Item Icon and Color
                String CurMat = cfg.getString("warps." + warp + ".Icon");
                String CurCol = cfg.getString("warps." + warp + ".Color");

                //Set Values to default values if left open
                CurMat = (CurMat == null) ? "BRICK" : CurMat;
                CurCol = (CurCol == null) ? "f" : CurCol;

                //Uppercase first letter of Warp names
                String WarpName = warp;
                if (cfg.getBoolean("opt.UppercaseFirstLetter")) {
                    WarpName = warp.substring(0,1).toUpperCase() + warp.substring(1);
                }

                //Add Item to WarpGUI
                inv.setItem(CurSlot, createGuiItem(Material.getMaterial(CurMat.toUpperCase()), "§" + CurCol.substring(0, 1) + WarpName));
            }
            CurSlot++;
        }
    }

    //Spigot Wiki Code snipped - shortened
    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    //Look for Changes
    private void ChgChecker() {
        //Update the GUI's Inventory if the Warps have changed
        if (WarpsChg) {
            //Setback the change tracker
            WarpsChg = false;

            //Reload Warp List
            CompleteWarpList = GetWarpList();

            //Clear current GUI and recreate Items
            inv.clear();
            InitItems();
        }
    }

    //Open GUI
    public void openInventory(Player ply) {
        ChgChecker();               //Check if Warps have been changed
        ply.openInventory(inv);
    }

    //Messenger
    private void SendMessage(Player ply, String Message, String Replacement) {
        if (Message != null) {
            ply.sendMessage(ChatColor.RED + Message.replaceAll("(?i)\\B%.+%", ChatColor.GOLD + Replacement + ChatColor.RED));
        } else {
            MainInstance.getLogger().info("Message was null!");
        }
    }

    //Selection Event
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) {
            return;
        }
        if (e.getClick().equals(ClickType.NUMBER_KEY)) {
            e.setCancelled(true);
        }
        e.setCancelled(true);

        Player ply = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        //Selected Item != Null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        //Warp to a Location player selected
        if (ply.hasPermission("dwarp.warp")) {
            //Define Warp Name Variables
            String WName = CompleteWarpList[e.getRawSlot()];
            String WarpName = "warps." + CompleteWarpList[e.getRawSlot()];

            //Check if WarpName exists
            if (cfg.contains(WarpName)) {
                Location loc = new Location(Bukkit.getWorld(Objects.requireNonNull(
                        cfg.getString(WarpName + ".world"))),
                        cfg.getDouble(WarpName + ".X"),
                        cfg.getDouble(WarpName + ".Y"),
                        cfg.getDouble(WarpName + ".Z"),
                        (float) cfg.getDouble(WarpName + ".Yaw"),
                        (float) cfg.getDouble(WarpName + ".Pitch"));
                ply.teleport(loc);
                SendMessage(ply, cfg.getString("msg.Warped"), WName);
            } else {
                SendMessage(ply, cfg.getString("msg.WarpMissing"), WName);
            }
        } else {
            //User doesn't have permission to use the command
            SendMessage(ply, cfg.getString("msg.InsufficientPerm"), "");
        }
    }
}
