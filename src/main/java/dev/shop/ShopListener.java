package dev.shop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;
    private final Map<UUID, String> step = new HashMap<>();
    private final Map<UUID, Double> tempPrice = new HashMap<>();
    private final Map<UUID, ItemStack> tempItem = new HashMap<>();
    private final Map<UUID, Location> tempChest = new HashMap<>();

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ショップ作成
    @EventHandler
    public void onChestClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST && block.getType() != Material.BARREL) return;

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (!step.containsKey(id)) {
            step.put(id, "WAIT");
            p.sendMessage("§aもう一度殴るとショップを作成します");
        } else if (step.get(id).equals("WAIT")) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage("§c売りたいアイテムを手に持ってください");
                return;
            }
            tempItem.put(id, item.clone());
            tempChest.put(id, block.getLocation());
            step.put(id, "PRICE");
            p.sendMessage("§e何Yenで " + item.getType().name() + " を売りますか？ チャットで入力してください");
        }
    }

    // チャット処理
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (!step.containsKey(id)) return;

        String mode = step.get(id);
        e.setCancelled(true);

        if (mode.equals("PRICE")) {
            try {
                double price = Double.parseDouble(e.getMessage());
                tempPrice.put(id, price);
                step.put(id, "AMOUNT");
                p.sendMessage("§e何個/" + price + "Yen で売りますか？ 数字を入力してください");
            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
        } else if (mode.equals("AMOUNT")) {
            try {
                int amount = Integer.parseInt(e.getMessage());
                double price = tempPrice.get(id);
                ItemStack item = tempItem.get(id);
                Location chest = tempChest.get(id);
                Location signLoc = chest.clone().add(0, 1, 0);

                plugin.getShopManager().saveShop(id, p.getName(), chest, signLoc, item.getType().name(), amount, price);
                p.sendMessage("§aショップを作成しました！");
                step.remove(id);
            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
        }
    }

    // 看板クリック購入処理（簡略版）
    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!(e.getClickedBlock().getState() instanceof Sign sign)) return;

        Player p = e.getPlayer();
        Location signLoc = e.getClickedBlock().getLocation();
        Map<String, Object> shop = plugin.getShopManager().getShopAt(signLoc);
        if (shop == null) return;

        p.sendMessage(ChatColor.GOLD + "=== ショップ情報 ===");
        p.sendMessage("§aオーナー: " + shop.get("owner"));
        p.sendMessage("§e商品: " + shop.get("item"));
        p.sendMessage("§e個数/価格: " + shop.get("amount") + "個 / " + shop.get("price") + " Yen");
        p.sendMessage("§e購入する場合はチャットに個数を入力してください");

        step.put(p.getUniqueId(), "BUY:" + signLoc.getWorld().getName() + "," +
                signLoc.getBlockX() + "," + signLoc.getBlockY() + "," + signLoc.getBlockZ());
    }
}
