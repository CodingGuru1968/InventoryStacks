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
import com.cryptomorin.xseries.XMaterial;

public class InventoryMoveItem implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent e) {
		if (!InventoryStacks.getInstance().getConfig().getBoolean("one-potion-per-slot"))
			return;

		if (e.getDestination().getType() != InventoryType.BREWING)
			return;

		ItemStack it = e.getItem();

		if (it == null || it.getType() == Material.AIR)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterial.matchXMaterial(it.getType())))
			return;

		if (!isAnyPotion(it))
			return;

		Inventory destination = e.getDestination();

		if (!hasEmptyBottleSlot(destination)) {
			e.setCancelled(true);
			return;
		}

		e.setCancelled(true);

		ItemStack one = it.clone();
		one.setAmount(1);

		FixBrewingStandTask task = new FixBrewingStandTask(destination, e.getSource(), one);
		task.runTaskLater(1L);
	}

	private boolean hasEmptyBottleSlot(Inventory inv) {
		for (int i = 0; i < 3; i++) {
			ItemStack s = inv.getItem(i);
			if (s == null || s.getType() == Material.AIR)
				return true;
		}
		return false;
	}

	private boolean isAnyPotion(ItemStack item) {
		Material t = item.getType();
		return t == Material.POTION || (XMaterial.matchXMaterial("SPLASH_POTION").map(XMaterial::get).orElse(null) == t)
				|| (XMaterial.matchXMaterial("LINGERING_POTION").map(XMaterial::get).orElse(null) == t);
	}

}