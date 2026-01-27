package com.codingguru.inventorystacks.scheduler;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.ItemUtil;

public class FixBrewingStandTask extends Schedule {

	private final Inventory destination;
	private final Inventory source;
	private final ItemStack currentItem;

	public FixBrewingStandTask(Inventory destination, Inventory source, ItemStack currentItem) {
		this.destination = destination;
		this.source = source;
		this.currentItem = currentItem;
	}

	@Override
	public void run() {
		if (!ItemUtil.addItemToBrewingStand(destination, currentItem))
			return;

		ItemUtil.removeOneMatching(source, currentItem);
	}
}