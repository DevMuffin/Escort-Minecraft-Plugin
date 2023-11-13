package com.anthonyb.escort.gui;

import static com.anthonyb.foobarplugin.inventory.ModernInventoryParamd.BORDER;
import static com.anthonyb.foobarplugin.inventory.ModernInventoryParamd.TITLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.anthonyb.escort.EscortPlugin;
import com.anthonyb.escort.kits.Kit;
import com.anthonyb.foobarlib.aws.dynamo.category.BasicData;
import com.anthonyb.foobarlib.aws.dynamo.category.PushData;
import com.anthonyb.foobarplugin.FooBarPlugin;
import com.anthonyb.foobarplugin.inventory.DynamicInventory;
import com.anthonyb.foobarplugin.inventory.ModernInventory;
import com.anthonyb.foobarplugin.inventory.pane.PrepopulatedPane;
import com.anthonyb.foobarplugin.item.InventoryItem;
import com.anthonyb.foobarplugin.item.ItemBuilderParamd.InventoryItemClickEvent;
import com.anthonyb.foobarplugin.message.Message;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class KitGUI {

	public static HashMap<UUID, String> kitSelection = new HashMap<>();

	private static final ModernInventory KITS_INV = ModernInventory.create(TITLE(ChatColor.GREEN + "Select Kit"))
			.border(BORDER(InventoryItem.create().material(Material.BLACK_STAINED_GLASS_PANE).noname()));

	// private static final Inventory KITS_INV = Bukkit.createInventory(null, 9,
	// Component.text("Select Kit").color(NamedTextColor.GREEN));

	static {
		List<InventoryItem> paneItems = new ArrayList<>();
		for (String kitName : Kit.kits.keySet()) {
			Kit kit = Kit.kits.get(kitName);
			paneItems.add(kit.getMenuItem().click(e -> {
				kitSelection.put(e.player().getUniqueId(), kitName);
				e.player().closeInventory();
				Message.m("Selected kit: &a" + kitName + "&r!").send(e.player());
			}));
		}
		KITS_INV.addPane(new PrepopulatedPane(paneItems));
	}

	// public static void open(Player p) {
	// KITS_INV.open(p);
	// }

	private static void onSelectKit(InventoryItemClickEvent e, Kit kit) {
		String kitName = kit.getName();
		kitSelection.put(e.player().getUniqueId(), kitName);
		e.player().closeInventory();
		Message.m("Selected kit: &a" + kitName + "&r!").send(e.player());
	}

	private static void onPurchaseKit(InventoryItemClickEvent e, Kit kit) {
		Player p = e.player();
		UUID playerId = p.getUniqueId();
		BasicData basicData = FooBarPlugin.getBasicData(playerId);
		PushData pushData = EscortPlugin.getPushData(playerId);
		int price = kit.price();
		boolean purchased = basicData.purchase(price);
		if (!purchased) {
			return;
		}
		pushData.addKit(kit.getName());
		openKitsMenu(p);
	}

	private static void openBuyKitMenu(Player p) {
		PushData pushData = EscortPlugin.getPushData(p.getUniqueId());
		List<InventoryItem> items = new ArrayList<>();
		int slot = 0;
		items.add(InventoryItem.create().material(Material.COMMAND_BLOCK).name("Back to Kits Menu").click(e -> {
			openKitsMenu(e.player());
		}).slot(slot));
		slot = 9;
		for (String kitName : Kit.kits.keySet()) {
			Kit kit = Kit.kits.get(kitName);
			boolean alreadyPurchased = pushData.hasKit(kitName);
			if (!alreadyPurchased) {
				items.add(kit.getMenuItem().click(e -> {
					KitGUI.onPurchaseKit(e, kit);
				}).alore(Component.text("Price: " + kit.price())));
			}
		}
		DynamicInventory inventory = DynamicInventory.create(p.getUniqueId(), TITLE("Purchase Kit"))
				.border(BORDER(InventoryItem.create().material(Material.BLACK_STAINED_GLASS_PANE).noname()))
				.items(items);
		inventory.open(p);
	}

	public static void openKitsMenu(Player p) {
		PushData pushData = EscortPlugin.getPushData(p.getUniqueId());
		List<InventoryItem> items = new ArrayList<>();
		int slot = 0;
		items.add(InventoryItem.create().material(Material.COMMAND_BLOCK).name("Buy").click(e -> {
			openBuyKitMenu(e.player());
		}).slot(slot));
		slot = 9;
		for (String kitName : Kit.kits.keySet()) {
			Kit kit = Kit.kits.get(kitName);
			boolean purchased = pushData.hasKit(kitName);
			if (purchased) {
				items.add(kit.getMenuItem().click(e -> {
					KitGUI.onSelectKit(e, kit);
				}));
			}
		}
		DynamicInventory inventory = DynamicInventory.create(p.getUniqueId(), TITLE("Select Kit"))
				.border(BORDER(InventoryItem.create().material(Material.BLACK_STAINED_GLASS_PANE).noname()))
				.items(items);
		inventory.open(p);
	}
}
