package me.omer.shopcomeback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AuctionManager {

    private final Shopcomeback plugin;
    private final List<AuctionItem> activeItems = new ArrayList<>();
    private final File dataFile;

    public AuctionManager(Shopcomeback plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "items.yml");
    }

    public List<AuctionItem> getActiveItems() {
        return activeItems;
    }

    public void addItem(Player seller, ItemStack item, double price) {
        AuctionItem auctionItem = new AuctionItem(
                UUID.randomUUID(),
                seller.getUniqueId(),
                seller.getName(),
                price,
                item.clone()
        );
        activeItems.add(auctionItem);
        saveItems();
    }

    public boolean buyItem(Player buyer, UUID auctionId, EconomyManager econ) {
        Optional<AuctionItem> optItem = activeItems.stream()
                .filter(i -> i.getId().equals(auctionId))
                .findFirst();

        if (optItem.isEmpty()) {
            buyer.sendMessage(Component.text("Bu eşya az önce satıldı veya kaldırıldı!", NamedTextColor.RED));
            return false;
        }

        AuctionItem auctionItem = optItem.get();

        if (auctionItem.getSellerUUID().equals(buyer.getUniqueId())) {
            if (buyer.getInventory().firstEmpty() == -1) {
                buyer.sendMessage(Component.text("Envanterinde boş yer yok!", NamedTextColor.RED));
                return false;
            }
            activeItems.remove(auctionItem);
            buyer.getInventory().addItem(auctionItem.getItem());
            buyer.sendMessage(Component.text("Eşyanı ihaleden geri çektin.", NamedTextColor.YELLOW));
            saveItems();
            return true;
        }

        if (econ.getBalance(buyer.getUniqueId()) < auctionItem.getPrice()) {
            buyer.sendMessage(Component.text("Yeterli paran yok! Gereken: " + auctionItem.getPrice() + " TL", NamedTextColor.RED));
            return false;
        }

        if (buyer.getInventory().firstEmpty() == -1) {
            buyer.sendMessage(Component.text("Envanterinde boş yer yok!", NamedTextColor.RED));
            return false;
        }

        econ.removeBalance(buyer.getUniqueId(), auctionItem.getPrice());
        econ.addBalance(auctionItem.getSellerUUID(), auctionItem.getPrice());

        activeItems.remove(auctionItem);
        buyer.getInventory().addItem(auctionItem.getItem());
        buyer.sendMessage(Component.text("Eşyayı " + auctionItem.getPrice() + " TL karşılığında satın aldın!", NamedTextColor.GREEN));

        OfflinePlayer seller = Bukkit.getOfflinePlayer(auctionItem.getSellerUUID());
        if (seller.isOnline() && seller.getPlayer() != null) {
            seller.getPlayer().sendMessage(Component.text(
                    buyer.getName() + " senin ihaledeki eşyanı " + auctionItem.getPrice() + " TL'ye satın aldı!",
                    NamedTextColor.GREEN
            ));
        }

        saveItems();
        return true;
    }

    public void saveItems() {
        YamlConfiguration config = new YamlConfiguration();
        for (AuctionItem item : activeItems) {
            String path = "items." + item.getId().toString();
            config.set(path + ".sellerUUID", item.getSellerUUID().toString());
            config.set(path + ".sellerName", item.getSellerName());
            config.set(path + ".price", item.getPrice());
            config.set(path + ".item", item.getItem());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("İhale eşyaları kaydedilemedi: " + e.getMessage());
        }
    }

    public void loadItems() {
        activeItems.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                UUID sellerUUID = UUID.fromString(section.getString(key + ".sellerUUID"));
                String sellerName = section.getString(key + ".sellerName");
                double price = section.getDouble(key + ".price");
                ItemStack item = section.getItemStack(key + ".item");

                if (item != null) {
                    activeItems.add(new AuctionItem(id, sellerUUID, sellerName, price, item));
                }
            } catch (Exception ignored) {}
        }
    }
}
