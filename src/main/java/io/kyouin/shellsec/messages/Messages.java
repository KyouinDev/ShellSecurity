package io.kyouin.shellsec.messages;

import io.kyouin.shellsec.ShellConstants;
import io.kyouin.shellsec.ShellSecurity;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Messages {

    private final ShellSecurity shellSec;

    private FileConfiguration defaultConfig;
    private FileConfiguration config;

    public Messages(ShellSecurity shellSec) {
        this.shellSec = shellSec;

        shellSec.saveResource("messages.yml", false);

        try (InputStream is = shellSec.getResource("messages.yml"); Reader reader = new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8)) {
            defaultConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(new File(shellSec.getDataFolder(), "messages.yml"));
    }

    public void sendMessage(CommandSender to, Player who, String message, boolean canTitle) {
        String updatedMessage = getMessageOrDefault(message).replaceAll("&", "§");

        if (who != null) {
            updatedMessage = updatedMessage.replace("{name}", to.getName());
        }

        if (canTitle && shellSec.getConfig().getBoolean("messages-as-titles", false)) {
            ((Player) to).sendTitle(ShellConstants.PREFIX, updatedMessage, 10, 60, 10);
        } else {
            to.sendMessage(ShellConstants.PREFIX + " " + updatedMessage);
        }
    }

    public String getMessageOrDefault(String key) {
        return config.getString(key, defaultConfig.getString(key));
    }
}
