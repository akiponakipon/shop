package dev.shop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private final Map<UUID, String> creatingShopStep = new HashMap<>();
    private final Map<UUID, ItemStack> creatingItem = new HashMap<>();
    private final Map<UUID, Location> chestLoc = new HashMap<>();

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestHit(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.BARREL) return;

        // 2回目の殴りで作成開始
        if (creatingShopStep.containsKey(p.getUniqueId())) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage("§c手に売りたいアイテムを持ってください");
                return;
            }
            creatingItem.put(p.getUniqueId(), item.clone());
            chestLoc.put(p.getUniqueId(), e.getClickedBlock().getLocation());
            creatingShopStep.put(p.getUniqueId(), "PRICE");
            p.sendMessage("§e何Yenで " + item.getType().name() + " を売りますか？チャットに入力してください");
        } else {
            creatingShopStep.put(p.getUniqueId(), "WAITING");
            p.sendMessage("§aもう一度殴ってショップ作成を開始します");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (!creatingShopStep.containsKey(id)) return;

        e.setCancelled(true);
        String step = creatingShopStep.get(id);
        if (step.equals("PRICE")) {
            try {
                double price = Double.parseDouble(e.getMessage());
                creatingShopStep.put(id, "AMOUNT");
                p.sendMessage("§e何個/" + price + "Yen で売りますか？ 数字を入力してください");
                // 価格を一時保存する方法は省略（Mapなどで保持）
            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
        } else if (step.equals("AMOUNT")) {
            try {
                int amount = Integer.parseInt(e.getMessage());
                ItemStack item = creatingItem.get(id);
                Location chest = chestLoc.get(id);
                Location signLoc = chest.clone().add(0, 1, 0); // 仮：チェストの上に設置
                // 看板設置処理 & shops.yml保存
                p.sendMessage("§aショップを作成しました！");
                creatingShopStep.remove(id);
                creatingItem.remove(id);
                chestLoc.remove(id);
            } catch (NumberFormatException ex) {
                p.sendMessage("§c数字を入力してください");
            }
        }
    }
}
