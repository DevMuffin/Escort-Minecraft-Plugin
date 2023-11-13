package com.anthonyb.escort;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.anthonyb.escort.commands.AdminCommands;
import com.anthonyb.escort.commands.Commands;
import com.anthonyb.escort.event.BlockPlaceBreak;
import com.anthonyb.escort.event.EntityDamageEvents;
import com.anthonyb.escort.event.EntityExplode;
import com.anthonyb.escort.event.PlayerDropPickupItem;
import com.anthonyb.escort.event.PlayerInteract;
import com.anthonyb.escort.event.PlayerItemConsume;
import com.anthonyb.escort.event.PlayerJoin;
import com.anthonyb.escort.event.PlayerRespawn;
import com.anthonyb.escort.event.SnowballEvents;
import com.anthonyb.escort.event.TeamAbandoned;
import com.anthonyb.foobarlib.aws.dynamo.category.PushData;
import com.anthonyb.foobarplugin.threads.Threads;

public class EscortPlugin extends JavaPlugin {

	public static EscortPlugin INSTANCE;

	public static EscortGame GAME;

	private static HashMap<UUID, PushData> pushDataCache = new HashMap<>();

	private static BukkitTask SPINDOWN_TASK = null;

	@Override
	public void onEnable() {
		INSTANCE = this;
		assignNewSpindownTask();
		GAME = new EscortGame(INSTANCE);
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new EntityDamageEvents(), this);
		pm.registerEvents(new PlayerInteract(), this);
		pm.registerEvents(new PlayerJoin(), this);
		pm.registerEvents(new PlayerRespawn(), this);
		pm.registerEvents(new BlockPlaceBreak(), this);
		pm.registerEvents(new PlayerDropPickupItem(), this);
		pm.registerEvents(new TeamAbandoned(), this);
		pm.registerEvents(new SnowballEvents(), this);
		pm.registerEvents(new EntityExplode(), this);
		pm.registerEvents(new PlayerItemConsume(), this);
		AdminCommands adminCommands = new AdminCommands();
		getCommand("admin").setExecutor(adminCommands);
		getCommand("endgame").setExecutor(adminCommands);
		getCommand("kit").setExecutor(new Commands());
		// Bukkit.getMessenger().registerOutgoingPluginChannel(this,
		// BungeeChannel.TEST);
		// Bukkit.getMessenger().registerIncomingPluginChannel(this,
		// BungeeChannel.TEST);
	}

	@Override
	public void onDisable() {
		if (GAME != null) {
			GAME.serverExit();
		}
		// if (GAME != null && !GAME.taskARN().isEmpty()) {
		// if (!LOCAL_DEV) {
		// ECS.unprotectTaskFromDownscale(GAME.serviceName(), GAME.taskARN());
		// }
		// Bukkit.getLogger().info("PROTECTION DISABLED");
		// }
	}

	public static void cancelSpindownTask() {
		if (SPINDOWN_TASK != null) {
			SPINDOWN_TASK.cancel();
		}
	}

	public static void assignNewSpindownTask() {
		cancelSpindownTask();
		SPINDOWN_TASK = Threads.runMainLater(new BukkitRunnable() {
			public void run() {
				if (GAME.getPhase().isGameplayPhase() && Bukkit.getOnlinePlayers().size() > 0) {
					// Don't allow spindown during a gameplay phase with players online!
					assignNewSpindownTask();
					return;
				}
				Bukkit.getServer().shutdown();
			}
		}, 15 * 60 * 20L, INSTANCE);
	}

	public static void setPushData(UUID uuid, PushData pushData) {
		pushDataCache.put(uuid, pushData);
	}

	public static PushData getPushData(UUID uuid) {
		PushData pushData = pushDataCache.get(uuid);
		if (pushData == null) {
			pushData = PushData.get(uuid.toString());
			setPushData(uuid, pushData);
		}
		return pushData;
	}
}
