package me.omer.shopcomeback;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final Shopcomeback plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private final double defaultBalance = 1000.0;

    public EconomyManager(Shopcomeback plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "economy.yml");
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, defaultBalance);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0.0, amount));
        saveEconomy();
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public void saveEconomy() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            config.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ekonomi verileri kaydedilemedi: " + e.getMessage());
        }
    }

    public void loadEconomy() {
        balances.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("balances");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = section.getDouble(key);
                balances.put(uuid, balance);
            } catch (Exception ignored) {}
        }
    }
}
