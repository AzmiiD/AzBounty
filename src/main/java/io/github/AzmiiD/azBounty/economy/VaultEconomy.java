package io.github.AzmiiD.azBounty.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class VaultEconomy {

    private Economy econ = null;

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return econ.has(player, amount);
    }

    public EconomyResponse withdraw(OfflinePlayer player, double amount) {
        return econ.withdrawPlayer(player, amount);
    }

    public EconomyResponse deposit(OfflinePlayer player, double amount) {
        return econ.depositPlayer(player, amount);
    }
}
