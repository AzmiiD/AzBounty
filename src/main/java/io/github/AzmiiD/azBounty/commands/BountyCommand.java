package io.github.AzmiiD.azBounty.commands;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.economy.VaultEconomy;
import io.github.AzmiiD.azBounty.gui.BountyBoardGUI;
import io.github.AzmiiD.azBounty.gui.BountyHistoryGUI;
import io.github.AzmiiD.azBounty.managers.BountyManager;
import io.github.AzmiiD.azBounty.managers.ConfigManager;
import io.github.AzmiiD.azBounty.model.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final AzBounty plugin = AzBounty.getInstance();
    private final ConfigManager configManager = plugin.getConfigManager();
    private final BountyManager bountyManager = plugin.getBountyManager();
    private final VaultEconomy economy = plugin.getEconomy();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdLabel = command.getLabel().toLowerCase();

        if (cmdLabel.equals("bounties") || cmdLabel.equals("bountyboard") || cmdLabel.equals("cb")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getMessage("player-only-command"));
                return true;
            }
            new BountyBoardGUI().open((Player) sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(configManager.getMessage("usage.bounty"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "place":
                handlePlace(sender, args);
                break;
            case "cancel":
                handleCancel(sender);
                break;
            case "history":
                handleHistory(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(configManager.getMessage("usage.bounty"));
                break;
        }

        return true;
    }

    private void handlePlace(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("player-only-command"));
            return;
        }
        if (!sender.hasPermission("codellabounty.place")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(configManager.getMessage("usage.place"));
            return;
        }

        Player creator = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found").replace("{target}", args[1]));
            return;
        }
        if (target.equals(creator) && !configManager.getConfig().getBoolean("allow-self-bounty")) {
            sender.sendMessage(configManager.getMessage("cannot-bounty-self"));
            return;
        }
        if (bountyManager.getBounty(target) != null) {
            sender.sendMessage(configManager.getMessage("target-already-has-bounty"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount"));
            return;
        }

        double minAmount = configManager.getConfig().getDouble("min-bounty-amount");
        double maxAmount = configManager.getConfig().getDouble("max-bounty-amount");

        if (amount < minAmount) {
            sender.sendMessage(configManager.getMessage("min-bounty").replace("{amount}", String.valueOf(minAmount)));
            return;
        }
        if (maxAmount > 0 && amount > maxAmount) {
            sender.sendMessage(configManager.getMessage("max-bounty").replace("{amount}", String.valueOf(maxAmount)));
            return;
        }

        double taxPercent = configManager.getConfig().getDouble("bounty-tax-percent");
        double totalCost = amount * (1 + taxPercent / 100);

        if (!economy.has(creator, totalCost)) {
            sender.sendMessage(configManager.getMessage("not-enough-money").replace("{amount}", String.format("%.2f", totalCost)));
            return;
        }

        economy.withdraw(creator, totalCost);
        bountyManager.placeBounty(creator, target, amount);

        creator.sendMessage(configManager.getMessage("bounty-placed")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{target}", target.getName()));

        long duration = configManager.getConfig().getLong("bounty-duration-minutes") * 60 * 1000;
        Bukkit.broadcastMessage(configManager.getMessage("bounty-placed-broadcast")
                .replace("{creator}", creator.getName())
                .replace("{target}", target.getName())
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{time}", bountyManager.formatDuration(duration)));
    }

    private void handleCancel(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("player-only-command"));
            return;
        }
        if (!sender.hasPermission("codellabounty.cancel")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return;
        }

        Player player = (Player) sender;
        Bounty bounty = bountyManager.getBountyByCreator(player.getUniqueId());

        if (bounty == null) {
            sender.sendMessage(configManager.getMessage("no-active-bounty-to-cancel"));
            return;
        }

        bountyManager.cancelBounty(bounty);
        economy.deposit(player, bounty.getReward()); // Refund, tax is not refunded

        sender.sendMessage(configManager.getMessage("bounty-cancelled").replace("{target}", bounty.getTargetName()));
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("player-only-command"));
            return;
        }
        if (!sender.hasPermission("codellabounty.history")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return;
        }

        Player player = (Player) sender;
        String targetName;

        if (args.length == 1) {
            targetName = player.getName();
        } else {
            targetName = args[1];
        }

        new BountyHistoryGUI().open(player, targetName);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("codellabounty.reload")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return;
        }
        configManager.reloadConfigs();
        sender.sendMessage(configManager.getMessage("config-reloaded"));
    }
}
