package com.codingguru.inventorystacks.items;

import org.bukkit.inventory.ItemStack;

public abstract class StackSizeApplier {

	public abstract void setup();

	protected abstract void apply(ItemStack stack, int size);

	public void applyItem(boolean isStartUp, ItemStack stack, int size) {
		if (isModernApi() && isStartUp)
			return;

		apply(stack, size);
	}

	public abstract boolean isModernApi();

}