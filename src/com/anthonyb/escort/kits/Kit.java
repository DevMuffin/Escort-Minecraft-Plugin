package com.anthonyb.escort.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.anthonyb.foobarplugin.item.EnumDisplayName;
import com.anthonyb.foobarplugin.item.InventoryItem;
import com.anthonyb.foobarplugin.item.ItemBuilder;
import com.anthonyb.foobarplugin.item.ItemBuilderParamd.EnchantmentData;

import net.kyori.adventure.text.Component;

public class Kit {

	private static final int HELMET = -1, CHESTPLATE = -2, LEGGINGS = -3, BOOTS = -4;

	public static Map<String, Kit> kits = new HashMap<>();

	public static final Kit WARRIOR = new Kit("Warrior",
			InventoryItem.create().material(Material.STONE_SWORD).name(Component.text("Warrior")))
			.add(CHESTPLATE, ItemBuilder.create().material(Material.LEATHER_CHESTPLATE))
			.add(BOOTS, ItemBuilder.create().material(Material.LEATHER_BOOTS))
			.add(0, ItemBuilder.create().material(Material.STONE_SWORD)),
			RUNNER = new Kit("Runner",
					InventoryItem.create().material(Material.LEATHER_BOOTS).name(Component.text("Runner")))
					.add(HELMET, ItemBuilder.create().material(Material.LEATHER_HELMET))
					.add(CHESTPLATE, ItemBuilder.create().material(Material.LEATHER_CHESTPLATE))
					.add(LEGGINGS, ItemBuilder.create().material(Material.LEATHER_LEGGINGS))
					.add(BOOTS, ItemBuilder.create().material(Material.LEATHER_BOOTS))
					.add(0, ItemBuilder.create().material(Material.WOODEN_SWORD))
					.effect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)),
			ARCHER = new Kit("Archer",
					InventoryItem.create().material(Material.BOW).name(Component.text("Archer"))
							.alore(Component.text("+ Gain arrows when you kill an enemy Player!")))
					.add(CHESTPLATE, ItemBuilder.create().material(Material.CHAINMAIL_CHESTPLATE))
					.add(0, ItemBuilder.create().material(Material.STICK)
							.enchant(new EnchantmentData(Enchantment.KNOCKBACK, 2),
									new EnchantmentData(Enchantment.DAMAGE_ALL, 5)))
					.add(1, ItemBuilder.create().material(Material.BOW)
							.enchant(new EnchantmentData(Enchantment.ARROW_KNOCKBACK, 1)))
					.add(2, ItemBuilder.create().material(Material.ARROW).amount(5))
					.killRewardItems(ItemBuilder.create().material(Material.ARROW).amount(5)),
			NINJA = new Kit("Ninja",
					InventoryItem.create().material(Material.POTION).potionData(new PotionData(PotionType.INVISIBILITY))
							.potionHideEffects().name(Component.text("Ninja"))
							.alore(Component.text("+ Gain invisibility potions when you kill an enemy player!"),
									Component.text("- Cannot contest the Robot while invisible!"),
									Component.text("- Become visible when hit by the enemy"),
									Component.text("- Arrows stuck to you will give away your position!"),
									Component.text("- Weilding your weapon will give away your position!")))
					.add(1, ItemBuilder.create().material(Material.IRON_AXE))
					.add(2, ItemBuilder.create().material(Material.POTION)
							.potionData(new PotionData(PotionType.INVISIBILITY, false, false)))
					.killRewardItems(ItemBuilder.create().material(Material.POTION)
							.potionData(new PotionData(PotionType.INVISIBILITY, false, false))),
			ICEMAN = new Kit("Ice Man",
					InventoryItem.create().material(Material.SNOWBALL).name(Component.text("Ice Man")))
					.add(CHESTPLATE, ItemBuilder.create().material(Material.LEATHER_CHESTPLATE))
					.add(0, ItemBuilder.create().material(Material.STONE_SWORD))
					.add(1, ItemBuilder.create().material(Material.SNOWBALL).amount(2))
					.killRewardItems(ItemBuilder.create().material(Material.SNOWBALL)),
			MINER = new Kit("Miner",
					InventoryItem.create().material(Material.STONE_PICKAXE).name(Component.text("Miner")))
					.add(HELMET, ItemBuilder.create().material(Material.LEATHER_HELMET))
					.add(CHESTPLATE, ItemBuilder.create().material(Material.LEATHER_CHESTPLATE))
					.add(LEGGINGS, ItemBuilder.create().material(Material.LEATHER_LEGGINGS))
					.add(BOOTS, ItemBuilder.create().material(Material.LEATHER_BOOTS))
					.add(0, ItemBuilder.create().material(Material.STONE_PICKAXE))
					.add(1, ItemBuilder.create().material(Material.TNT))
					.killRewardItems(ItemBuilder.create().material(Material.TNT));

	private Map<Integer, ItemStack> items;

	private boolean canPickupArrows;

	private String name;
	private InventoryItem menuItem;

	private ItemBuilder[] killRewardItems;

	private List<PotionEffect> effects;

	public Kit(String name, InventoryItem menuItem) {
		this.name = name;
		this.menuItem = menuItem;
		this.killRewardItems = new ItemBuilder[0];
		if (this.name.equalsIgnoreCase("Archer")) {
			this.canPickupArrows = true;
		} else {
			this.canPickupArrows = false;
		}
		this.items = new HashMap<>();
		this.effects = new ArrayList<PotionEffect>();
		kits.put(name, this);
	}

	public Kit add(int position, ItemBuilder itemBuilder) {
		ItemStack is = itemBuilder.is();
		this.items.put(position, is);
		String description = EnumDisplayName.of(is.getType().name());
		if (is.getAmount() > 1) {
			description += " x" + is.getAmount();
		}
		this.menuItem.alore(Component.text(description));
		return this;
	}

	public Kit killRewardItems(ItemBuilder... items) {
		killRewardItems = items;
		return this;
	}

	public Kit effect(PotionEffect potionEffect) {
		this.effects.add(potionEffect);
		return this;
	}

	public void apply(Player player, boolean teamA) {
		AttributeInstance genericMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		double maxHealth = genericMaxHealth == null ? 20D : genericMaxHealth.getValue();
		player.setHealth(maxHealth);
		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
		player.setGameMode(GameMode.SURVIVAL);
		player.setArrowsStuck(0);
		player.setArrowsInBody(0);

		PlayerInventory inventory = player.getInventory();
		player.getEquipment().clear();
		inventory.clear();
		for (int slot : this.items.keySet()) {
			ItemStack item = this.items.get(slot);
			ItemMeta im = item.getItemMeta();
			if (im instanceof LeatherArmorMeta) {
				if (teamA) {
					((LeatherArmorMeta) im).setColor(Color.BLUE);
				} else {
					((LeatherArmorMeta) im).setColor(Color.RED);
				}
				item.setItemMeta(im);
			}
			switch (slot) {
				case HELMET:
					player.getEquipment().setHelmet(item);
					break;
				case CHESTPLATE:
					player.getEquipment().setChestplate(item);
					break;
				case LEGGINGS:
					player.getEquipment().setLeggings(item);
					break;
				case BOOTS:
					player.getEquipment().setBoots(item);
					break;
				default:
					inventory.setItem(slot, item);
			}
		}
		// Always add a pickaxe so the player can break the blocks they are allowed to.
		inventory.addItem(ItemBuilder.create().material(Material.WOODEN_PICKAXE).is());
		player.addPotionEffects(effects);
	}

	public boolean canPickupArrows() {
		return this.canPickupArrows;
	}

	public String getName() {
		return this.name;
	}

	public InventoryItem getMenuItem() {
		return this.menuItem.copy();
	}

	public ItemBuilder[] getKillRewardItems() {
		return this.killRewardItems;
	}

	public int price() {
		return 10;
	}
}
