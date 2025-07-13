package io.github.AzmiiD.azBounty.listeners;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.managers.BountyManager;
import io.github.AzmiiD.azBounty.model.Bounty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final BountyManager bountyManager;

    public PlayerDeathListener() {
        this.bountyManager = AzBounty.getInstance().getBountyManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Check if the victim had a bounty
        Bounty bounty = bountyManager.getBounty(victim);
        if (bounty == null) {
            return;
        }

        // Check if the killer is a valid player
        if (killer == null || killer.equals(victim)) {
            return; // Bounty not claimed by a valid player
        }

        bountyManager.claimBounty(bounty, killer);
    }
}