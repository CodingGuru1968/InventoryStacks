package com.codingguru.inventorystacks.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMetaStackSizeApplier extends StackSizeApplier {

	@Override
	public void setup() {
	}

	@Override
	public void apply(ItemStack stack, int size) {
		if (stack.getMaxStackSize() == size)
			return;

		ItemMeta currentMeta = stack.getItemMeta();

		if (currentMeta == null)
			return;

		currentMeta.setMaxStackSize(size);
		stack.setItemMeta(currentMeta);
	}

	@Override
	public boolean isModernApi() {
		return true;
	}
}
