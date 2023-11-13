package com.anthonyb.escort.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.anthonyb.escort.gui.KitGUI;
import com.anthonyb.escort.kits.Kit;
import com.anthonyb.foobarplugin.minigame.TeamGame;

public class PlayerRespawn implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player respawnPlayer = e.getPlayer();
		String kitSelectedName = KitGUI.kitSelection.getOrDefault(respawnPlayer.getUniqueId(), Kit.WARRIOR.getName());
		TeamGame.getTeamIdx(respawnPlayer).ifPresent(teamIdx -> {
			Kit.kits.getOrDefault(kitSelectedName, Kit.WARRIOR).apply(respawnPlayer, teamIdx == 0 ? true : false);
		});
	}
}
