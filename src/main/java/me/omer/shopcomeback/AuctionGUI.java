package me.omer.shopcomeback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuctionGUI implements Listener {

    private final Shopcomeback plugin;
    private final AuctionManager manager;
    private final EconomyManager economy;
    private final NamespacedKey auctionIdKey;

    public enum SortMode {
        NEWEST("En Yeni İlanlar"),
        PRICE_LOW_HIGH("En Ucuzdan En Pahalıya"),
        PRICE_HIGH_LOW("En Pahalıdan En Ucuza");

        private final String displayName;
        SortMode(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        public SortMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    public AuctionGUI(Shopcomeback plugin, AuctionManager manager, EconomyManager economy) {
        this.plugin = plugin;
        this.manager = manager;
        this.economy = economy;
        this.auctionIdKey = new NamespacedKey(plugin, "auction_id");
    }

    public static class AuctionHolder implements InventoryHolder {
        private final int page;
        private final boolean onlyMyItems;
        private final SortMode sortMode;

        public AuctionHolder(int page, boolean onlyMyItems, SortMode sortMode) {
            this.page = page;
            this.onlyMyItems = onlyMyItems;
            this.sortMode = sortMode;
        }

        public int getPage() { return page; }
        public boolean isOnlyMyItems() { return onlyMyItems; }
        public SortMode getSortMode() { return sortMode; }

        @Override
        public @NotNull Inventory getInventory() { return null; }
    }

    public void openGUI(Player player, int page) {
        openGUI(player, page, false, SortMode.NEWEST);
    }

    public void openGUI(Player player, int page, boolean onlyMyItems, SortMode sortMode) {
        List<AuctionItem> allItems = manager.getActiveItems();

        List<AuctionItem> filtered = new ArrayList<>(onlyMyItems
                ? allItems.stream().filter(i -> i.getSellerUUID().equals(player.getUniqueId())).collect(Collectors.toList())
                : allItems);

        if (sortMode == SortMode.PRICE_LOW_HIGH) {
            filtered.sort(Comparator.comparingDouble(AuctionItem::getPrice));
        } else if (sortMode == SortMode.PRICE_HIGH_LOW) {
            filtered.sort(Comparator.comparingDouble(AuctionItem::getPrice).reversed());
        } else {
            Collections.reverse(filtered);
        }

        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / itemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String titlePrefix = onlyMyItems ? "Eşyalarım" : "İhale Pazarı";
        Inventory inv = Bukkit.createInventory(
                new AuctionHolder(page, onlyMyItems, sortMode),
                54,
                Component.text(titlePrefix + " (Sayfa " + (page + 1) + "/" + totalPages + ")", NamedTextColor.DARK_GRAY)
        );

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filtered.size());

        for (int i = start; i < end; i++) {
            AuctionItem auction = filtered.get(i);
            ItemStack displayItem = auction.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();

            List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Satıcı: ", NamedTextColor.GRAY).append(Component.text(auction.getSellerName(), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Fiyat: ", NamedTextColor.GRAY).append(Component.text(auction.getPrice() + " TL", NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());

            if (auction.getSellerUUID().equals(player.getUniqueId())) {
                lore.add(Component.text("» Geri çekmek için tıkla!", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("» Satın almak için tıkla!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lore);
            meta.getPersistentDataContainer().set(auctionIdKey, PersistentDataType.STRING, auction.getId().toString());
            displayItem.setItemMeta(meta);

            inv.setItem(i - start, displayItem);
        }

        ItemStack glass = createButton(Material.BLACK_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY, null);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, "« Önceki Sayfa", NamedTextColor.YELLOW, null));
        }

        long myItemCount = allItems.stream().filter(i -> i.getSellerUUID().equals(player.getUniqueId())).count();
        List<Component> chestLore = List.of(
                Component.text("Satıştaki eşyaların: ", NamedTextColor.GRAY)
                        .append(Component.text(myItemCount + " adet", NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text(onlyMyItems ? "» Tüm pazara dönmek için tıkla!" : "» Kendi eşyalarını görmek için tıkla!",
                        onlyMyItems ? NamedTextColor.AQUA : NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
        );
        inv.setItem(46, createButton(
                onlyMyItems ? Material.ENDER_CHEST : Material.CHEST,
                onlyMyItems ? "Filtre: Sadece Eşyalarım" : "Sattığım Eşyalar",
                NamedTextColor.GOLD,
                chestLore
        ));

        List<Component> sortLore = new ArrayList<>();
        for (SortMode mode : SortMode.values()) {
            boolean isCurrent = (mode == sortMode);
            sortLore.add(Component.text((isCurrent ? "▶ " : "  ") + mode.getDisplayName(),
                    isCurrent ? NamedTextColor.GREEN : NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        sortLore.add(Component.empty());
        sortLore.add(Component.text("» Sıralamayı değiştirmek için tıkla!", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        inv.setItem(47, createButton(Material.REPEATER, "Sıralama: " + sortMode.getDisplayName(), NamedTextColor.RED, sortLore));

        double balance = economy.getBalance(player.getUniqueId());
        inv.setItem(49, createButton(Material.GOLD_INGOT, "Bakiyen: " + balance + " TL", NamedTextColor.GOLD,
                List.of(Component.text("Eşya satmak için: /ah sell <fiyat>", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))));

        inv.setItem(51, createButton(Material.EMERALD, "Pazarı Yenile", NamedTextColor.GREEN,
                List.of(Component.text("» Güncel ilanları yüklemek için tıkla!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))));

        double totalMarketValue = allItems.stream().mapToDouble(AuctionItem::getPrice).sum();
        List<Component> infoLore = List.of(
                Component.text("Toplam İlan: ", NamedTextColor.GRAY).append(Component.text(allItems.size() + " eşya", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
                Component.text("Piyasa Hacmi: ", NamedTextColor.GRAY).append(Component.text(totalMarketValue + " TL", NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false)
        );
        inv.setItem(52, createButton(Material.COMPASS, "Pazar İstatistikleri", NamedTextColor.AQUA, infoLore));

        if (page < totalPages - 1) {
            inv.setItem(53, createButton(Material.ARROW, "Sonraki Sayfa »", NamedTextColor.YELLOW, null));
        }

        player.openInventory(inv);
    }

    private ItemStack createButton(Material mat, String name, NamedTextColor color, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AuctionHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == 45 && clicked.getType() == Material.ARROW) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openGUI(player, holder.getPage() - 1, holder.isOnlyMyItems(), holder.getSortMode());
            return;
        }

        if (slot == 46) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
            openGUI(player, 0, !holder.isOnlyMyItems(), holder.getSortMode());
            return;
        }

        if (slot == 47) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.3f);
            openGUI(player, 0, holder.isOnlyMyItems(), holder.getSortMode().next());
            return;
        }

        if (slot == 51) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
            openGUI(player, holder.getPage(), holder.isOnlyMyItems(), holder.getSortMode());
            return;
        }

        if (slot == 53 && clicked.getType() == Material.ARROW) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openGUI(player, holder.getPage() + 1, holder.isOnlyMyItems(), holder.getSortMode());
            return;
        }

        if (slot < 45 && clicked.hasItemMeta()) {
            String uuidStr = clicked.getItemMeta().getPersistentDataContainer().get(auctionIdKey, PersistentDataType.STRING);
            if (uuidStr != null) {
                UUID auctionId = UUID.fromString(uuidStr);
                if (manager.buyItem(player, auctionId, economy)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                    openGUI(player, holder.getPage(), holder.isOnlyMyItems(), holder.getSortMode());
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                }
            }
        }
    }
}
