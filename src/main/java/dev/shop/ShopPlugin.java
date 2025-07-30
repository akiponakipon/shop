package dev.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ShopPlugin extends JavaPlugin implements Listener {

    // プレイヤー所持金をメモリ管理（シンプル版）
    private final Map<UUID, Double> money = new HashMap<>();

    // 売却価格リスト
    private final Map<Material, Double> sellPrices = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("ShopPlugin Enabled!");
        Bukkit.getPluginManager().registerEvents(this, this);

        setupSellPrices();

        // コマンド登録
        getCommand("sell").setExecutor(this);
        getCommand("selllist").setExecutor(this);
        getCommand("money").setExecutor(this);
        getCommand("shophelp").setExecutor(this);
        getCommand("shopcommandlist").setExecutor(this);
        getCommand("officialshop").setExecutor(this);
    }

    private void setupSellPrices() {
        sellPrices.put(Material.COPPER_INGOT, 0.002);
        sellPrices.put(Material.COPPER_BLOCK, 0.018);
        sellPrices.put(Material.COAL, 0.005);
        sellPrices.put(Material.COAL_BLOCK, 0.045);
        sellPrices.put(Material.LAPIS_LAZULI, 0.008);
        sellPrices.put(Material.LAPIS_BLOCK, 0.072);
        sellPrices.put(Material.IRON_INGOT, 0.015);
        sellPrices.put(Material.IRON_BLOCK, 0.135);
        sellPrices.put(Material.GOLD_INGOT, 5.0);
        sellPrices.put(Material.GOLD_BLOCK, 45.0);
        sellPrices.put(Material.DIAMOND, 15.0);
        sellPrices.put(Material.DIAMOND_BLOCK, 135.0);
        sellPrices.put(Material.EMERALD, 27.5);
        sellPrices.put(Material.EMERALD_BLOCK, 247.5);
        sellPrices.put(Material.NETHERITE_INGOT, 125.0);
        sellPrices.put(Material.NETHERITE_BLOCK, 1125.0);
    }

    /* ===== 経済ユーティリティ ===== */
    private double getMoney(Player p) {
        return money.getOrDefault(p.getUniqueId(), 0.0);
    }

    private void setMoney(Player p, double amount) {
        money.put(p.getUniqueId(), Math.min(amount, 100000000.0)); // 上限1億
    }

    private void addMoney(Player p, double amount) {
        setMoney(p, getMoney(p) + amount);
    }

    private boolean removeMoney(Player p, double amount) {
        double bal = getMoney(p);
        if (bal < amount) return false;
        setMoney(p, bal - amount);
        return true;
    }

    /* ===== コマンド処理 ===== */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "sell":
                handleSell(player);
                return true;
            case "selllist":
                handleSellList(player);
                return true;
            case "money":
                player.sendMessage("§e現在の所持金: " + getMoney(player) + " Yen");
                return true;
            case "shophelp":
                handleShopHelp(player);
                return true;
            case "shopcommandlist":
                handleShopCommandList(player);
                return true;
            case "officialshop":
                openOfficialShop(player);
                return true;
        }
        return false;
    }

    private void handleSell(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§c手に持っているアイテムがありません");
            return;
        }
        Material mat = item.getType();
        if (!sellPrices.containsKey(mat)) {
            player.sendMessage("§cこのアイテムは売却できません");
            return;
        }
        int amount = item.getAmount();
        double price = sellPrices.get(mat) * amount;
        player.getInventory().setItemInMainHand(null);
        addMoney(player, price);
        player.sendMessage("§a" + mat.name() + " を " + amount + " 個売却しました！ +" + price + " Yen");
    }

    private void handleSellList(Player player) {
        player.sendMessage("§6=== [売却可能アイテム一覧] ===");
        sellPrices.forEach((mat, price) ->
                player.sendMessage(mat.name() + ": " + price + " Yen"));
        player.sendMessage("§c[注意] 手に持っている売却可能アイテムをすべて売却します。");
        player.sendMessage("§cひとつずつ売却したい場合は、手持ちの個数を1個にしてください。");
    }

    private void handleShopHelp(Player player) {
        player.sendMessage("§a=== [Shop プラグインヘルプ] ===");
        player.sendMessage("このプラグインは経済・ショップ機能を追加します。");
        player.sendMessage("");
        player.sendMessage("◆ 経済");
        player.sendMessage(" - /sell でアイテムを売却してYenを獲得できます");
        player.sendMessage(" - /money で自分の所持金を確認できます");
        player.sendMessage(" - /selllist で売却可能なアイテムと価格を確認できます");
        player.sendMessage("");
        player.sendMessage("◆ 公式ショップ");
        player.sendMessage(" - /officialshop で購入可能アイテム一覧を開きます");
        player.performCommand("shopcommandlist");
    }

    private void handleShopCommandList(Player player) {
        player.sendMessage("§6=== [Shopコマンド一覧] ===");
        player.sendMessage("§e/sell §7- 手に持っている売却可能アイテムを売却する");
        player.sendMessage("§e/selllist §7- 売却可能なアイテムと価格を確認する");
        player.sendMessage("§e/money §7- 自分の所持金を確認する");
        player.sendMessage("§e/shophelp §7- プラグインの説明を表示する");
        player.sendMessage("§e/shopcommandlist §7- 利用可能なコマンド一覧を表示する");
        player.sendMessage("§e/officialshop §7- 公式ショップを開く");
    }

    /* ===== 公式ショップ ===== */
    private void openOfficialShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Official Shop");

        int slot = 0;
        for (Map.Entry<Material, Double> entry : sellPrices.entrySet()) {
            Material mat = entry.getKey();
            ItemStack item = new ItemStack(mat, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + mat.name());
            meta.setLore(Collections.singletonList("§7購入価格: " + (entry.getValue() * 5) + " Yen"));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§6Official Shop")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Material mat = clicked.getType();
        if (!sellPrices.containsKey(mat)) return;

        double price = sellPrices.get(mat) * 5;
        if (!removeMoney(player, price)) {
            player.sendMessage("§cお金が足りないため商品を購入することができません");
            return;
        }
        player.getInventory().addItem(new ItemStack(mat, 1));
        player.sendMessage("§a" + mat.name() + " を " + price + " Yen で購入しました！");
    }
}
