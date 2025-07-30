package dev.shop;

import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ShopManager shopManager;
    private BlacklistManager blacklistManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;

        // データフォルダ作成
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // マネージャー初期化
        this.shopManager = new ShopManager(this);
        this.blacklistManager = new BlacklistManager(this);
        this.economyManager = new EconomyManager(this);

        // リスナー登録
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getLogger().info("Shop Plugin Enabled!");
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
