package com.anthonyb.escort.event;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.foobarlib.aws.dynamo.category.PushData;

public class PlayerJoin implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        player.setBedSpawnLocation(null);
        player.setGameMode(GameMode.ADVENTURE);
        EscortPlugin.setPushData(playerId, PushData.get(playerId.toString()));
    }
}
