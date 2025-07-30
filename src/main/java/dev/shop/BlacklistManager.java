package dev.shop;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BlacklistManager {

    private final File file;
    private final YamlConfiguration config;

    public BlacklistManager(ShopPlugin plugin) {
        file = new File(plugin.getDataFolder(), "blacklist.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void addBlacklist(String owner, String target) {
        List<String> list = config.getStringList("blacklist." + owner);
        if (!list.contains(target)) {
            list.add(target);
            config.set("blacklist." + owner, list);
            save();
        }
    }

    public void removeBlacklist(String owner, String target) {
        List<String> list = config.getStringList("blacklist." + owner);
        list.remove(target);
        config.set("blacklist." + owner, list);
        save();
    }

    public boolean isBlacklisted(String owner, String target) {
        return config.getStringList("blacklist." + owner).contains(target);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
