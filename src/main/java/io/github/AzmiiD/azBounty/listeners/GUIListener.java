package io.github.AzmiiD.azBounty.listeners;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class GUIListener implements Listener {

    private final ConfigManager configManager = AzBounty.getInstance().getConfigManager();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        List<String> protectedTitles = Arrays.asList(
                configManager.getMessage("bounty-board-title"),
                configManager.getMessage("bounty-history-title")
        );

        if (protectedTitles.contains(inventoryTitle)) {
            event.setCancelled(true);
        }
    }
}
