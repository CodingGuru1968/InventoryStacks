package com.codingguru.inventorystacks.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public class ItemStackComparisonUtil {

	public static boolean isSimilarIgnoringMaxStackSize(ItemStack first, ItemStack second) {
		if (first == null || second == null)
			return false;

		if (!ItemHandler.getInstance().isUsingModernAPI())
			return first.isSimilar(second);

		return withoutMaxStackSize(first).isSimilar(withoutMaxStackSize(second));
	}

	public static boolean hasSameMetaIgnoringMaxStackSize(ItemStack first, ItemStack second) {
		if (first == null || second == null)
			return false;

		if (!ItemHandler.getInstance().isUsingModernAPI())
			return equals(first.getItemMeta(), second.getItemMeta());

		return equals(withoutMaxStackSize(first).getItemMeta(), withoutMaxStackSize(second).getItemMeta());
	}

	private static boolean equals(ItemMeta first, ItemMeta second) {
		return first == null ? second == null : first.equals(second);
	}

	private static ItemStack withoutMaxStackSize(ItemStack original) {
		ItemStack normalized = original.clone();

		if (!normalized.hasItemMeta())
			return normalized;

		ItemMeta meta = normalized.getItemMeta();

		if (meta == null || !meta.hasMaxStackSize())
			return normalized;

		meta.setMaxStackSize(null);
		normalized.setItemMeta(meta);
		return normalized;
	}

}
