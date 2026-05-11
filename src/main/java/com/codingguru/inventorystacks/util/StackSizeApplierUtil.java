package com.codingguru.inventorystacks.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.items.ItemMetaStackSizeApplier;
import com.codingguru.inventorystacks.items.LegacyNmsStackSizeApplier;
import com.codingguru.inventorystacks.items.StackSizeApplier;

public final class StackSizeApplierUtil {

	public static StackSizeApplier create() {
		if (hasItemMetaSetMaxStackSize() && !shouldUseLegacyReflection()) {
			return new ItemMetaStackSizeApplier();
		}

		return new LegacyNmsStackSizeApplier();
	}

	public static boolean shouldUseLegacyReflection() {
		if (InventoryStacks.getInstance().getConfig().getBoolean("use-legacy-reflection", false))
			return true;

		return isGeyserCompatibilityEnabled() && isGeyserPresent();
	}

	public static boolean isGeyserCompatibilityEnabled() {
		return InventoryStacks.getInstance().getConfig().getBoolean("geyser-support.auto-use-legacy-reflection", true);
	}

	public static boolean isGeyserPresent() {
		return Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null
				|| Bukkit.getPluginManager().getPlugin("Geyser-Bukkit") != null
				|| Bukkit.getPluginManager().getPlugin("GeyserMC") != null
				|| Bukkit.getPluginManager().getPlugin("floodgate") != null;
	}

	private static boolean hasItemMetaSetMaxStackSize() {
		try {
			ItemMeta.class.getMethod("setMaxStackSize", Integer.class);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

}
