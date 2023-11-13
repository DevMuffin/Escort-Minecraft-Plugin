package com.anthonyb.escort.event;

import static com.anthonyb.escort.EscortPlugin.GAME;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.escort.gui.KitGUI;
import com.anthonyb.escort.kits.Kit;
import com.anthonyb.escort.utils.TeleportSafely;
import com.anthonyb.foobarlib.minigame.MinigamePhase;
import com.anthonyb.foobarplugin.item.ItemBuilder;
import com.anthonyb.foobarplugin.message.Message;
import com.anthonyb.foobarplugin.minigame.TeamGame;
import com.anthonyb.foobarplugin.nbt.CustomNBT;
import com.anthonyb.foobarplugin.threads.Threads;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class EntityDamageEvents implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamaged(EntityDamageEvent e) {
		if (GAME.ROBOT != null) {
			if (e.getEntity().equals(GAME.ROBOT.getAS())) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamagedByEntity(EntityDamageByEntityEvent e) {
		if (GAME.ROBOT != null) {
			if (e.getEntity().equals(GAME.ROBOT.getAS())) {
				e.setCancelled(true);
				Entity damager = e.getDamager();
				if (damager instanceof Player) {
					Player player = (Player) damager;
					Message.m("&a[Robot] &rDon\'t hit me!").send(player);
				}
				return;
			}
		}
		Entity damaged = e.getEntity();
		Entity damager = e.getDamager();
		if (e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			double distance = damaged.getLocation().distance(damager.getLocation());
			e.setDamage(Math.max(2, 13 - distance));
			Vector knockbackDir = damaged.getLocation().toVector().subtract(damager.getLocation().toVector())
					.normalize();
			damaged.setVelocity(knockbackDir.multiply(Math.max(0.25, 2.5D - distance)));
		}
		if ((damaged instanceof Player) && (damager instanceof Player)) {
			Player damagedPlayer = (Player) damaged;
			// Player damagerPlayer = (Player) damager;
			UUID damagedPlayerId = damagedPlayer.getUniqueId();
			// UUID damagerPlayerId = damagerPlayer.getUniqueId();
			String damagedKitName = KitGUI.kitSelection.getOrDefault(damagedPlayerId, Kit.WARRIOR.getName());
			// String damagerKitName = KitGUI.kitSelection.getOrDefault(damagerPlayerId,
			// Kit.WARRIOR.getName());
			if (damagedKitName.equals(Kit.NINJA.getName())) {
				if (damagedPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					damagedPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
					damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1 * 20, 2));
					damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 3 * 20, 2));
					damagedPlayer.showTitle(
							Title.title(Component.text(""), Component.text("REVEALED").color(NamedTextColor.RED)));
					Message.m("YOU ARE NOW &aVISIBLE&r!").sendActionbar(damagedPlayer);
				}
			}
			// if (damagerKitName.equals(Kit.NINJA.getName())) {
			// damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1
			// * 20, 0));
			// }
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent e) {
		if (GAME.ROBOT != null) {
			if (e.getEntity().equals(GAME.ROBOT.getAS())) {
				e.setCancelled(true);
			}
		}
	}

	private static List<UUID> alreadyDead = new ArrayList<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player killed = e.getPlayer();
		e.setDroppedExp(0);
		e.getDrops().clear();
		final Location bedSpawnLocation = killed.getBedSpawnLocation();
		if (bedSpawnLocation != null) {
			Threads.runMainLater(() -> {
				if (!killed.isOnline()) {
					return;
				}
				TeleportSafely.tp(killed, bedSpawnLocation);
			}, 1L, EscortPlugin.INSTANCE);
		}
		if (EscortPlugin.GAME.getPhase().equals(MinigamePhase.PREGAME)) {
			e.setKeepInventory(true); // Prevents player from losing their Build phase blocks.
			e.setCancelled(true);
			TeleportSafely.tp(killed, killed.getBedSpawnLocation());
			return;
		}
		if (alreadyDead.contains(killed.getUniqueId())) {
			e.setCancelled(true);
			return;
		}
		Player killer = e.getPlayer().getKiller();
		if (killer != null) {
			String killerKitName = KitGUI.kitSelection.getOrDefault(killer.getUniqueId(), Kit.WARRIOR.getName());
			Kit killerKit = Kit.kits.getOrDefault(killerKitName, Kit.WARRIOR);
			for (ItemBuilder rewardItem_DIRECT : killerKit.getKillRewardItems()) {
				ItemBuilder rewardItem = ItemBuilder.copy(rewardItem_DIRECT);
				rewardItem.name(
						killed.name().color(NamedTextColor.YELLOW)
								.append(Component.text("'s Death Contribution").color(NamedTextColor.YELLOW)));
				killer.getInventory().addItem(rewardItem.is());
			}
		}
		// Cancel the death for our custom death handler.
		e.setCancelled(true);
		alreadyDead.add(killed.getUniqueId());
		killed.setGameMode(GameMode.SPECTATOR);
		int seconds = 5;
		killed.showTitle(Title.title(Component.text("You Died").color(NamedTextColor.RED),
				Component.text(seconds + "s to respawn!")));
		Threads.runMainLater(new BukkitRunnable() {
			@Override
			public void run() {
				if (!EscortPlugin.GAME.getPhase().isGameplayPhase()) {
					return;
				}
				TeleportSafely.tp(killed, bedSpawnLocation); // teleport them again in case they moved.
				alreadyDead.remove(killed.getUniqueId());
				// Just directly copied respawn code here - Anthony TODO - remove respawn code?
				// If I do that I will need to manually count kills here since they wont
				// register with the game:
				String kitSelectedName = KitGUI.kitSelection.getOrDefault(killed.getUniqueId(), Kit.WARRIOR.getName());
				try {
					int teamIdx = killed.getPersistentDataContainer().get(CustomNBT.PLAYER_TEAM_IDX_KEY,
							PersistentDataType.INTEGER);
					Kit.kits.getOrDefault(kitSelectedName, Kit.WARRIOR).apply(killed, teamIdx == 0 ? true : false);
				} catch (Exception exception) {
				}
			}
		}, seconds * 20L, EscortPlugin.INSTANCE);
		// Because death was cancelled, print out the death message explicitly.
		int teamIdx = TeamGame.getTeamIdx(killed).orElse(0);
		NamedTextColor deathMessageColor = TeamGame.getTeamColor(teamIdx);
		// Skull unicode.
		Bukkit.broadcast(Component.text("\u2620 ").color(deathMessageColor)
				.append(e.deathMessage().color(deathMessageColor)));
	}
}
