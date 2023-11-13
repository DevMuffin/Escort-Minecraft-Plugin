package com.anthonyb.escort.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.anthonyb.escort.EscortPlugin;

import net.kyori.adventure.text.Component;

public class AdminCommands implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (label.equalsIgnoreCase("endgame")) {
			sender.sendMessage(Component.text("ENDGAME"));
			Bukkit.getServer().shutdown(); // TODO - this didn't seem to work - did we get stuck on saving or did this
											// not actually kill the docker container?
			// We may need to send some sort of docker stop here instead.
			return true;
		}
		// Anthony TODO - put op guard back in.
		// if (!sender.isOp()) {
		// if (sender instanceof Player) {
		// Message.e("You cannot do that!").send((Player) sender);
		// }
		// return true;
		// }
		if (label.equalsIgnoreCase("admin")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("startGame")) {
					EscortPlugin.GAME.startGame();
				} else if (args[0].equalsIgnoreCase("forcewin")) {
					EscortPlugin.GAME.ROBOT.forceWin(0);
				} else if (args[0].equalsIgnoreCase("test")) {
					if (args.length > 1) {
						Location loc = ((Player) sender).getLocation();
						loc.add(25, 0, 0);
						loc.getWorld().spawnParticle(Particle.valueOf(args[1].toUpperCase()), loc, 50, 0, 0, 0);
					}
				} else if (args[0].equalsIgnoreCase("test2")) {
					if (args.length > 1) {
						Location loc = ((Player) sender).getLocation();
						loc.add(25, 0, 0);
						loc.getWorld().spawnParticle(Particle.valueOf(args[1].toUpperCase()), loc, 50, 3, 3, 3);
					}
				}
			}
			return true;
		}
		// if (!(sender instanceof Player)) {
		//
		// }
		return false;
	}
}
