package com.anthonyb.escort.event;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.escort.gui.KitGUI;
import com.anthonyb.escort.kits.Kit;
import com.anthonyb.foobarlib.minigame.MinigamePhase;

public class PlayerDropPickupItem implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerAttemptPickupItemEvent e) {
        // Allow destroying and picking up blocks from the build phase.
        if (!EscortPlugin.GAME.getPhase().equals(MinigamePhase.PREGAME)) {
            e.getItem().remove();
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        String kitName = KitGUI.kitSelection.get(playerId);
        if (kitName == null) {
            e.setCancelled(true);
            return;
        }
        Kit kit = Kit.kits.get(kitName);
        if (kit == null) {
            e.setCancelled(true);
            return;
        }
        if (!kit.canPickupArrows()) {
            e.setCancelled(true);
        }
    }
}
