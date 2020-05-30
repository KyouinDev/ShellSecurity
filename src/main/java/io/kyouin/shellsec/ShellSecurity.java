package io.kyouin.shellsec;

import io.kyouin.shellsec.listeners.ListenerExplosion;
import io.kyouin.shellsec.listeners.ListenerPlayer;
import io.kyouin.shellsec.utils.Constants;
import org.bukkit.plugin.java.JavaPlugin;

public class ShellSecurity extends JavaPlugin {

    private final Constants constants;

    public ShellSecurity() {
        constants = new Constants(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        getServer().getPluginManager().registerEvents(new ListenerExplosion(this), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayer(this), this);
    }

    public Constants getConstants() {
        return constants;
    }
}
