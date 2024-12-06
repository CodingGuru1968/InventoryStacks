package com.codingguru.inventorystacks.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {
	
	public static boolean addItemToBrewingStand(Inventory inventory, ItemStack item) {
		ItemStack slot1 = inventory.getItem(0);
		ItemStack slot2 = inventory.getItem(1);
		ItemStack slot3 = inventory.getItem(2);

		if (slot1 == null || slot1.getType() == Material.AIR) {
			inventory.setItem(0, item);
			return true;
		} else if (slot2 == null || slot2.getType() == Material.AIR) {
			inventory.setItem(1, item);
			return true;
		} else if (slot3 == null || slot3.getType() == Material.AIR) {
			inventory.setItem(2, item);
			return true;
		}
		
		return false;
	}

	public static void addItem(Player player, ItemStack item) {
		int amount = item.getAmount();
		int maxAmount = item.getMaxStackSize();

		if (amount > maxAmount) {
			item.setAmount(maxAmount);
			add(player, item);

			for (int i = amount - maxAmount; i >= maxAmount; i -= maxAmount) {
				add(player, item);
			}

			if (amount % maxAmount > 0) {
				item.setAmount(amount % maxAmount);
				add(player, item);
			}
		} else {
			add(player, item);
		}
	}

	private static void add(Player player, ItemStack item) {
		if (player.getInventory().firstEmpty() == -1) {
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		} else {
			player.getInventory().addItem(item);
		}
	}

}