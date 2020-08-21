package io.kyouin.shellsec.listeners;

import io.kyouin.shellsec.ShellSecurity;
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
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Objects;

public class ListenerPlayer implements Listener {

    private final ShellSecurity shellSec;

    public ListenerPlayer(ShellSecurity shellSec) {
        this.shellSec = shellSec;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();

        if (block == null || !block.getType().name().contains("SHULKER_BOX")) {
            return;
        }

        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();
        String shulkerOwner = shellSec.getShulkerOwner(block);

        if (shulkerOwner != null && !shulkerOwner.equals(uuid)) {
            if (shellSec.getConfig().getBoolean("admin-bypass", true) && p.hasPermission("shellsec.bypass")) {
                shellSec.getMessages().sendMessage(p,null, "bypass-open-locked", false);
            } else {
                e.setCancelled(true);
                shellSec.getMessages().sendMessage(p, null, "cant-interact-locked", true);

                if (shellSec.getConfig().getBoolean("admin-alerts-interact", true)) {
                    shellSec.sendAlert(p, "interact-locked-attempt", "shellsec.alerts.interact");
                }
            }

            return;
        }

        ItemStack item = e.getItem();
        Material material = item == null ? Material.AIR : item.getType();

        if (material == Material.NAME_TAG && shulkerOwner == null) {
            e.setCancelled(true);

            if (p.hasPermission("shellsec.lock")) {
                if (shellSec.getConfig().getBoolean("disallows-lock-non-empty", false) && !Arrays.stream(((ShulkerBox) block.getState()).getInventory().getContents()).allMatch(content -> content == null || content.getType() == Material.AIR)) {
                    shellSec.getMessages().sendMessage(p, null, "cant-lock-non-empty", true);
                } else {
                    shellSec.applyNameTag(uuid, (ShulkerBox) block.getState(), item);
                    shellSec.getMessages().sendMessage(p, null, "shulker-locked", true);
                }
            } else {
                shellSec.getMessages().sendMessage(p, null, "cant-lock", true);
            }
        } else if (material == Material.MILK_BUCKET && shulkerOwner != null) {
            e.setCancelled(true);

            shellSec.applyMilkBucket((ShulkerBox) block.getState(), item);
            shellSec.getMessages().sendMessage(p, null,  "shulker-unlocked", true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!e.getBlock().getType().name().contains("SHULKER_BOX")) {
            return;
        }

        ItemMeta itemMeta = e.getItemInHand().getItemMeta();
        NamespacedKey shulkerOwnerKey = shellSec.getShulkerOwnerKey();

        if (Objects.requireNonNull(itemMeta).getPersistentDataContainer().get(shulkerOwnerKey, PersistentDataType.STRING) == null) {
            return;
        }

        ShulkerBox shulker = (ShulkerBox) e.getBlock().getState();
        shulker.getPersistentDataContainer().set(shulkerOwnerKey, PersistentDataType.STRING, e.getPlayer().getUniqueId().toString());
        shulker.update();
    }

    @EventHandler
    public void onDestroy(BlockBreakEvent e) {
        if (!e.getBlock().getType().name().contains("SHULKER_BOX")) {
            return;
        }

        Block block = e.getBlock();
        String shulkerOwner = shellSec.getShulkerOwner(block);

        if (shulkerOwner == null) {
            return;
        }

        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();

        if (!shulkerOwner.equals(uuid)) {
            e.setCancelled(true);
            shellSec.getMessages().sendMessage(p, null,"cant-break-locked", true);

            if (shellSec.getConfig().getBoolean("admin-alerts-break", true)) {
                shellSec.sendAlert(p, "break-locked-attempt", "shellsec.alerts.break");
            }

            return;
        }

        block.getDrops().stream().filter(drop -> drop.getType().name().contains("SHULKER_BOX")).findAny().ifPresent(drop -> {
            ItemMeta itemMeta = drop.getItemMeta();
            Objects.requireNonNull(itemMeta).getPersistentDataContainer().set(shellSec.getShulkerOwnerKey(), PersistentDataType.STRING, shulkerOwner);
            drop.setItemMeta(itemMeta);

            block.getWorld().dropItemNaturally(block.getLocation(), drop);
            e.setDropItems(false);
        });
    }
}
