package io.github.AzmiiD.azBounty.gui;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.managers.ConfigManager;
import io.github.AzmiiD.azBounty.managers.BountyManager;
import io.github.AzmiiD.azBounty.model.CompletedBounty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BountyHistoryGUI {

    private final AzBounty plugin = AzBounty.getInstance();
    private final ConfigManager configManager = plugin.getConfigManager();
    private final BountyManager bountyManager = plugin.getBountyManager();

    public void open(Player player, String targetPlayerName) {
        List<CompletedBounty> history = bountyManager.getPlayerHistory(targetPlayerName);

        if (history.isEmpty()) {
            player.sendMessage(configManager.getMessage("no-bounty-history"));
            return;
        }

        String title = configManager.getMessage("bounty-history-for").replace("{player}", targetPlayerName);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        for (CompletedBounty bounty : history) {
            ItemStack item = new ItemStack(Material.PAPER, 1);
            ItemMeta meta = item.getItemMeta();

            String name = configManager.getMessage("history-item.name")
                    .replace("{target}", bounty.getTargetName());
            meta.setDisplayName(name);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
            String date = sdf.format(new Date(bounty.getCompletionTimestamp()));

            List<String> lore = configManager.getMessageList("history-item.lore").stream()
                    .map(line -> line.replace("{creator}", bounty.getCreatorName()))
                    .map(line -> line.replace("{target}", bounty.getTargetName()))
                    .map(line -> line.replace("{claimer}", bounty.getClaimerName()))
                    .map(line -> line.replace("{amount}", String.format("%.2f", bounty.getReward())))
                    .map(line -> line.replace("{date}", date))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        player.openInventory(gui);
    }
}