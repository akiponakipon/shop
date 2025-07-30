package dev.shop;

import dev.shop.commands.MoneyCommand;
import dev.shop.commands.SellCommand;
import dev.shop.commands.SellListCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ShopManager shopManager;
    private BlacklistManager blacklistManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;

        // マネージャー初期化
        this.shopManager = new ShopManager(this);
        this.blacklistManager = new BlacklistManager(this);
        this.economyManager = new EconomyManager(this);

        // イベント登録
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        // コマンド登録
        SellCommand sellCommand = new SellCommand(this);
        getCommand("sell").setExecutor(sellCommand);
        getCommand("selllist").setExecutor(new SellListCommand(this, sellCommand));
        getCommand("money").setExecutor(new MoneyCommand(this));

        getLogger().info("Shop Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Shop Plugin Disabled!");
    }

    public static ShopPlugin getInstance() {
        return instance;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public BlacklistManager getBlacklistManager() {
        return blacklistManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
