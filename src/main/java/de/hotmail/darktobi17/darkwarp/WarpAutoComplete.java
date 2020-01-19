package de.hotmail.darktobi17.darkwarp;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class WarpAutoComplete implements TabCompleter {
    DarkWarp MainInstance;
    FileConfiguration cfg;
    WarpInvHandler InvHandler;

    public WarpAutoComplete(DarkWarp Main, WarpInvHandler IHandler) {
        MainInstance = Main;
        InvHandler = IHandler;
        cfg = Main.getCfg();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        //Variables
        List<String> RtnValues = new ArrayList<>();

        //Test if Command sender is an Player or the Console
        if (sender instanceof Player) {
            //Cast sender to ply obj
            Player ply = (Player) sender;

            //Get return Variables
            switch(args.length) {
                case 2:
                    if ((args[0].equals("create") || args[0].equals("-c")) && ply.hasPermission("dwarp.create")) {
                        RtnValues.add("<Name>");
                    }
                    if ((args[0].equals("remove") || args[0].equals("-r")) && ply.hasPermission("dwarp.remove")) {
                        //Get all Warps
                        String AllWarps = String.join(", ", Objects.requireNonNull(cfg.getConfigurationSection("warps")).getKeys(false));
                        RtnValues.addAll(Arrays.asList(AllWarps.split(", ")));
                    }
                    break;
                case 3:
                    //Return Material List
                    if ((args[0].equals("create") || args[0].equals("-c")) && ply.hasPermission("dwarp.create")) {
                        String CurMatName;
                        for (Material Mat: Material.values()) {
                            if (Mat.name().length() >= args[2].length() && args[2].equalsIgnoreCase(Mat.name().substring(0, args[2].length()))) {
                                CurMatName = Mat.name().toLowerCase();
                                RtnValues.add(CurMatName.substring(0,1).toUpperCase() + CurMatName.substring(1).toLowerCase());
                            }
                        }
                    }
                    break;
                case 4:
                    //Return Color List
                    if ((args[0].equals("create") || args[0].equals("-c")) && ply.hasPermission("dwarp.create")) {
                        String CurColName;
                        for (ChatColor Col: ChatColor.values()) {
                            if (Col.name().length() >= args[3].length() && args[3].equalsIgnoreCase(Col.name().substring(0, args[3].length()))) {
                                CurColName = Col.name().toLowerCase();
                                RtnValues.add(CurColName.substring(0,1).toUpperCase() + CurColName.substring(1));
                            }
                        }
                    }
                    break;
                case 5:
                    //Return Slot List
                    String[] WarpList = InvHandler.GetWarpList();
                    if ((args[0].equals("create") || args[0].equals("-c")) && ply.hasPermission("dwarp.create")) {
                        for (int i = 0; i < MainInstance.getSlots(); i++) {
                            if (WarpList[i] == null) {
                                RtnValues.add(String.valueOf(i));
                            }
                        }
                    }
                    break;
                case 6:
                    //Return Visibility
                    if ((args[0].equals("create") || args[0].equals("-c")) && ply.hasPermission("dwarp.create")) {
                        RtnValues.add((alias.equals("warp") ? "true" : "1"));
                        RtnValues.add((alias.equals("warp") ? "false" : "0"));
                    }
                    break;
                default:
                    //Check if Player has permission to warp
                    if (ply.hasPermission("dwarp.warp")) {
                        RtnValues.add("gui");
                        RtnValues.add("-g");
                    }
                    //Check if Player has permission to see the list
                    if (ply.hasPermission("dwarp.list")) {
                        RtnValues.add("list");
                        RtnValues.add("-l");
                    }
                    //Check if Player has the permission to create a new Warp
                    if (ply.hasPermission("dwarp.create")) {
                        RtnValues.add("create");
                        RtnValues.add("-c");
                    }
                    //Check if Player has the permission to remove a warp
                    if (ply.hasPermission("dwarp.remove")) {
                        RtnValues.add("remove");
                        RtnValues.add("-r");
                    }
                    //Get all Warps
                    String AllWarps = String.join(", ", Objects.requireNonNull(cfg.getConfigurationSection("warps")).getKeys(false));
                    RtnValues.addAll(Arrays.asList(AllWarps.split(", ")));


                    //Remove unwanted Items
                    List<String> RtnVals = new ArrayList<>();
                    for (String rtnValue : RtnValues) {
                        if (rtnValue.length() >= args[0].length() && args[0].equalsIgnoreCase(rtnValue.substring(0, args[0].length()))) {
                            RtnVals.add(rtnValue);
                        }
                    }
                    //Add add entries again
                    RtnValues.clear();
                    RtnValues = RtnVals;
                    break;
            }
        } else {
            //Console called command...
            MainInstance.getLogger().info("The Warp command cannot be called by the Console!");
        }

        return RtnValues;
    }
}
