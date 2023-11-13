package com.anthonyb.escort.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.anthonyb.escort.gui.KitGUI;

public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run that command!");
			return true;
		}
		Player player = (Player) sender;
		if (label.equalsIgnoreCase("kit")) {
			KitGUI.openKitsMenu(player);
			return true;
		}
		return false;
	}
}
