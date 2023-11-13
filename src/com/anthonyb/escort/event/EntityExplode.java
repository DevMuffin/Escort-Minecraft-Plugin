package com.anthonyb.escort.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.foobarplugin.threads.Threads;

public class EntityExplode implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        Bukkit.broadcastMessage("YEP");
        if (e.getEntityType().equals(EntityType.PRIMED_TNT)) {
            TNTPrimed tnt = (TNTPrimed) e.getEntity();
            e.setCancelled(true);
            // e.getLocation().create
            e.getLocation().createExplosion(tnt.getSource(), 0.0015f, false, false);
            // Bukkit.broadcastMessage("YEP2");
            // Threads.runMainLater(() -> {

            // }, 3 * 20, EscortPlugin.INSTANCE);
            // // e.blockList().clear(); // Prevent block damage.
        }
    }
}
