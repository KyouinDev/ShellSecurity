package io.kyouin.shellsec;

import io.kyouin.shellsec.listeners.ListenerExplosion;
import io.kyouin.shellsec.listeners.ListenerPlayer;
import io.kyouin.shellsec.utils.Constants;
import io.kyouin.shellsec.utils.Messages;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ShellSecurity extends JavaPlugin {

    private final NamespacedKey shulkerOwnerKey;

    private final Messages messages;

    public ShellSecurity() {
        shulkerOwnerKey = new NamespacedKey(this, Constants.OWNER_KEY);
        messages = new Messages(this);
    }

    @Override
    public void onEnable() {
        messages.reloadConfig();

        saveDefaultConfig();
        reloadConfig();

        getServer().getPluginManager().registerEvents(new ListenerExplosion(this), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayer(this), this);
    }

    public String getShulkerOwner(Block block) {
        if (!(block.getState() instanceof ShulkerBox)) {
            return null;
        }

        return ((ShulkerBox) block.getState()).getPersistentDataContainer().get(shulkerOwnerKey, PersistentDataType.STRING);
    }

    public NamespacedKey getShulkerOwnerKey() {
        return shulkerOwnerKey;
    }

    public Messages getMessages() {
        return messages;
    }
}
