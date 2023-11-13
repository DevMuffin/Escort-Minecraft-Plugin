package com.anthonyb.escort;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.anthonyb.escort.gui.KitGUI;
import com.anthonyb.escort.kits.Kit;
import com.anthonyb.escort.robot.Robot;
import com.anthonyb.escort.utils.TeleportSafely;
import com.anthonyb.escort.worldedit.EditWorld;
import com.anthonyb.foobarlib.aws.dynamo.category.GameServerData;
import com.anthonyb.foobarlib.java.MutableInt;
import com.anthonyb.foobarlib.minigame.MinigamePhase;
import com.anthonyb.foobarplugin.item.ItemBuilder;
import com.anthonyb.foobarplugin.message.Message;
import com.anthonyb.foobarplugin.message.Message.SoundInfo;
import com.anthonyb.foobarplugin.minigame.Minigame;
import com.anthonyb.foobarplugin.minigame.ScoreboardGame;
import com.anthonyb.foobarplugin.minigame.TeamGame;
import com.anthonyb.foobarplugin.minigame.TimedGame;
import com.anthonyb.foobarplugin.threads.Threads;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;

public class EscortGame extends Minigame implements TimedGame, ScoreboardGame {

	private Scoreboard scoreboard;

	private MutableInt timeLeft;
	public Robot ROBOT;

	private EditSession bedrockWallSession;
	private com.sk89q.worldedit.world.World weWorld;

	public EscortGame(JavaPlugin plugin) {
		super(plugin);
		super.disableDurability();
		super.disableHunger();
		super.disableDayNightCycle();
		super.disableMobSpawning();
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.timeLeft = new MutableInt(this.duration());
	}

	@Override
	public Objective createObjective(DisplaySlot displaySlot) {
		switch (displaySlot) {
			default:
			case SIDEBAR:
				Objective pushObj = this.scoreboard.registerNewObjective("Push", "Push",
						Component.text("Push").color(NamedTextColor.AQUA));
				pushObj.setDisplaySlot(DisplaySlot.SIDEBAR);
				return pushObj;
		}
	}

	@Override
	public void addComponents() {
		super.components.put(TeamGame.COMPONENT_ID, new TeamGame(2, this));
	}

	@Override
	public int duration() {
		return 5 * 60 * 20;
		// return 30 * 20;
		// return 2 * 60 * 20;

		// return 5 * 60 * 20; 5 minutes TOTAL (this includes the build pre-game phase)
	}

	@Override
	public MutableInt timeLeft() {
		return this.timeLeft;
	}

