@Override
public void onEnable() {
    instance = this;

    this.shopManager = new ShopManager(this);
    this.blacklistManager = new BlacklistManager(this);
    this.economyManager = new EconomyManager(this);

    getServer().getPluginManager().registerEvents(new ShopListener(this), this);

    // SellCommand をインスタンス化して両方に渡す
    SellCommand sellCommand = new SellCommand(this);

    getCommand("sell").setExecutor(sellCommand);
    getCommand("selllist").setExecutor(new SellListCommand(this, sellCommand));
    getCommand("money").setExecutor(new MoneyCommand(this));

    getLogger().info("Shop Plugin Enabled!");
}
