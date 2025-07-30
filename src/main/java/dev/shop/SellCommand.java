package dev.shop.commands;

import dev.shop.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SellCommand implements CommandExecutor {

    private final ShopPlugin plugin;
    private final Map<Material, Double> sellPrices = new HashMap<>();

    public SellCommand(ShopPlugin plugin) {
        this.plugin = plugin;

        // 売却リストを登録
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cプレイヤーのみが使用できます");
            return true;
        }
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage("§c手に売却できるアイテムを持ってください");
            return true;
        }

        Material type = item.getType();
        if (sellPrices.containsKey(type)) {
            int amount = item.getAmount();
            double price = sellPrices.get(type) * amount;

            plugin.getEconomyManager().addMoney(p, price);
            p.getInventory().setItemInMainHand(null);

            p.sendMessage("§a" + amount + "個の " + type.name() + " を " + price + " Yen で売却しました！");
        } else {
            p.sendMessage("§eこのアイテムは売却できません");
        }
        return true;
    }

    public Map<Material, Double> getSellPrices() {
        return sellPrices;
    }
}
