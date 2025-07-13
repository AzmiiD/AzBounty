package io.github.AzmiiD.azBounty;

import io.github.AzmiiD.azBounty.commands.BountyCommand;
import io.github.AzmiiD.azBounty.listeners.GUIListener;
import io.github.AzmiiD.azBounty.commands.BountyTabCompleter;
import io.github.AzmiiD.azBounty.economy.VaultEconomy;
import io.github.AzmiiD.azBounty.listeners.PlayerDeathListener;
import io.github.AzmiiD.azBounty.managers.BountyManager;
import io.github.AzmiiD.azBounty.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class AzBounty extends JavaPlugin {
    private static AzBounty instance;
    private VaultEconomy economy;
    private ConfigManager configManager;
    private BountyManager bountyManager;

    public void onEnable() {
        instance = this;
        this.economy = new VaultEconomy();
        if (!this.economy.setupEconomy()) {
            this.getLogger().severe("Vault dependency not found! Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
        } else {
            this.configManager = new ConfigManager();
            this.bountyManager = new BountyManager();
            Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
            this.getCommand("bounty").setExecutor(new BountyCommand());
            this.getCommand("bounties").setExecutor(new BountyCommand());
            this.getCommand("bounty").setTabCompleter(new BountyTabCompleter());
            this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
            this.getLogger().info("AzBounty has been enabled!");
        }
    }

    public void onDisable() {
        if (this.bountyManager != null) {
            this.bountyManager.saveBounties();
        }

        this.getLogger().info("AzBounty has been disabled!");
    }

    public static AzBounty getInstance() {
        return instance;
    }

    public VaultEconomy getEconomy() {
        return this.economy;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public BountyManager getBountyManager() {
        return this.bountyManager;
    }
}
