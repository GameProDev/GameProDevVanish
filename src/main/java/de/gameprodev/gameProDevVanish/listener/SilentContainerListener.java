package de.gameprodev.gameProDevVanish.listener;

import de.gameprodev.gameProDevVanish.service.VanishService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SilentContainerListener implements Listener {
    private final VanishService vanishService;
    private final Map<UUID, SilentOpenSession> sessions = new HashMap<>();

    public SilentContainerListener(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!vanishService.isVanished(player)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Container container)) {
            return;
        }

        event.setCancelled(true);
        Inventory source = container.getInventory();
        String title = readableTitle(block.getType().name());
        Inventory mirror = Bukkit.createInventory(player, source.getSize(), title);
        mirror.setContents(cloneItems(source.getContents()));
        sessions.put(player.getUniqueId(), new SilentOpenSession(mirror));
        player.openInventory(mirror);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        SilentOpenSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (event.getView().getTopInventory() == session.mirror()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        SilentOpenSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (event.getView().getTopInventory() == session.mirror()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        SilentOpenSession session = sessions.remove(player.getUniqueId());
        if (session == null || event.getInventory() != session.mirror()) {
            return;
        }
    }

    private String readableTitle(String materialName) {
        return ChatColor.DARK_GRAY + "Lautlos: " + materialName.toLowerCase().replace('_', ' ');
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        ItemStack[] cloned = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            cloned[i] = items[i] == null ? null : items[i].clone();
        }
        return cloned;
    }

    private record SilentOpenSession(Inventory mirror) {
    }
}
