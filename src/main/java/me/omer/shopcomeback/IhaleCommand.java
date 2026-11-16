package me.omer.shopcomeback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IhaleCommand implements CommandExecutor, TabCompleter {

    private final Shopcomeback plugin;
    private final AuctionManager manager;
    private final EconomyManager economy;
    private final AuctionGUI gui;

    public IhaleCommand(Shopcomeback plugin, AuctionManager manager, EconomyManager economy, AuctionGUI gui) {
        this.plugin = plugin;
        this.manager = manager;
        this.economy = economy;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("para")) {
            double bal = economy.getBalance(player.getUniqueId());
            player.sendMessage(Component.text("Mevcut bakiyen: ", NamedTextColor.GRAY)
                    .append(Component.text(bal + " TL", NamedTextColor.GOLD)));
            return true;
        }

        if (command.getName().equalsIgnoreCase("paragonder")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Kullanım: /paragonder <oyuncu> <miktar>", NamedTextColor.RED));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(Component.text("Bu oyuncu şu anda sunucuda değil!", NamedTextColor.RED));
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(Component.text("Kendine para gönderemezsin!", NamedTextColor.RED));
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                    player.sendMessage(Component.text("0'dan büyük geçerli bir miktar girmelisin!", NamedTextColor.RED));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Miktar sadece sayı olmalıdır!", NamedTextColor.RED));
                return true;
            }

            if (!economy.removeBalance(player.getUniqueId(), amount)) {
                player.sendMessage(Component.text("Yeterli bakiyen yok!", NamedTextColor.RED));
                return true;
            }

            economy.addBalance(target.getUniqueId(), amount);
            player.sendMessage(Component.text(target.getName() + " adlı oyuncuya ", NamedTextColor.GREEN)
                    .append(Component.text(amount + " TL", NamedTextColor.GOLD))
                    .append(Component.text(" gönderdin!", NamedTextColor.GREEN)));

            target.sendMessage(Component.text(player.getName() + " sana ", NamedTextColor.GREEN)
                    .append(Component.text(amount + " TL", NamedTextColor.GOLD))
                    .append(Component.text(" gönderdi!", NamedTextColor.GREEN)));
            return true;
        }

        if (args.length == 0) {
            gui.openGUI(player, 0);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("sat")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Kullanım: /" + label + " sell <fiyat>", NamedTextColor.RED));
                return true;
            }

            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType() == Material.AIR || handItem.getAmount() <= 0) {
                player.sendMessage(Component.text("Satmak istediğin eşyayı elinde tutmalısın!", NamedTextColor.RED));
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
                if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
                    player.sendMessage(Component.text("Geçerli ve 0'dan büyük bir fiyat girmelisin!", NamedTextColor.RED));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Fiyat sadece sayı olmalıdır! Örnek: /ah sell 500", NamedTextColor.RED));
                return true;
            }

            manager.addItem(player, handItem, price);
            player.getInventory().setItemInMainHand(null);

            player.sendMessage(Component.text("Elindeki eşya ", NamedTextColor.GREEN)
                    .append(Component.text(price + " TL", NamedTextColor.GOLD))
                    .append(Component.text(" fiyatıyla ihaleye çıkarıldı!", NamedTextColor.GREEN)));
            return true;
        }

        player.sendMessage(Component.text("Kullanım: /" + label + " veya /" + label + " sell <fiyat>", NamedTextColor.RED));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("ihale") || command.getName().equalsIgnoreCase("ah") || command.getName().equalsIgnoreCase("pazar")) {
            if (args.length == 1) {
                return List.of("sell", "sat");
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("sat"))) {
                return List.of("100", "500", "1000", "5000");
            }
        }
        if (command.getName().equalsIgnoreCase("paragonder")) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                return names;
            }
            if (args.length == 2) {
                return List.of("100", "500", "1000");
            }
        }
        return Collections.emptyList();
    }
}
