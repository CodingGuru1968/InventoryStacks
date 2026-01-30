package com.codingguru.inventorystacks.listeners.itemmeta;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.Schedule;

public class UpdateItemMetaListener implements Listener {

	private final java.util.Set<Integer> scheduled = java.util.Collections
			.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		callNow(e.getCurrentItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPickup(EntityPickupItemEvent e) {
		callNow(e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent e) {
		callNow(e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMove(InventoryMoveItemEvent e) {
		callNow(e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent e) {
		callLater(e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		callLater(e.getEntity().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPrepareCrafter(CrafterCraftEvent e) {
		callLater(e.getResult());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent e) {
		callLater(e.getResult());
	}

	private void callNow(ItemStack stack) {
		if (stack == null)
			return;

		if (stack.getType().isAir())
			return;

		ItemHandler.getInstance().applyItem(false, stack);
	}

	private void callLater(ItemStack stack) {
		if (stack == null)
			return;

		if (stack.getType().isAir())
			return;

		final int key = System.identityHashCode(stack);

		if (!scheduled.add(key))
			return;

		Schedule stackApplyTask = new Schedule() {
			@Override
			public void run() {
				try {
					ItemHandler.getInstance().applyItem(false, stack);
				} finally {
					scheduled.remove(key);
				}
			}
		};

		stackApplyTask.runTaskLater(1L);
	}
}