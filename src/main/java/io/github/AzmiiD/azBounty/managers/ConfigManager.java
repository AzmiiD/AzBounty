package io.github.AzmiiD.azBounty.managers;

import io.github.AzmiiD.azBounty.AzBounty;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final AzBounty plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ConfigManager() {
        this.plugin = AzBounty.getInstance();
        loadConfigs();
    }

    public void loadConfigs() {
        // Load config.yml
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        // Reload config.yml
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Reload messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defMessagesStream = plugin.getResource("messages.yml");
        if (defMessagesStream != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defMessagesStream)));
        }
    }

    public String getMessage(String path) {
        String message = messages.getString(path, "&cMessage not found: " + path);
        message = message.replace("{prefix}", messages.getString("prefix"));
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public List<String> getMessageList(String path) {
        return messages.getStringList(path).stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    public FileConfiguration getConfig() {
        return config;
    }
}