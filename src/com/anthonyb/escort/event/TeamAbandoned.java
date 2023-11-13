package com.anthonyb.escort.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.foobarplugin.message.Message;
import com.anthonyb.foobarplugin.minigame.TeamGame;
import com.anthonyb.foobarplugin.minigame.customevents.TeamAbandonedEvent;

public class TeamAbandoned implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeamAbandoned(TeamAbandonedEvent e) {
        if (!EscortPlugin.GAME.getPhase().isGameplayPhase()) {
            return;
        }
        int teamIdx = e.getAbandonedTeamIdx();
        Message.m(
                TeamGame.getTeamChatColor(teamIdx) + TeamGame.getTeamName(teamIdx) + " Team &rhas resigned the match!")
                .broadcast();
        EscortPlugin.GAME.ROBOT.forceWin(teamIdx == 0 ? 1 : 0); // Forcibly make the opposite team win.
    }
}
