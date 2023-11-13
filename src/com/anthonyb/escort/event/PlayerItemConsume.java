package com.anthonyb.escort.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerItemConsume implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item.getType().equals(Material.POTION)) {
            Player player = e.getPlayer();
            player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 1));
        }
    }
}
