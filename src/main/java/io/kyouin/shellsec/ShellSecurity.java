package io.kyouin.shellsec;

import io.kyouin.shellsec.listeners.ListenerExplosion;
import io.kyouin.shellsec.listeners.ListenerPlayer;
import io.kyouin.shellsec.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ShellSecurity extends JavaPlugin {

    private final NamespacedKey shulkerOwnerKey;

    private final Messages messages;

    public ShellSecurity() {
        shulkerOwnerKey = new NamespacedKey(this, ShellConstants.OWNER_KEY);
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

    public String getShulkerOwner(BlockState blockState) {
        if (!(blockState instanceof ShulkerBox)) {
            return null;
        }

        return ((ShulkerBox) blockState).getPersistentDataContainer().get(shulkerOwnerKey, PersistentDataType.STRING);
    }

    public void applyNameTag(String who, ShulkerBox shulker, ItemStack nameTag) {
        shulker.getPersistentDataContainer().set(shulkerOwnerKey, PersistentDataType.STRING, who);
        shulker.update();

        nameTag.setAmount(nameTag.getAmount() - 1);
    }

    public void applyMilkBucket(ShulkerBox shulker, ItemStack bucket) {
        shulker.getPersistentDataContainer().remove(shulkerOwnerKey);
        shulker.update();

        bucket.setType(Material.BUCKET);
    }

    public void sendAlert(Player who, String configKey, String message, String permission) {
        if (getConfig().getBoolean(configKey, true)) {
            Bukkit.getServer().getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(permission))
                    .forEach(op -> messages.sendMessage(op, who, message, false));

            messages.sendMessage(Bukkit.getConsoleSender(), who, message, false);
        }
    }

    public NamespacedKey getShulkerOwnerKey() {
        return shulkerOwnerKey;
    }

    public Messages getMessages() {
        return messages;
    }
}
