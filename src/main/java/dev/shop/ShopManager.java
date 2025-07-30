package dev.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {

    private final ShopPlugin plugin;
    private final File shopsFile;
    private YamlConfiguration shopsConfig;

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopsFile.exists()) {
            try {
                shopsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.shopsConfig = YamlConfiguration.loadConfiguration(shopsFile);
    }

    public void saveShop(UUID owner, String ownerName, Location chest, Location sign, String item, int amount, double price) {
        String id = UUID.randomUUID().toString();
        shopsConfig.set("shops." + id + ".owner", ownerName);
        shopsConfig.set("shops." + id + ".uuid", owner.toString());
        shopsConfig.set("shops." + id + ".chest", locToString(chest));
        shopsConfig.set("shops." + id + ".sign", locToString(sign));
        shopsConfig.set("shops." + id + ".item", item);
        shopsConfig.set("shops." + id + ".amount", amount);
        shopsConfig.set("shops." + id + ".price", price);
        save();
    }

    public Map<String, Object> getShopAt(Location signLoc) {
        if (shopsConfig.getConfigurationSection("shops") == null) return null;
        for (String id : shopsConfig.getConfigurationSection("shops").getKeys(false)) {
            Location loc = stringToLoc(shopsConfig.getString("shops." + id + ".sign"));
            if (loc != null && loc.equals(signLoc)) {
                return shopsConfig.getConfigurationSection("shops." + id).getValues(true);
            }
        }
        return null;
    }

    public void save() {
        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLoc(String s) {
        if (s == null) return null;
        String[] split = s.split(",");
        return new Location(Bukkit.getWorld(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3]));
    }
}
