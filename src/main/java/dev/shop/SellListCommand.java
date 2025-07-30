package dev.shop.commands;

import dev.shop.ShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellListCommand implements CommandExecutor {

    private final ShopPlugin plugin;
    private final SellCommand sellCommand;

    public SellListCommand(ShopPlugin plugin, SellCommand sellCommand) {
        this.plugin = plugin;
        this.sellCommand = sellCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        p.sendMessage(ChatColor.YELLOW + "=== 売却可能アイテム一覧 ===");
        sellCommand.getSellPrices().forEach((material, price) -> {
            p.sendMessage(ChatColor.AQUA + material.name() + ": " + ChatColor.GREEN + price + " Yen");
        });
        p.sendMessage(ChatColor.RED + "[注意] 手に持っている売却できるアイテムはすべて売却されます。一つずつ売りたい場合は手持ちを1個にしてください。");
        return true;
    }
}
