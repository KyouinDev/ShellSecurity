package io.kyouin.shellsec.utils;

import io.kyouin.shellsec.ShellSecurity;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Messages {

    private final ShellSecurity shellSec;
    private FileConfiguration defaultConfig;
    private FileConfiguration config;

    public Messages(ShellSecurity shellSec) {
        this.shellSec = shellSec;

        try (InputStream is = shellSec.getResource("messages.yml")) {
            if (is == null) throw new IllegalArgumentException("Couldn't find messages.yml");

            shellSec.saveResource("messages.yml", false);

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                defaultConfig = YamlConfiguration.loadConfiguration(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(new File(shellSec.getDataFolder(), "messages.yml"));
    }

    public void sendMessage(CommandSender to, Player who, String message, boolean canTitle) {
        message = getMessageOrDefault(message).replaceAll("&", "§");

        if (who != null) message = message.replace("{name}", to.getName());

        if (canTitle && shellSec.getConfig().getBoolean("messages-as-titles", false)) {
            ((Player) to).sendTitle(Constants.PREFIX, message, 10, 60, 10);
        } else {
            to.sendMessage(Constants.PREFIX + " " + message);
        }
    }

    public String getMessageOrDefault(String key) {
        return config.getString(key, defaultConfig.getString(key));
    }
}
