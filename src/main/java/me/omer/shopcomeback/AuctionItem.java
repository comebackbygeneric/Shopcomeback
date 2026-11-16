package me.omer.shopcomeback;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class AuctionItem {
    private final UUID id;
    private final UUID sellerUUID;
    private final String sellerName;
    private final double price;
    private final ItemStack item;

    public AuctionItem(UUID id, UUID sellerUUID, String sellerName, double price, ItemStack item) {
        this.id = id;
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.price = price;
        this.item = item;
    }

    public UUID getId() { return id; }
    public UUID getSellerUUID() { return sellerUUID; }
    public String getSellerName() { return sellerName; }
    public double getPrice() { return price; }
    public ItemStack getItem() { return item; }
}
