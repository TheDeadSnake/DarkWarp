package de.hotmail.darktobi17.darkwarp;

//Bukkit / Spigot Imports
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WarpCmdHandler implements CommandExecutor {
    private DarkWarp MainInstance;
    private FileConfiguration cfg;
    private WarpInvHandler InvHandler;

    //CmdHandler Constructor
    public WarpCmdHandler(DarkWarp Main, WarpInvHandler IHandler) {
        MainInstance = Main;
        this.cfg = Main.getCfg();
        this.InvHandler = IHandler;
    }

    //Functions
    //Send Pre-Defined Messages from the Config File and replace the %something%
    private void SendMessage(Player ply, String Message, String Replacement) {
        if (Message != null) {
            ply.sendMessage(ChatColor.RED + Message.replaceAll("(?i)\\B%.+%", ChatColor.GOLD + Replacement + ChatColor.RED));
        } else {
            MainInstance.getLogger().info("Message was null!");
        }
    }

    //If SaveExactPlayerRotation in the Config File is turned off snap to 90° Yaw Angles
    private float GetRoughYaw(Player ply) {
        float yaw = ply.getLocation().getYaw();

        //Set rough yaw rotations (90°)
        if (yaw >= -45 && yaw <= 44.9) yaw = (float) 0.0;
        if (yaw >= 45 && yaw <= 135 ) yaw = (float) 90.0;
        if (yaw <= -45 && yaw >= 135) yaw = (float) -90.0;
        if (yaw <= -135 && yaw >= 135) yaw = (float) -180.0;

        return yaw;
    }

    //Warp - General
    private void CreateWarp(Player ply, String WName, String Mat, String Col, Integer SlotID, Boolean DiGUI) {
        String WarpName = "warps." + WName;
        //Check for Players permission
        if (ply.hasPermission("dwarp.create")) {
            //Check if warp already exists
            if (!cfg.contains(WarpName)) {
                //Create new Warp location
                cfg.set(WarpName + ".world", ply.getWorld().getName());
                cfg.set(WarpName + ".X", ply.getLocation().getX());
                cfg.set(WarpName + ".Y", ply.getLocation().getY());
                cfg.set(WarpName + ".Z", ply.getLocation().getZ());
                cfg.set(WarpName + ".Yaw", cfg.getBoolean("opt.SaveExactPlayerRotation") ? ply.getLocation().getYaw() : GetRoughYaw(ply));
                cfg.set(WarpName + ".Pitch", cfg.getBoolean("opt.SaveExactPlayerRotation") ? ply.getLocation().getPitch() : 0);
                cfg.set(WarpName + ".Icon", Mat);
                cfg.set(WarpName + ".Color", Col);
                cfg.set(WarpName + ".Slot", SlotID);
                cfg.set(WarpName + ".GUI", DiGUI);
                MainInstance.saveConfig();
                InvHandler.SetWarpsChg();
                SendMessage(ply, cfg.getString("msg.WarpCreated"), WName);
            } else {
                SendMessage(ply, cfg.getString("msg.WarpDuplicate"), WName);
            }
        } else {
            //User doesn't have permission to use the command
            SendMessage(ply, cfg.getString("msg.InsufficientPerm"), "");
        }
    }
    private void RemoveWarp(Player ply, String WName) {
        String WarpName = "warps." + WName;
            //Check for Players permission
            if (ply.hasPermission("dwarp.remove")) {
                //Check if warp exists
                if (cfg.contains(WarpName)) {
                    //Set Warp location to null => delete
                    cfg.set(WarpName, null);
                    MainInstance.saveConfig();
                    InvHandler.SetWarpsChg();
                    SendMessage(ply, cfg.getString("msg.WarpRemoved"), WName);
                } else {
                    SendMessage(ply, cfg.getString("msg.WarpMissing"), WName);
                }
            } else {
                //User doesn't have permission to use the command
                SendMessage(ply, cfg.getString("msg.InsufficientPerm"), "");
            }
    }

    //Warp
    private void WarpPlayer(Player ply, String WName) {
        String WarpName = "warps." + WName;
        //Check for Players permission
        if (ply.hasPermission("dwarp.warp")) {
            //Check if WarpName exists
            if (cfg.contains(WarpName)) {
                //Teleport Player to location
                Location loc = new Location(Bukkit.getWorld(Objects.requireNonNull(cfg.getString(WarpName + ".world"))), cfg.getDouble(WarpName + ".X"), cfg.getDouble(WarpName + ".Y"), cfg.getDouble(WarpName + ".Z"), (float) cfg.getDouble(WarpName + ".Yaw"), (float) cfg.getDouble(WarpName + ".Pitch"));
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

    private void ShowHelp(Player ply) {
        //Send instruction how to use the commands
        String ShortCommands = "";
        //Help Header
        ply.sendMessage(
                ChatColor.GOLD + "_.-*'" +
                        ChatColor.RED + "[" +
                        ChatColor.GRAY + "Dark " +
                        ChatColor.GOLD + "Warp " +
                        ChatColor.DARK_AQUA + "- " +
                        ChatColor.GREEN + "Help" +
                        ChatColor.RED + "]" +
                        ChatColor.GOLD + "'*-._");
        //Check if Player has permission to warp
        if (ply.hasPermission("dwarp.warp")) {
            ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GOLD + "warp " +
                    ChatColor.AQUA + "<Name>");
            ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GRAY + "warp " +
                    ChatColor.GOLD + "gui");
            ShortCommands += ChatColor.GOLD + "-g" + ChatColor.GRAY + "|";
        }
        //Check if Player has permission to see the list
        if (ply.hasPermission("dwarp.list")) {
            ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GRAY + "warp " +
                    ChatColor.GOLD + "list");
            ShortCommands += ChatColor.GOLD + "-l" + ChatColor.GRAY + "|";
        }
        //Check if Player has the permission to create a new Warp
        if (ply.hasPermission("dwarp.create")) {
            ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GRAY + "warp " +
                    ChatColor.GOLD + "create " +
                    ChatColor.AQUA + "<Name> <Material> <ColorCode> <Slot> <Visible>");
            ShortCommands += ChatColor.GOLD + "-c" + ChatColor.GRAY + "|";
        }
        //Check if Player has the permission to remove a warp
        if (ply.hasPermission("dwarp.remove")) {
            ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GRAY + "warp " +
                    ChatColor.GOLD + "remove " +
                    ChatColor.AQUA + "<Name>");
            ShortCommands += ChatColor.GOLD + "-r" + ChatColor.GRAY + "|";
        }
        ply.sendMessage(ChatColor.YELLOW + "/" + ChatColor.GRAY + "warp " + ChatColor.GOLD + "help");

        //Send text how the short commands work
        ply.sendMessage("");
        ply.sendMessage(
                ChatColor.YELLOW + "/" +
                        ChatColor.GRAY + "w " +
                        ChatColor.GRAY + "(" +
                        ShortCommands +
                        ChatColor.GOLD + "-h" +
                        ChatColor.GRAY + ")");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Test if Command sender is an Player or the Console
        if (sender instanceof Player) {
            //Cast sender to ply obj
            Player ply = (Player) sender;

            //Switch through different commands
            switch ((args.length > 0 && args[0] != null ? args[0] : "").toLowerCase()) {
                case "create":
                case "-c":
                    //Create a new Warp Location
                    String CurMat = (args.length >= 3 && args[2] != null ) ? args[2].toUpperCase() : "BRICK";
                    String CurCol = (args.length >= 4 && args[3] != null ) ? args[3].toUpperCase() : "WHITE";
                    String SlotID = (args.length >= 5 && args[4] != null ) ? args[4].toLowerCase() : "501";
                    int iSlotID;
                    String DiGUI = (args.length >= 6 && args[5] != null ) ? args[5].toLowerCase() : "true";
                    boolean bDiGUI;

                    //Translate Color Name to Color Char
                    String PreCurCol = CurCol;
                    for (ChatColor col: ChatColor.values()) {
                        if (col.name().equals(CurCol)) {
                            CurCol = String.valueOf(col.getChar());
                            break;
                        }
                    }
                    if (PreCurCol.equals(CurCol)) {
                        CurCol = "f";
                    }

                    //Check if given Icon Material does exist
                    if (Material.getMaterial(CurMat) == null) {
                        ply.sendMessage("The provided material('" + CurMat + "') does not exist.");
                        break;
                    }
                    //Check if given Color Code does exist
                    if (ChatColor.getByChar(CurCol.substring(0, 1)) == null) {
                        ply.sendMessage("The provided color('" + CurCol + "') does not exist.");
                        break;
                    }
                    //Check if given SlotID is a number and lower than
                    try {
                        iSlotID = Integer.parseInt(SlotID);
                        if (iSlotID > 27 && iSlotID != 501) {
                            ply.sendMessage("The provided Slot number('" + SlotID + "') is too high (MAX. 27).");
                            break;
                        }
                    } catch (Exception ex) {
                        ply.sendMessage("The provided Slot number('" + SlotID + "') is not an number.");
                        break;
                    }
                    //Convert DisplayInGUI into Boolean (no check / error message defaults to false if not bool)
                    bDiGUI = Boolean.parseBoolean(DiGUI.replace("1", "true"));

                    CreateWarp(ply, ((args[1] != null) ? args[1] : "").toLowerCase(), CurMat, CurCol.substring(0, 1), iSlotID, bDiGUI);
                    break;
                case "remove":
                case "-r":
                    //Remove an existing Warp Location
                    //Check if WName is not null
                    if (args.length >= 2 && args[1] != null && !args[1].equals("")) {
                        RemoveWarp(ply, args[1].toLowerCase());
                    } else {
                        SendMessage(ply, cfg.getString("msg.MissingWarpName"), "");
                    }
                    break;
                case "list":
                case "-l":
                    //Print a List of all Warps
                    if (ply.hasPermission("dwarp.list")) {
                        String AllWarps = String.join(", ", Objects.requireNonNull(cfg.getConfigurationSection("warps")).getKeys(false));
                        SendMessage(ply, cfg.getString("msg.ListWarps"), (AllWarps.equals("null") ? cfg.getString("msg.NoWarps") : AllWarps));
                    } else {
                        //User doesn't have permission to use the command
                        SendMessage(ply, cfg.getString("msg.InsufficientPerm"), "");
                    }
                    break;
                case "gui":
                case "-g":
                    //Open a GUI
                    InvHandler.openInventory(ply);
                    break;
                case "help":
                case "-h":
                    ShowHelp(ply);
                    break;
                default:
                    //Warp to a Location
                    if (ply.hasPermission("dwarp.warp")) {
                        if (args.length > 0 && args[0] != null && !args[0].equals("")) {
                            WarpPlayer(ply, args[0].toLowerCase());
                        } else {
                            ShowHelp(ply);
                        }
                    } else {
                        //User doesn't have permission to use the command
                        SendMessage(ply, cfg.getString("msg.InsufficientPerm"), "");
                    }
                    break;
            }
        } else {
            //Console called command...
            MainInstance.getLogger().info("The Warp command cannot be called by the Console!");
        }
        return false;
    }
}
