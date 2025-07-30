@EventHandler
public void onSignClick(PlayerInteractEvent e) {
    if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
    if (e.getClickedBlock() == null) return;
    if (!(e.getClickedBlock().getState() instanceof Sign sign)) return;

    Player p = e.getPlayer();
    Location signLoc = e.getClickedBlock().getLocation();

    Map<String, Object> shop = plugin.getShopManager().getShopAt(signLoc);
    if (shop == null) return;

    // ブラックリストチェック
    if (plugin.getBlacklistManager().isBlacklisted((String) shop.get("owner"), p.getName())) {
        p.sendMessage("§cあなたはこのショップで購入できません");
        return;
    }

    // ショップ情報表示
    String owner = (String) shop.get("owner");
    String item = (String) shop.get("item");
    int amount = (int) shop.get("amount");
    double price = (double) shop.get("price");

    p.sendMessage(ChatColor.GOLD + "=== ショップ情報 ===");
    p.sendMessage("§aオーナー: " + owner);
    p.sendMessage("§e商品: " + item);
    p.sendMessage("§e個数/価格: " + amount + "個 / " + price + " Yen");
    p.sendMessage("§e残り在庫: 未実装カウント");
    p.sendMessage("§e購入する場合はチャットに購入個数を入力してください");

    // 購入待ちに登録
    creatingShopStep.put(p.getUniqueId(), "BUY:" + signLoc.getWorld().getName() + "," + signLoc.getBlockX() + "," + signLoc.getBlockY() + "," + signLoc.getBlockZ());
}

@EventHandler
public void onBuyChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    UUID id = p.getUniqueId();
    if (!creatingShopStep.containsKey(id)) return;

    String step = creatingShopStep.get(id);
    if (!step.startsWith("BUY:")) return;

    e.setCancelled(true);

    try {
        int buyAmount = Integer.parseInt(e.getMessage());
        String locString = step.substring(4);
        Location signLoc = stringToLoc(locString);

        Map<String, Object> shop = plugin.getShopManager().getShopAt(signLoc);
        if (shop == null) {
            p.sendMessage("§cショップが見つかりません");
            creatingShopStep.remove(id);
            return;
        }

        String owner = (String) shop.get("owner");
        String item = (String) shop.get("item");
        int amount = (int) shop.get("amount");
        double price = (double) shop.get("price");

        double totalPrice = price * buyAmount;
        double playerMoney = plugin.getEconomy().getMoney(p);

        if (playerMoney < totalPrice) {
            p.sendMessage("§cお金が足りないため商品を購入することができません");
            creatingShopStep.remove(id);
            return;
        }

        // 在庫チェック（仮: amount でチェック）
        if (buyAmount > amount) {
            p.sendMessage("§c在庫が不足しています");
            creatingShopStep.remove(id);
            return;
        }

        // お金処理
        plugin.getEconomy().removeMoney(p, totalPrice);
        // 売り手のデータはUUIDから処理する必要あり（簡略化）
        // plugin.getEconomy().addMoney(ownerPlayer, totalPrice);

        // アイテム付与
        Material mat = Material.valueOf(item);
        ItemStack stack = new ItemStack(mat, buyAmount);
        p.getInventory().addItem(stack);

        // shops.ymlの在庫を減らす処理（未実装）
        p.sendMessage("§a" + item + " を " + buyAmount + " 個購入しました！ -" + totalPrice + " Yen");

    } catch (NumberFormatException ex) {
        p.sendMessage("§c数字を入力してください");
    }

    creatingShopStep.remove(id);
}
