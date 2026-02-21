package de.gameprodev.gameProDevVanish;

import de.gameprodev.gameProDevVanish.command.VanishCommand;
import de.gameprodev.gameProDevVanish.listener.PlayerConnectionListener;
import de.gameprodev.gameProDevVanish.listener.SilentContainerListener;
import de.gameprodev.gameProDevVanish.listener.VanishProtectionListener;
import de.gameprodev.gameProDevVanish.service.VanishService;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameProDevVanish extends JavaPlugin {
    private VanishService vanishService;

    @Override
    public void onEnable() {
        this.vanishService = new VanishService(this);

        VanishCommand vanishCommand = new VanishCommand(vanishService);
        Objects.requireNonNull(getCommand("vanish"), "Command vanish missing in plugin.yml")
                .setExecutor(vanishCommand);
        Objects.requireNonNull(getCommand("vanish"), "Command vanish missing in plugin.yml")
                .setTabCompleter(vanishCommand);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(vanishService), this);
        getServer().getPluginManager().registerEvents(new VanishProtectionListener(vanishService), this);
        getServer().getPluginManager().registerEvents(new SilentContainerListener(vanishService), this);
    }

    @Override
    public void onDisable() {
        if (vanishService != null) {
            vanishService.clearAllVanishStates();
        }
    }
}
