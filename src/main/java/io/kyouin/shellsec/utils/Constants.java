package io.kyouin.shellsec.utils;

import io.kyouin.shellsec.ShellSecurity;
import org.bukkit.NamespacedKey;

public class Constants {

    public final static String PREFIX = "§dShellSecurity";

    private final NamespacedKey shulkerOwnerKey;

    public Constants(ShellSecurity shellSec) {
        this.shulkerOwnerKey = new NamespacedKey(shellSec, "shulker_owner");
    }

    public NamespacedKey getShulkerOwnerKey() {
        return shulkerOwnerKey;
    }
}
