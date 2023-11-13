package com.anthonyb.escort.robot;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.foobarlib.minigame.MinigamePhase;
import com.anthonyb.foobarplugin.item.ItemBuilder;
import com.anthonyb.foobarplugin.message.Message;
import com.anthonyb.foobarplugin.message.Message.SoundInfo;
import com.anthonyb.foobarplugin.minigame.TeamGame;
import com.anthonyb.foobarplugin.particle.ParticleShape;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Robot {

	private boolean teamAContesting, teamBContesting;

	private ArmorStand as;

	private Vector robotDirection;

	private int pushDelta, farthestA, farthestB;

	private Location spawnLocation;

	public Robot(Location location, Vector robotDirection) {
		assert robotDirection.isNormalized();
		this.teamAContesting = true;
		this.teamBContesting = true;
		this.spawnLocation = location.add(0.5, 0, 0.5);
		this.robotDirection = robotDirection;
		this.as = (ArmorStand) location.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
		this.as.setGravity(false);
		this.as.setCollidable(false);
		this.as.setInvulnerable(true);
		this.as.setBasePlate(false);
		this.as.setArms(true);
		this.as.setGlowing(true);
		EntityEquipment equipment = this.as.getEquipment();
		equipment.setHelmet(new ItemBuilder().material(Material.DRAGON_HEAD).is());
		this.pushDelta = 0;
		this.farthestA = 0;
		this.farthestB = 0;
		// Update the winning team right away so it's set to DRAW.
		EscortPlugin.GAME.getScoreboard().getTeam("winningTeam")
				// Right facing arrow.
				.prefix(Component.text("\u25B6 ").color(NamedTextColor.YELLOW)
						.append(Component.text(this.getWinningTeamStr())));
	}

	private void displayParticles(int dirSign) {
		int winningTeam = this.getWinningTeam();
		Location particleLoc = this.spawnLocation.clone()
				.add(this.robotDirection.clone().multiply(dirSign * Math.max(this.farthestA, this.farthestB)))
				.add(0, 10, 0);
		Particle particleType = Particle.SOUL_FIRE_FLAME;
		if (winningTeam == 1) {
			particleType = dirSign > 0 ? Particle.GLOW_SQUID_INK : Particle.FLAME;
		} else if (winningTeam == 2) {
			particleType = dirSign < 0 ? Particle.GLOW_SQUID_INK : Particle.FLAME;
		}
		ParticleShape.drawCube(particleLoc, particleType, 1, 0.5, 15, false,
				horizRad -> 0.75D * horizRad + 0.184051D, verticalRad -> 0.0735072D * verticalRad + 0.184051D,
				Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
	}

	private void _move(int dirSign) {
		// Location farthestPushLoc = this.spawnLocation.clone().add(
		// this.robotDirection.clone().multiply(winningTeam == 1 ? 1 * this.farthestA :
		// -1 * this.farthestB))
		// .add(0, 10, 0);
		// ParticleShape.drawCube(farthestPushLoc, Particle.GLOW_SQUID_INK, 1, 0.5, 15,
		// false,
		// horizRad -> 0.75D * horizRad + 0.184051D, verticalRad -> 0.0735072D *
		// verticalRad + 0.184051D,
		// Bukkit.getOnlinePlayers().toArray(new
		// Player[Bukkit.getOnlinePlayers().size()]));
		// // Note that we flip the sign for the teams since we want to display the
		// // farthest push on the OPPOSITE side of the map.
		// Location farthestPushOnOppositeSideLoc = this.spawnLocation.clone().add(
		// this.robotDirection.clone().multiply(winningTeam == 1 ? -1 * this.farthestA :
		// 1 * this.farthestB))
		// .add(0, 10, 0);
		// ParticleShape.drawCube(farthestPushOnOppositeSideLoc, Particle.FLAME, 1, 0.5,
		// 15, false,
		// Bukkit.getOnlinePlayers().toArray(new
		// Player[Bukkit.getOnlinePlayers().size()]));

		int winningTeamBeforeMove = this.getWinningTeam();
		Location loc = this.as.getLocation();
		loc.add(this.robotDirection.clone().multiply(dirSign));
		this.as.teleport(loc);
		this.pushDelta += dirSign;
		if (this.pushDelta > this.farthestA) {
			this.farthestA = this.pushDelta;
			if (this.farthestA > this.farthestB) {
				if (winningTeamBeforeMove != 1) {
					Message.m(TeamGame.getTeamChatColor(0) + TeamGame.getTeamName(0) + " Team &rhas taken the lead!")
							.withSound(SoundInfo.create(Sound.ENTITY_ENDER_DRAGON_GROWL)).broadcastUsingActionbar();
				}
				if (EscortPlugin.GAME.getPhase().equals(MinigamePhase.OVERTIME)) {
					EscortPlugin.GAME._endGame();
				}
			}
		} else if (-this.pushDelta > this.farthestB) {
			this.farthestB = -this.pushDelta;
			if (this.farthestB > this.farthestA) {
				if (winningTeamBeforeMove != 2) {
					Message.m(TeamGame.getTeamChatColor(1) + TeamGame.getTeamName(1) + " Team &rhas taken the lead!")
							.withSound(SoundInfo.create(Sound.ENTITY_ENDER_DRAGON_GROWL)).broadcastUsingActionbar();
				}
				if (EscortPlugin.GAME.getPhase().equals(MinigamePhase.OVERTIME)) {
					EscortPlugin.GAME._endGame();
				}
			}
		}
		this.displayParticles(dirSign);
		// Update the winning team each _move call.
		EscortPlugin.GAME.getScoreboard().getTeam("winningTeam")
				// Right facing arrow.
				.prefix(Component.text("\u25B6 ").color(NamedTextColor.YELLOW)
						.append(Component.text(this.getWinningTeamStr())));
		Block trailBlock = loc.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
		if (trailBlock.getType().equals(Material.WHITE_CONCRETE)) {
			trailBlock.setType(dirSign > 0 ? Material.BLUE_WOOL : Material.RED_WOOL);
		} else if (trailBlock.getType().equals(Material.BEDROCK)) {
			// Reached the end, end the game.
			EscortPlugin.GAME.endGame();
		}
	}

	public void move() {
		this.teamAContesting = false;
		this.teamBContesting = false;
		List<Entity> nearbyEntities = this.as.getNearbyEntities(5, 10, 5);
		for (Entity nearbyEntity : nearbyEntities) {
			if (!(nearbyEntity instanceof Player)) {
				continue;
			}
			Player nearbyPlayer = (Player) nearbyEntity;
			if (!nearbyPlayer.getGameMode().equals(GameMode.SURVIVAL)
					|| nearbyPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				continue; // Don't allow dead or invisible players to contest.
			}
			TeamGame.getTeamIdx(nearbyPlayer).ifPresent(teamIdx -> {
				if (teamIdx == 0) {
					this.teamAContesting = true;
				} else if (teamIdx == 1) {
					this.teamBContesting = true;
				}
			});
		}
		if (EscortPlugin.GAME.getPhase().equals(MinigamePhase.OVERTIME)) {
			int winningTeam = this.getWinningTeam();
			if (winningTeam != 0) {
				// If we are in overtime because the losing team has touched the Robot (rather
				// than due to a draw state) the losing team must stay on the Robot!
				if (!(winningTeam == 1 ? this.teamBContesting : this.teamAContesting)) {
					// Losing team is not contesting! End the game.
					int losingTeamIdx = winningTeam == 1 ? 1 : 0;
					Message.m(TeamGame.getTeamChatColor(losingTeamIdx) + TeamGame.getTeamName(losingTeamIdx)
							+ " Team &rblew their last chance and could not maintain control of the &aRobot&r during &aOVERTIME&r!")
							.broadcast();
					EscortPlugin.GAME._endGame();
				}
			}
		}
		if (this.teamAContesting ^ this.teamBContesting) {
			_move(this.teamAContesting ? 1 : -1);
		}
	}

	public void forceWin(int teamIdx) {
		if (teamIdx == 0) {
			this.farthestA = 999;
			this.farthestB = 0;
		} else {
			this.farthestA = 0;
			this.farthestB = 999;
		}
		EscortPlugin.GAME._endGame();
	}

	public boolean getTeamAContesting() {
		return this.teamAContesting;
	}

	public boolean getTeamBContesting() {
		return this.teamBContesting;
	}

	/**
	 * @return 0 if draw, 1 if team A, 2 if team B.
	 */
	public int getWinningTeam() {
		if (this.farthestA == this.farthestB) {
			return 0;
		}
		return this.farthestA > this.farthestB ? 1 : 2;
	}

	public String getWinningTeamStr() {
		int winningTeam = this.getWinningTeam();
		switch (winningTeam) {
			case 1:
			case 2:
				int teamIdx = winningTeam - 1;
				return TeamGame.getTeamChatColor(teamIdx) + TeamGame.getTeamName(teamIdx) + " Team";
			default:
				return "DRAW";
		}
	}

	public ArmorStand getAS() {
		return this.as;
	}
}
