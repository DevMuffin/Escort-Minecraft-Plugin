package com.anthonyb.escort.event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.foobarlib.minigame.MinigamePhase;
import com.anthonyb.foobarplugin.minigame.TeamGame;

public class BlockPlaceBreak implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType().equals(Material.TNT)) {
            block.setType(Material.AIR);
            Player player = e.getPlayer();
            player.setCooldown(Material.TNT, 10 * 20);
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            TNTPrimed primedTnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
            primedTnt.setGlowing(true);
            primedTnt.setSource(player);
            // block.getLocation().add
            // TNT tnt = (TNT) block;
            // tnt.setUnstable(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        boolean blue = false;
        if ((blue = block.getType() == Material.BLUE_CONCRETE) || block.getType() == Material.RED_CONCRETE) {
            if (EscortPlugin.GAME.getPhase().equals(MinigamePhase.PREGAME)) {
                final boolean blueF = blue;
                TeamGame.getTeamIdx(player).ifPresent(teamIdx -> {
                    if (blueF && teamIdx != 0) {
                        e.setCancelled(true);
                    } else if (!blueF && teamIdx != 1) {
                        e.setCancelled(true);
                    }
                });
            } else {
                // Allow breaking of the block
                e.setCancelled(false); // Don't need but it makes the logic explicitly clear here.
                e.setDropItems(false);
            }
        } else {
            e.setCancelled(true);
        }
    }
}
