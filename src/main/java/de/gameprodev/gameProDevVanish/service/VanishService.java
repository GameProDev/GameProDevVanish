package de.gameprodev.gameProDevVanish.service;

import de.gameprodev.gameProDevVanish.GameProDevVanish;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VanishService {
    public static final String PERMISSION_VANISH_SELF = "gameprodevvanish.vanish";
    public static final String PERMISSION_VANISH_SEE = "gameprodevvanish.see";

    private final GameProDevVanish plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, PlayerStateSnapshot> snapshots = new HashMap<>();

    public VanishService(GameProDevVanish plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public Set<Player> getOnlineVanishedPlayers() {
        return vanishedPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toSet());
    }

    public boolean toggleVanish(Player target) {
        boolean newState = !isVanished(target);
        setVanish(target, newState);
        return newState;
    }

    public void setVanish(Player target, boolean vanish) {
        if (vanish) {
            vanishedPlayers.add(target.getUniqueId());
            snapshots.put(target.getUniqueId(), PlayerStateSnapshot.capture(target));
            applyStealthState(target);
            hideTargetFromNonPrivilegedViewers(target);
            target.sendMessage(ChatColor.GREEN + "Du bist jetzt im Vanish.");
            return;
        }

        vanishedPlayers.remove(target.getUniqueId());
        restoreState(target);
        showTargetToAllViewers(target);
        target.sendMessage(ChatColor.RED + "Du bist nicht mehr im Vanish.");
    }

    public void clearVanish(Player target) {
        if (!isVanished(target)) {
            return;
        }
        vanishedPlayers.remove(target.getUniqueId());
        restoreState(target);
        showTargetToAllViewers(target);
    }

    public void clearAllVanishStates() {
        for (Player vanishedPlayer : getOnlineVanishedPlayers()) {
            restoreState(vanishedPlayer);
            showTargetToAllViewers(vanishedPlayer);
        }
        vanishedPlayers.clear();
        snapshots.clear();
    }

    public void applyVisibilityForJoin(Player joiningPlayer) {
        for (Player vanishedPlayer : getOnlineVanishedPlayers()) {
            if (joiningPlayer.equals(vanishedPlayer)) {
                continue;
            }
            if (joiningPlayer.hasPermission(PERMISSION_VANISH_SEE)) {
                joiningPlayer.showPlayer(plugin, vanishedPlayer);
            } else {
                joiningPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }

        if (isVanished(joiningPlayer)) {
            hideTargetFromNonPrivilegedViewers(joiningPlayer);
        }
    }

    private void hideTargetFromNonPrivilegedViewers(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            if (viewer.hasPermission(PERMISSION_VANISH_SEE)) {
                viewer.showPlayer(plugin, target);
            } else {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    private void showTargetToAllViewers(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            viewer.showPlayer(plugin, target);
        }
    }

    private void applyStealthState(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCanPickupItems(false);
        player.setCollidable(false);
        player.setInvulnerable(true);
    }

    private void restoreState(Player player) {
        PlayerStateSnapshot snapshot = snapshots.remove(player.getUniqueId());
        if (snapshot == null) {
            player.setCanPickupItems(true);
            player.setCollidable(true);
            player.setInvulnerable(false);
            return;
        }

        snapshot.apply(player);
    }

    private record PlayerStateSnapshot(
            boolean allowFlight,
            boolean flying,
            boolean canPickupItems,
            boolean collidable,
            boolean invulnerable) {
        private static PlayerStateSnapshot capture(Player player) {
            return new PlayerStateSnapshot(
                    player.getAllowFlight(),
                    player.isFlying(),
                    player.getCanPickupItems(),
                    player.isCollidable(),
                    player.isInvulnerable());
        }

        private void apply(Player player) {
            player.setAllowFlight(allowFlight);
            player.setFlying(flying);
            player.setCanPickupItems(canPickupItems);
            player.setCollidable(collidable);
            player.setInvulnerable(invulnerable);
        }
    }
}
