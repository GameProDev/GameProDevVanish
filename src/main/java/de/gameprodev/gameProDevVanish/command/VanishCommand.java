package de.gameprodev.gameProDevVanish.command;

import de.gameprodev.gameProDevVanish.service.VanishService;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor, TabCompleter {
    private final VanishService vanishService;

    public VanishCommand(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Command ist nur fuer Spieler.");
            return true;
        }

        if (!player.isOp() || !player.hasPermission(VanishService.PERMISSION_VANISH_SELF)) {
            player.sendMessage(ChatColor.RED + "Nur OPs duerfen /vanish nutzen.");
            return true;
        }

        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Nutzung: /" + label);
            return true;
        }

        boolean vanished = vanishService.toggleVanish(player);
        player.sendMessage(vanished
                ? ChatColor.GREEN + "Vanish aktiviert."
                : ChatColor.RED + "Vanish deaktiviert.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
