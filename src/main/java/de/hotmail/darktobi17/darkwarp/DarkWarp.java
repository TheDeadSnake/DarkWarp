package de.hotmail.darktobi17.darkwarp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class DarkWarp extends JavaPlugin {
    //Setup Config File
    private FileConfiguration cfg = getConfig();
    FileConfiguration getCfg() { return cfg; } //Getter for the Config
    private int Slots = 9;
    int getSlots() { return Slots; } //Getter for the number of Slots

    @Override
    public void onEnable() {
        //Plugin Startup - Setup/Load Config
        cfg.addDefault("msg.InsufficientPerm",  "You are not allowed to use that command.");
        cfg.addDefault("msg.WarpCreated",       "You created the warp %WarpName%.");
        cfg.addDefault("msg.WarpRemoved",       "You removed the warp %WarpName%.");
        cfg.addDefault("msg.WarpDuplicate",     "A Warp with the name '%WarpName%' already exists.");
        cfg.addDefault("msg.WarpMissing",       "A Warp with the name '%WarpName%' does not exist.");
        cfg.addDefault("msg.MissingWarpName",   "Please enter a Warp name.");
        cfg.addDefault("msg.ListWarps",         "All Warps: %AllWarps%.");
        cfg.addDefault("msg.Warped",            "You have been warped to %WarpName%.");
        cfg.addDefault("msg.NoWarps",           "There are no warps yet.");
        cfg.addDefault("opt.GUITitle",          "Warp menu");
        cfg.addDefault("opt.SaveExactPlayerRotation",   true);
        cfg.addDefault("opt.UppercaseFirstLetter",      true);
        cfg.addDefault("opt.RowCount",          1);

        //Copy over the default block above in the plugins\DarkWarp\config.yml
        cfg.options().copyDefaults(true);

        //Save Config File
        saveConfig();

        //Load row numbers from config
        String RowCount = cfg.getString("opt.RowCount");
        RowCount = (RowCount != null) ? RowCount : "1";
        int iRowCount;
        try {
            iRowCount = Integer.parseInt(RowCount);
        } catch (Exception ex) {
            iRowCount = 1;
        }
        Slots = 9 * iRowCount;

        //Register Event(s) and Command(s)
        //Register Event and give WarpInvHandler an handle back to Main
        WarpInvHandler CurInvHandler = new WarpInvHandler(this);
        getServer().getPluginManager().registerEvents(CurInvHandler, this);
        //Register Commands and give WarpCmdHandler an handle back to Main and the WarpInvHandler
        WarpCmdHandler CurCmdHandler = new WarpCmdHandler(this, CurInvHandler);
        this.getCommand("warp").setExecutor(CurCmdHandler);
        //Register Command Tab Autocomplete
        WarpAutoComplete CurAutoComplete = new WarpAutoComplete(this, CurInvHandler);
        this.getCommand("warp").setTabCompleter(CurAutoComplete);
    }
}
