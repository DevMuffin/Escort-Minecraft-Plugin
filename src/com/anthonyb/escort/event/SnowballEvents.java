package com.anthonyb.escort.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.anthonyb.foobarplugin.message.Message;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;

public class SnowballEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerThrowSnowball(PlayerLaunchProjectileEvent e) {
        if (e.getProjectile().getType().equals(EntityType.SNOWBALL)) {
            e.getPlayer().setCooldown(Material.SNOWBALL, 3 * 20);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerHitBySnowball(ProjectileCollideEvent e) {
        Entity collidedWith = e.getCollidedWith();
        if (collidedWith instanceof Player) {
            Player player = (Player) collidedWith;
            if (e.getEntityType().equals(EntityType.SNOWBALL)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2));
                Message.m("You've been &aSLOWED").sendActionbar(player);
            }
        }
    }
}
