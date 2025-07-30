package dev.shop.commands;

import dev.shop.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public SellCommand(ShopPlugin plugin) {
        this.plugin = plugin;
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

        // 例: 石炭を 0.005 Yen で売る
        if (item.getType() == Material.COAL) {
            int amount = item.getAmount();
            double price = 0.005 * amount;
            plugin.getEconomyManager().addMoney(p, price);
            p.getInventory().setItemInMainHand(null);
            p.sendMessage("§a" + amount + "個の石炭を " + price + " Yen で売却しました！");
        } else {
            p.sendMessage("§eこのアイテムは売却できません");
        }
        return true;
    }
}
