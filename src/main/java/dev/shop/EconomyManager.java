package dev.shop;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class EconomyManager {
    private final ShopPlugin plugin;

    public EconomyManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    private File getPlayerFile(Player p) {
        File dir = new File(plugin.getDataFolder(), "playerfile");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, p.getUniqueId().toString() + ".yml");
    }

    private YamlConfiguration getPlayerConfig(Player p) {
        return YamlConfiguration.loadConfiguration(getPlayerFile(p));
    }

    public double getMoney(Player p) {
        return getPlayerConfig(p).getDouble("money", 0);
    }

    public void setMoney(Player p, double amount) {
        YamlConfiguration config = getPlayerConfig(p);
        config.set("money", amount);
        try {
            config.save(getPlayerFile(p));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMoney(Player p, double amount) {
        setMoney(p, getMoney(p) + amount);
    }

    public void removeMoney(Player p, double amount) {
        setMoney(p, Math.max(0, getMoney(p) - amount));
    }
}
