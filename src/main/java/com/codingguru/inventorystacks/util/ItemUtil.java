package com.codingguru.inventorystacks.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

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