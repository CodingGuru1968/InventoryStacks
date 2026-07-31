package com.codingguru.inventorystacks.scheduler;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ItemUtil;

public class FixBrewingStandTask extends Schedule {

	private final Inventory destination;
	private final Inventory source;
	private final ItemStack currentItem;

	public FixBrewingStandTask(InventoryStacks plugin, Inventory destination, Inventory source, ItemStack currentItem) {
		super(plugin);
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