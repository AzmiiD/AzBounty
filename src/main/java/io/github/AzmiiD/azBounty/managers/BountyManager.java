package io.github.AzmiiD.azBounty.managers;

import io.github.AzmiiD.azBounty.AzBounty;
import io.github.AzmiiD.azBounty.model.Bounty;
import io.github.AzmiiD.azBounty.model.CompletedBounty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BountyManager {

    private final AzBounty plugin;
    private final Map<UUID, Bounty> activeBounties = new HashMap<>(); // Key: Target UUID
    private final List<CompletedBounty> completedBounties = new ArrayList<>();
    private final File bountiesFile;
    private final FileConfiguration bountiesConfig;

    public BountyManager() {
        this.plugin = AzBounty.getInstance();
        this.bountiesFile = new File(plugin.getDataFolder(), "bounties.yml");
        if (!bountiesFile.exists()) {
            try {
                bountiesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.bountiesConfig = YamlConfiguration.loadConfiguration(bountiesFile);
        loadBounties();
        startExpirationTask();
    }

    public void placeBounty(Player creator, Player target, double amount) {
        long durationMillis = plugin.getConfigManager().getConfig().getLong("bounty-duration-minutes") * 60 * 1000;
        long expiration = (durationMillis == 0) ? 0 : System.currentTimeMillis() + durationMillis;

        Bounty bounty = new Bounty(creator.getUniqueId(), creator.getName(), target.getUniqueId(), target.getName(), amount, System.currentTimeMillis(), expiration);
        activeBounties.put(target.getUniqueId(), bounty);
    }

    public void cancelBounty(Bounty bounty) {
        activeBounties.remove(bounty.getTargetUniqueId());
    }

    public void claimBounty(Bounty bounty, Player killer) {
        activeBounties.remove(bounty.getTargetUniqueId());
        completedBounties.add(new CompletedBounty(bounty.getCreatorName(), bounty.getTargetName(), killer.getName(), bounty.getReward(), System.currentTimeMillis()));

        plugin.getEconomy().deposit(killer, bounty.getReward());

        String message = plugin.getConfigManager().getMessage("bounty-claimed")
                .replace("{killer}", killer.getName())
                .replace("{target}", bounty.getTargetName())
                .replace("{amount}", String.format("%.2f", bounty.getReward()));
        Bukkit.broadcastMessage(message);
    }

    public Bounty getBounty(Player target) {
        return activeBounties.get(target.getUniqueId());
    }

    public Bounty getBountyByCreator(UUID creatorId) {
        return activeBounties.values().stream()
                .filter(b -> b.getCreatorUniqueId().equals(creatorId))
                .findFirst().orElse(null);
    }

    public Collection<Bounty> getActiveBounties() {
        return activeBounties.values();
    }

    public List<CompletedBounty> getCompletedBounties() {
        return completedBounties;
    }

    public List<CompletedBounty> getPlayerHistory(String playerName) {
        return completedBounties.stream()
                .filter(b -> b.getTargetName().equalsIgnoreCase(playerName) || b.getCreatorName().equalsIgnoreCase(playerName) || b.getClaimerName().equalsIgnoreCase(playerName))
                .sorted(Comparator.comparingLong(CompletedBounty::getCompletionTimestamp).reversed())
                .collect(Collectors.toList());
    }


    private void startExpirationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Bounty> toRemove = new ArrayList<>();
                for (Bounty bounty : activeBounties.values()) {
                    if (bounty.isExpired()) {
                        toRemove.add(bounty);
                        OfflinePlayer creator = Bukkit.getOfflinePlayer(bounty.getCreatorUniqueId());
                        plugin.getEconomy().deposit(creator, bounty.getReward()); // Refund the creator
                        String message = plugin.getConfigManager().getMessage("bounty-expired")
                                .replace("{target}", bounty.getTargetName())
                                .replace("{creator}", bounty.getCreatorName());
                        Bukkit.broadcastMessage(message);
                    }
                }
                toRemove.forEach(b -> activeBounties.remove(b.getTargetUniqueId()));
            }
        }.runTaskTimer(plugin, 20L * 60, 20L * 60); // Check every minute
    }

    public void saveBounties() {
        // Save Active Bounties
        bountiesConfig.set("active-bounties", null); // Clear old data
        for (Map.Entry<UUID, Bounty> entry : activeBounties.entrySet()) {
            String path = "active-bounties." + entry.getKey().toString();
            Bounty b = entry.getValue();
            bountiesConfig.set(path + ".creator-uuid", b.getCreatorUniqueId().toString());
            bountiesConfig.set(path + ".creator-name", b.getCreatorName());
            bountiesConfig.set(path + ".target-name", b.getTargetName());
            bountiesConfig.set(path + ".reward", b.getReward());
            bountiesConfig.set(path + ".creation-time", b.getCreationTimestamp());
            bountiesConfig.set(path + ".expiration-time", b.getExpirationTimestamp());
        }

        // Save Completed Bounties
        bountiesConfig.set("completed-bounties", null); // Clear old data
        List<Map<String, Object>> completedList = new ArrayList<>();
        for (CompletedBounty cb : completedBounties) {
            Map<String, Object> map = new HashMap<>();
            map.put("creator-name", cb.getCreatorName());
            map.put("target-name", cb.getTargetName());
            map.put("claimer-name", cb.getClaimerName());
            map.put("reward", cb.getReward());
            map.put("completion-time", cb.getCompletionTimestamp());
            completedList.add(map);
        }
        bountiesConfig.set("completed-bounties", completedList);

        try {
            bountiesConfig.save(bountiesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBounties() {
        // Load Active Bounties
        if (bountiesConfig.isConfigurationSection("active-bounties")) {
            ConfigurationSection activeSection = bountiesConfig.getConfigurationSection("active-bounties");
            for (String key : activeSection.getKeys(false)) {
                UUID targetId = UUID.fromString(key);
                String path = "active-bounties." + key;
                Bounty bounty = new Bounty(
                        UUID.fromString(bountiesConfig.getString(path + ".creator-uuid")),
                        bountiesConfig.getString(path + ".creator-name"),
                        targetId,
                        bountiesConfig.getString(path + ".target-name"),
                        bountiesConfig.getDouble(path + ".reward"),
                        bountiesConfig.getLong(path + ".creation-time"),
                        bountiesConfig.getLong(path + ".expiration-time")
                );
                if (!bounty.isExpired()) {
                    activeBounties.put(targetId, bounty);
                }
            }
        }

        // Load Completed Bounties
        if (bountiesConfig.isList("completed-bounties")) {
            List<Map<?, ?>> completedList = bountiesConfig.getMapList("completed-bounties");
            for (Map<?, ?> map : completedList) {
                CompletedBounty cb = new CompletedBounty(
                        (String) map.get("creator-name"),
                        (String) map.get("target-name"),
                        (String) map.get("claimer-name"),
                        (Double) map.get("reward"),
                        (Long) map.get("completion-time")
                );
                completedBounties.add(cb);
            }
        }
    }

    public String formatDuration(long millis) {
        if (millis <= 0) {
            return "Permanent";
        }
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}
