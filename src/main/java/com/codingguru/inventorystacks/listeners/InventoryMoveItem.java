package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.FixBrewingStandTask;
import com.codingguru.inventorystacks.util.ServerTypeUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class InventoryMoveItem implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent e) {
		if (VersionUtil.v1_20_R4.isServerVersionHigher()
				&& ItemHandler.getInstance().getServerType() != ServerTypeUtil.PAPER)
			return;

		if (e.getDestination().getType() != InventoryType.BREWING)
			return;

		if (e.getItem() == null || e.getItem().getType() == Material.AIR)
			return;

		if (e.getItem().getType() != Material.POTION)
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("one-potion-per-slot"))
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterialUtil.POTION))
			return;

		Inventory destination = e.getDestination();

		ItemStack slot1 = destination.getItem(0);
		ItemStack slot2 = destination.getItem(1);
		ItemStack slot3 = destination.getItem(2);

		boolean isSpace = (slot1 == null || slot1.getType() == Material.AIR)
				|| (slot2 == null || slot2.getType() == Material.AIR)
				|| (slot3 == null || slot3.getType() == Material.AIR);

		// WORKING PAPER SOLUTION

		e.setCancelled(true);

		if (!isSpace)
			return;

		ItemStack currentItem = e.getItem().clone();
		currentItem.setAmount(1);

		FixBrewingStandTask brewingStandTask = new FixBrewingStandTask(destination, e.getSource(), currentItem);
		brewingStandTask.runTaskLater(1L);
	}

}