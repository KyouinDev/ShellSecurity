package io.kyouin.shellsec.listeners;

import io.kyouin.shellsec.ShellSecurity;
import io.kyouin.shellsec.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ListenerPlayer implements Listener {

    private final ShellSecurity shellSec;

    public ListenerPlayer(ShellSecurity shellSec) {
        this.shellSec = shellSec;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();

        if (block == null || !block.getType().name().contains("SHULKER_BOX")) return;

        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();
        ShulkerBox shulker = (ShulkerBox) block.getState();
        PersistentDataContainer dataContainer = shulker.getPersistentDataContainer();
        NamespacedKey shulkerOwnerKey = shellSec.getConstants().getShulkerOwnerKey();
        String shulkerOwner = dataContainer.get(shulkerOwnerKey, PersistentDataType.STRING);

        if (shulkerOwner != null && !shulkerOwner.equals(uuid)) {
            if (shellSec.getConfig().getBoolean("admin-bypass", true) && p.hasPermission("shellsec.bypass")) {
                p.sendMessage(Constants.PREFIX + "You opened a locked shulker box.");
            } else {
                e.setCancelled(true);
                p.sendMessage(Constants.PREFIX_ERROR + "You can't interact with a locked shulker box.");

                if (shellSec.getConfig().getBoolean("admin-alerts-interact", true)) {
                    String error = Constants.PREFIX + "§8" + p.getName() + " §7attempted to interact with a locked shulker box.";

                    Bukkit.getConsoleSender().sendMessage(error);
                    Bukkit.getServer().getOnlinePlayers().stream()
                            .filter(player -> player.hasPermission("shellsec.alerts.interact"))
                            .forEach(op -> op.sendMessage(error));
                }
            }

            return;
        }

        ItemStack item = e.getItem();
        Material material = item == null ? Material.AIR : item.getType();

        if (material == Material.NAME_TAG && shulkerOwner == null) {
            e.setCancelled(true);

            if (p.hasPermission("shellsec.lock")) {
                if (shellSec.getConfig().getBoolean("disallows-lock-non-empty", false) && !Arrays.stream(shulker.getInventory().getContents()).allMatch(content -> content == null || content.getType() == Material.AIR)) {
                    p.sendMessage(Constants.PREFIX_ERROR + "You can't lock a non-empty shulker box.");
                } else {
                    dataContainer.set(shulkerOwnerKey, PersistentDataType.STRING, uuid);
                    shulker.update();

                    item.setAmount(item.getAmount() - 1);
                    p.sendMessage(Constants.PREFIX + "You locked this shulker box.");
                }
            } else {
                p.sendMessage(Constants.PREFIX_ERROR + "You can't lock a shulker box.");
            }
        } else if (material == Material.MILK_BUCKET && shulkerOwner != null) {
            e.setCancelled(true);

            dataContainer.remove(shulkerOwnerKey);
            shulker.update();

            item.setType(Material.BUCKET);
            p.sendMessage(Constants.PREFIX + "You unlocked this shulker box.");
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!e.getBlock().getType().name().contains("SHULKER_BOX")) return;

        ItemMeta itemMeta = e.getItemInHand().getItemMeta();
        NamespacedKey shulkerOwnerKey = shellSec.getConstants().getShulkerOwnerKey();

        if (itemMeta == null || itemMeta.getPersistentDataContainer().get(shulkerOwnerKey, PersistentDataType.STRING) == null) return;

        ShulkerBox shulker = (ShulkerBox) e.getBlock().getState();
        shulker.getPersistentDataContainer().set(shulkerOwnerKey, PersistentDataType.STRING, e.getPlayer().getUniqueId().toString());
        shulker.update();
    }

    @EventHandler
    public void onDestroy(BlockBreakEvent e) {
        if (!e.getBlock().getType().name().contains("SHULKER_BOX")) return;

        Block block = e.getBlock();
        NamespacedKey shulkerOwnerKey = shellSec.getConstants().getShulkerOwnerKey();
        String shulkerOwner = ((ShulkerBox) block.getState()).getPersistentDataContainer().get(shulkerOwnerKey, PersistentDataType.STRING);

        if (shulkerOwner == null) return;

        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();

        if (!shulkerOwner.equals(uuid)) {
            e.setCancelled(true);
            p.sendMessage(Constants.PREFIX_ERROR + "You can't break a locked shulker box.");

            if (shellSec.getConfig().getBoolean("admin-alerts-break", true)) {
                String error = Constants.PREFIX + "§8" + p.getName() + " §7attempted to break a locked shulker box.";

                Bukkit.getConsoleSender().sendMessage(error);
                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission("shellsec.alerts.break"))
                        .forEach(op -> op.sendMessage(error));
            }

            return;
        }

        block.getDrops().stream().filter(drop -> drop.getType().name().contains("SHULKER_BOX")).findAny().ifPresent(drop -> {
            ItemMeta itemMeta = drop.getItemMeta();

            if (itemMeta == null) return;

            itemMeta.getPersistentDataContainer().set(shulkerOwnerKey, PersistentDataType.STRING, shulkerOwner);
            drop.setItemMeta(itemMeta);

            block.getWorld().dropItemNaturally(block.getLocation(), drop);
            e.setDropItems(false);
        });
    }
}
