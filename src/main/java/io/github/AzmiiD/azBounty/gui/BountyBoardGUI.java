package io.github.AzmiiD.azBounty.gui;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.managers.ConfigManager;
import io.github.AzmiiD.azBounty.managers.BountyManager;
import io.github.AzmiiD.azBounty.model.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BountyBoardGUI {

    private final AzBounty plugin = AzBounty.getInstance();
    private final ConfigManager configManager = plugin.getConfigManager();
    private final BountyManager bountyManager = plugin.getBountyManager();

    public void open(Player player) {
        String title = configManager.getMessage("bounty-board-title");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        for (Bounty bounty : bountyManager.getActiveBounties()) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            meta.setOwningPlayer(Bukkit.getOfflinePlayer(bounty.getTargetUniqueId()));

            String name = configManager.getMessage("bounty-board-item.name")
                    .replace("{target}", bounty.getTargetName());
            meta.setDisplayName(name);

            long remainingTime = bounty.getExpirationTimestamp() - System.currentTimeMillis();
            String formattedTime = bountyManager.formatDuration(remainingTime);

            List<String> lore = configManager.getMessageList("bounty-board-item.lore").stream()
                    .map(line -> line.replace("{creator}", bounty.getCreatorName()))
                    .map(line -> line.replace("{target}", bounty.getTargetName()))
                    .map(line -> line.replace("{amount}", String.format("%.2f", bounty.getReward())))
                    .map(line -> line.replace("{time}", formattedTime))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        player.openInventory(gui);
    }
}