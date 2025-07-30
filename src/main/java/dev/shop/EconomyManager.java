package dev.shop;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class EconomyManager {

    private final ShopPlugin plugin;

    public EconomyManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    private File getPlayerFile(String name) {
        File folder = new File(plugin.getDataFolder(), "playerfile");
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, name + ".yml");
    }

    // --- 所持金を取得 ---
    public double getMoney(Player player) {
        return getMoney(player.getName());
    }

    public double getMoney(String playerName) {
        File file = getPlayerFile(playerName);
        if (!file.exists()) return 0;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getDouble("money", 0);
    }

    // --- 所持金を設定 ---
    public void setMoney(String playerName, double amount) {
        File file = getPlayerFile(playerName);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("money", amount);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- お金を追加 ---
    public void addMoney(Player player, double amount) {
        addMoney(player.getName(), amount);
    }

    public void addMoney(String playerName, double amount) {
        double current = getMoney(playerName);
        setMoney(playerName, current + amount);
    }

    // --- お金を減らす ---
    public void removeMoney(Player player, double amount) {
        removeMoney(player.getName(), amount);
    }

    public void removeMoney(String playerName, double amount) {
        double current = getMoney(playerName);
        setMoney(playerName, Math.max(0, current - amount));
    }
}
