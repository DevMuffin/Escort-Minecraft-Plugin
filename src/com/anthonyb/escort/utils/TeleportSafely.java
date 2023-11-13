package com.anthonyb.escort.utils;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TeleportSafely {
    public static boolean tp(Player player, @Nullable Location location) {
        player.setVelocity(new Vector(0, 0, 0)); // prevent fall damage and the like
        player.setFallDistance(0f);
        if (location == null) {
            return false;
        }
        return player.teleport(location);
    }
}
