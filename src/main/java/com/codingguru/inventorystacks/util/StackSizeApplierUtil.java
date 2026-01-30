package com.codingguru.inventorystacks.util;

import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.items.ItemMetaStackSizeApplier;
import com.codingguru.inventorystacks.items.LegacyNmsStackSizeApplier;
import com.codingguru.inventorystacks.items.StackSizeApplier;

public final class StackSizeApplierUtil {

	public static StackSizeApplier create() {
		if (hasItemMetaSetMaxStackSize()
				&& !InventoryStacks.getInstance().getConfig().getBoolean("use-legacy-reflection", false)) {
			return new ItemMetaStackSizeApplier();
		}

		return new LegacyNmsStackSizeApplier();
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