	private void startGameplayPhase(Location spawnRobotLoc, Vector robotDir) {
		super.setPhase(MinigamePhase.IN_PROGRESS);
		if (bedrockWallSession != null) {
			bedrockWallSession.undo(WorldEdit.getInstance().newEditSession(weWorld));
		}
		// Put winning team into the Scoreboard.
		Team winningTeam = this.getScoreboard().registerNewTeam("winningTeam");
		winningTeam.addEntry(ChatColor.YELLOW + " \u25C0"); // Left facing arrow.
		Objective sidebarObjective = this.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
		sidebarObjective.getScore(ChatColor.LIGHT_PURPLE + "Winning Team").setScore(5);
		sidebarObjective.getScore(ChatColor.YELLOW + " \u25C0").setScore(4); // Left facing arrow.
		// Create the Robot.
		ROBOT = new Robot(spawnRobotLoc, robotDir);
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (getPhase().equals(MinigamePhase.ENDING)) {
					this.cancel();
					return;
				}
				ROBOT.move();
			}
		};
		br.runTaskTimer(EscortPlugin.INSTANCE, 0L, 10L);

		// TeamGame teamGame = (TeamGame) super.components.get(TeamGame.COMPONENT_ID);

		for (Player player : Bukkit.getOnlinePlayers()) {
			String kitSelectedName = KitGUI.kitSelection.getOrDefault(player.getUniqueId(), Kit.WARRIOR.getName());
			Kit.kits.getOrDefault(kitSelectedName, Kit.WARRIOR).apply(player, true);
			Location bedSpawnLocation = player.getBedSpawnLocation();
			if (bedSpawnLocation != null) {
				TeleportSafely.tp(player, bedSpawnLocation);
			}
			player.setGameMode(GameMode.SURVIVAL);
			player.showTitle(Title.title(Component.text("FIGHT").color(NamedTextColor.DARK_RED), Component.text("")));
		}
	}

	@Override
	protected void _startGame() {
		// Cancel any active spindown tasks since a game is starting! Anthony TODO -
		// when you move this to the Minigame area of FooBarLib, this should probably
		// happen when the state is set to LOBBY_STARTING rather than actually in game
		// since you wouldn't want the game to be cancelled if the lobby is about to
		// start!
		EscortPlugin.cancelSpindownTask();
		// TODO - set these correctly for every map.

		World world = Bukkit.getWorlds().get(0);
		// Old version of push map 1: 75, 1, 9
		// new version of push map 1: 171 39 66
		Location spawnRobotLoc = new Location(world, 171, 39, 66);
		Vector robotDir = new Vector(0, 0, 1); // old map: 1f on x, new map: 1f on z
		Vector perpRobotDir = new Vector(robotDir.getZ(), robotDir.getY(), robotDir.getX()); // Gets a perpendicular
																								// vector to the
																								// movement vector.

		Location aSpawn = spawnRobotLoc.clone().add(robotDir.clone().multiply(-65)), // old map: was 80, now 65
				bSpawn = spawnRobotLoc.clone().add(robotDir.clone().multiply(65)); // old map: was 80, now 65
		// Make sure the spawns are facing the robot.
		aSpawn.setDirection(spawnRobotLoc.toVector().subtract(aSpawn.toVector()));
		bSpawn.setDirection(spawnRobotLoc.toVector().subtract(bSpawn.toVector()));

		super.setPhase(MinigamePhase.PREGAME);
		int radiusOut = 200;
		BlockVector3 startBedrockWall = BlockVector3.at(spawnRobotLoc.getBlockX() - perpRobotDir.getX() * radiusOut,
				-65, spawnRobotLoc.getBlockZ() - perpRobotDir.getZ() * radiusOut);
		BlockVector3 endBedrockWall = BlockVector3.at(spawnRobotLoc.getBlockX() + perpRobotDir.getX() * radiusOut,
				spawnRobotLoc.getBlockY() + 100,
				spawnRobotLoc.getBlockZ() + perpRobotDir.getZ() * radiusOut);
		Message sendNoWallMessageAfterTP = null;
		try {
			// throw new RuntimeException("DEVELOPER TESTING SKIPPING WORLD EDIT");
			bedrockWallSession = EditWorld.set(weWorld = EditWorld.convertWorld(world),
					BlockTypes.BEDROCK.getDefaultState(),
					startBedrockWall, endBedrockWall);
		} catch (/* WorldEdit */Exception e) {
			sendNoWallMessageAfterTP = Message.m("NO WALL! GO CRAZY!")
					.withSound(SoundInfo.create(Sound.ENTITY_ENDER_DRAGON_GROWL));
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(this.scoreboard);
			player.showTitle(Title.title(Component.text("BUILD").color(NamedTextColor.DARK_RED), Component.text("")));
		}

		Threads.runMainLater(() -> {
			if (!super.getPhase().equals(MinigamePhase.PREGAME)) {
				return;
			}
			startGameplayPhase(spawnRobotLoc, robotDir);
		}, /* 40 * 20L */ 40 * 20L, EscortPlugin.INSTANCE); // 40 seconds

		TeamGame teamGame = (TeamGame) super.components.get(TeamGame.COMPONENT_ID);

		for (UUID playerAId : teamGame.getTeam(0)) {
			Player playerA = Bukkit.getPlayer(playerAId);
			playerA.playerListName(playerA.playerListName().color(NamedTextColor.BLUE));
			String kitSelectedName = KitGUI.kitSelection.getOrDefault(playerA.getUniqueId(), Kit.WARRIOR.getName());
			Kit.kits.getOrDefault(kitSelectedName, Kit.WARRIOR).apply(playerA, true);
			TeleportSafely.tp(playerA, aSpawn);
			playerA.setGameMode(GameMode.SURVIVAL);
			playerA.setBedSpawnLocation(aSpawn, true);
			playerA.getInventory().addItem(ItemBuilder.create().material(Material.BLUE_CONCRETE).amount(50).is());
		}
		for (UUID playerBId : teamGame.getTeam(1)) {
			Player playerB = Bukkit.getPlayer(playerBId);
			playerB.playerListName(playerB.playerListName().color(NamedTextColor.RED));
			String kitSelectedName = KitGUI.kitSelection.getOrDefault(playerB.getUniqueId(), Kit.WARRIOR.getName());
			Kit.kits.getOrDefault(kitSelectedName, Kit.WARRIOR).apply(playerB, false);
			TeleportSafely.tp(playerB, bSpawn);
			playerB.setGameMode(GameMode.SURVIVAL);
			playerB.setBedSpawnLocation(bSpawn, true);
			playerB.getInventory().addItem(ItemBuilder.create().material(Material.RED_CONCRETE).amount(50).is());
		}
		if (sendNoWallMessageAfterTP != null) {
			sendNoWallMessageAfterTP.broadcast();
		}
	}

	@Override
	protected void _resetGame() {
		// Tell us the current time in EST.
		Bukkit.getLogger().info("RESET GAME " + Instant.now().atZone(ZoneId.of("America/New_York")));
		EscortPlugin.assignNewSpindownTask();
	}

	public void _endGame() {
		if (super.getPhase().isEndPhase()) {
			return; // Already ending.
		}
		Bukkit.getScheduler().cancelTasks(EscortPlugin.INSTANCE); // Cancel all tasks that have not run yet.
		super.setPhase(MinigamePhase.ENDING);
		int winningTeam = ROBOT.getWinningTeam();
		ROBOT.getAS().remove();
		Message.m((winningTeam == 1 ? TeamGame.getTeamChatColor(0) + TeamGame.getTeamName(0)
				: TeamGame.getTeamChatColor(1) + TeamGame.getTeamName(1)) + " Team &rhas won!").broadcast();
		TeamGame teamGame = (TeamGame) super.components.get(TeamGame.COMPONENT_ID);
		teamGame.getTeamPlayers(0).forEach(player -> {
			player.playSound(player.getLocation(),
					winningTeam == 1 ? Sound.MUSIC_DISC_PIGSTEP : Sound.MUSIC_DISC_MELLOHI, 100f, 1f);
			player.showTitle(
					Title.title(Component.text(winningTeam == 1 ? "You Win" : "L").color(TeamGame.getTeamColor(0)),
							Component.text("")));
		});
		teamGame.getTeamPlayers(1).forEach(player -> {
			player.playSound(player.getLocation(),
					winningTeam == 1 ? Sound.MUSIC_DISC_MELLOHI : Sound.MUSIC_DISC_PIGSTEP, 100f, 1f);
			player.showTitle(
					Title.title(Component.text(winningTeam == 1 ? "L" : "You Win").color(TeamGame.getTeamColor(1)),
							Component.text("")));
		});
		Threads.runMainLater(() -> {
			// End the game.
			super.setPhase(MinigamePhase.ENDED);
		}, 10 * 20L, EscortPlugin.INSTANCE);
	}

	@Override
	public void endGame() {
		// Check overtime first.
		// - if robot is contested by losing team
		// - if the game is currently in a draw state (winning team is 0)
		Runnable overtime = () -> {
			Message.m("[!!!] OVERTIME [!!!]").broadcast();
			super.setPhase(MinigamePhase.OVERTIME);
			this.getScoreboard().getTeam("timeLeftTeam").prefix(Component.text("** OVERTIME "));
		};
		int winningTeam = ROBOT.getWinningTeam();
		if (winningTeam == 0) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.showTitle(
						Title.title(Component.text("OVERTIME"), Component.text("The next team to PUSH wins!")));
			}
			overtime.run();
		} else if (winningTeam == 1 && ROBOT.getTeamBContesting()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.showTitle(Title.title(Component.text("OVERTIME").color(Message.ACCENT_COLOR_COMPONENT),
						Component.text(TeamGame.getTeamName(1) + " Team is contesting the Robot!")
								.color(TeamGame.getTeamColor(1))));
			}
			overtime.run();
		} else if (winningTeam == 2 && ROBOT.getTeamAContesting()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.showTitle(Title.title(Component.text("OVERTIME").color(Message.ACCENT_COLOR_COMPONENT),
						Component.text(TeamGame.getTeamName(0) + " Team is contesting the Robot!")
								.color(TeamGame.getTeamColor(0))));
			}
			overtime.run();
		} else {
			this._endGame();
		}
	}

	@Override
	public Location getLobbyLoc() {
		return new Location(Bukkit.getWorlds().get(0), 0, 1, 0);
	}

	@Override
	public int getSuggestedMinPlayers() {
		return 2;// 4;
	}

	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	@Override
	public String getServerDataTableId() {
		return GameServerData.PUSHGAMEDATA_TABLE_ID;
	}
}
