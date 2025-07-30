package dev.shop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
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

    // 購入処理用
    private final Map<UUID, Sign> pendingPurchase = new HashMap<>();

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ===== ショップ作成開始 =====
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

    // ===== チャット入力で値段/個数を設定 =====
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (!step.containsKey(id) && !pendingPurchase.containsKey(id)) return;

        e.setCancelled(true);

        // 購入処理待ち
        if (pendingPurchase.containsKey(id)) {
            try {
                int amount = Integer.parseInt(e.getMessage());
                Sign sign = pendingPurchase.remove(id);

                String owner = ChatColor.stripColor(sign.getLine(0));
                String itemName = sign.getLine(1);
                String[] split = sign.getLine(2).replace("Yen", "").split("個/");
                int shopAmount = Integer.parseInt(split[0].replace("個", "").trim());
                double price = Double.parseDouble(split[1].trim());

                double totalCost = (price / shopAmount) * amount;

                if (plugin.getEconomyManager().getMoney(p) < totalCost) {
                    p.sendMessage("§cお金が足りないため購入できません");
                    return;
                }

                plugin.getEconomyManager().removeMoney(p, totalCost);

                ItemStack item = new ItemStack(Material.matchMaterial(itemName), amount);
                p.getInventory().addItem(item);

                p.sendMessage("§a" + owner + " のショップから " + amount + "個購入しました！ -" + totalCost + " Yen");

                // TODO: 本当はオーナーにお金を渡して、チェストから在庫を減らす処理も追加すべき

            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
            return;
        }

        // ショップ作成処理
        String mode = step.get(id);

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

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block chestBlock = chest.getBlock();
                    if (!(chestBlock.getBlockData() instanceof Directional)) {
                        p.sendMessage("§cチェスト/樽ではありません");
                        cleanup(id);
                        return;
                    }

                    Directional dir = (Directional) chestBlock.getBlockData();
                    Location signLoc = chest.clone().add(dir.getFacing().getModX(), dir.getFacing().getModY(), dir.getFacing().getModZ());
                    Block signBlock = signLoc.getBlock();

                    if (signBlock.getType() != Material.AIR) {
                        p.sendMessage("§c看板を設置できるスペースがありません（正面がふさがっています）");
                        cleanup(id);
                        return;
                    }

                    signBlock.setType(Material.OAK_WALL_SIGN);
                    Sign sign = (Sign) signBlock.getState();
                    sign.setLine(0, ChatColor.GREEN + p.getName());
                    sign.setLine(1, item.getType().name());
                    sign.setLine(2, amount + "個/" + price + "Yen");
                    sign.update();

                    plugin.getShopManager().saveShop(id, p.getName(), chest, signLoc, item.getType().name(), amount, price);
                    p.sendMessage("§aショップを作成しました！（正面に看板設置）");

                    cleanup(id);
                });

            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
        }
    }

    // ===== 看板を左クリックで確認＆購入準備 =====
    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) e.getClickedBlock().getState();
        Player p = e.getPlayer();

        String owner = ChatColor.stripColor(sign.getLine(0));
        String itemName = sign.getLine(1);
        String amountPrice = sign.getLine(2);

        p.sendMessage("§e=== ショップ情報 ===");
        p.sendMessage("§a所有者: " + owner);
        p.sendMessage("§bアイテム: " + itemName);
        p.sendMessage("§d価格: " + amountPrice);
        p.sendMessage("§6購入する場合はチャット欄に購入個数を入力してください。");

        pendingPurchase.put(p.getUniqueId(), sign);
    }

    private void cleanup(UUID id) {
        step.remove(id);
        tempPrice.remove(id);
        tempItem.remove(id);
        tempChest.remove(id);
    }
}
