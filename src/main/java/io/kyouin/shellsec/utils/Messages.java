package io.kyouin.shellsec.utils;

import io.kyouin.shellsec.ShellSecurity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messages {

    private final ShellSecurity shellSec;

    public Messages(ShellSecurity shellSec) {
        this.shellSec = shellSec;
    }

    public void sendMessage(CommandSender to, String message) {
        to.sendMessage(Constants.PREFIX + " " + message);
    }

    public void sendTitle(Player p, String message) {
        p.sendTitle(Constants.PREFIX, message, 10, 60, 10);
    }

    public void send(Player p, String message) {
        if (shellSec.getConfig().getBoolean("messages-as-titles", false)) {
            sendTitle(p, message);
        } else {
            sendMessage(p, message);
        }
    }
}
