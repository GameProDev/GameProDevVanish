package de.gameprodev.gameProDevVanish.listener;

import de.gameprodev.gameProDevVanish.service.VanishService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final VanishService vanishService;

    public PlayerConnectionListener(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        vanishService.applyVisibilityForJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        vanishService.clearVanish(event.getPlayer());
    }
}
