package me.omer.shopcomeback;

import org.bukkit.plugin.java.JavaPlugin;

public class Shopcomeback extends JavaPlugin {

    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private AuctionGUI auctionGUI;

    @Override
    public void onEnable() {
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);
        this.auctionGUI = new AuctionGUI(this, auctionManager, economyManager);

        economyManager.loadEconomy();
        auctionManager.loadItems();

        IhaleCommand commandExecutor = new IhaleCommand(this, auctionManager, economyManager, auctionGUI);
        getCommand("ihale").setExecutor(commandExecutor);
        getCommand("ihale").setTabCompleter(commandExecutor);
        getCommand("para").setExecutor(commandExecutor);
        getCommand("paragonder").setExecutor(commandExecutor);
        getCommand("paragonder").setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(auctionGUI, this);
        getLogger().info("Shopcomeback v1.1.0 başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) economyManager.saveEconomy();
        if (auctionManager != null) auctionManager.saveItems();
    }
}
