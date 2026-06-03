package com.codingguru.inventorystacks.listeners.itemmeta;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.hooks.WorldGuardHook;
import com.codingguru.inventorystacks.scheduler.Schedule;

public class UpdateItemMetaListener implements Listener {

	private final java.util.Set<Integer> scheduled = java.util.Collections
			.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
		Location loc = WorldGuardHook.isEnabled() ? e.getWhoClicked().getLocation() : null;
		callNow(player, loc, e.getCurrentItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPickup(EntityPickupItemEvent e) {
		Player player = e.getEntity() instanceof Player ? (Player) e.getEntity() : null;
		Location loc = WorldGuardHook.isEnabled() ? e.getEntity().getLocation() : null;
		callNow(player, loc, e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent e) {
		Location loc = WorldGuardHook.isEnabled() ? WorldGuardHook.getLocationFromInventory(e.getInventory()) : null;
		callNow(null, loc, e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMove(InventoryMoveItemEvent e) {
		if (!WorldGuardHook.isEnabled()) {
			callNow(null, null, e.getItem());
			return;
		}

		if (!InventoryStacks.getInstance().getConfig().getBoolean("worldguard.hopper-support", false))
			return;

		callNow(null, WorldGuardHook.getLocationFromInventory(e.getDestination()), e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent e) {
		Location loc = WorldGuardHook.isEnabled() ? e.getBlock().getLocation() : null;
		callLater(loc, e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		Location loc = WorldGuardHook.isEnabled() ? e.getLocation() : null;
		callLater(loc, e.getEntity().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPrepareCrafter(CrafterCraftEvent e) {
		Location loc = WorldGuardHook.isEnabled() ? e.getBlock().getLocation() : null;
		callLater(loc, e.getResult());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent e) {
		Location loc = WorldGuardHook.isEnabled() ? e.getBlock().getLocation() : null;
		callLater(loc, e.getResult());
	}

	private void callNow(Player player, Location loc, ItemStack stack) {
		if (!shouldHandle(player, loc, stack))
			return;

		ItemHandler.getInstance().applyItem(false, stack);
	}

	private void callLater(Location loc, ItemStack stack) {
		if (!shouldHandle(null, loc, stack))
			return;

		final int key = System.identityHashCode(stack);

		if (!scheduled.add(key))
			return;

		new Schedule() {
			@Override
			public void run() {
				try {
					ItemHandler.getInstance().applyItem(false, stack);
				} finally {
					scheduled.remove(key);
				}
			}
		}.runTaskLater(1L);
	}

	private boolean shouldHandle(Player player, Location loc, ItemStack stack) {
		if (stack == null || stack.getType().isAir()) {
			return false;
		}

		FileConfiguration config = InventoryStacks.getInstance().getConfig();

		if (!ItemHandler.getInstance().hasUpdatedStack(stack)) {
			if (config.getBoolean("auto-stack-cleanup", true)) {
				handleCleanup(stack);
			}
			return false;
		}

		if (config.getBoolean("use-permission.enabled", false) && player != null) {
			if (!player.hasPermission("STACKS.*") && !player.hasPermission("STACKS.USE")) {
				if (config.getBoolean("use-permission.auto-stack-cleanup", true)) {
					handleCleanup(stack);
				}
				return false;
			}
		}

		if (WorldGuardHook.isEnabled() && !WorldGuardHook.isInTargetRegion(loc)) {
			if (config.getBoolean("worldguard.auto-stack-cleanup", true)) {
				handleCleanup(stack);
			}
			return false;
		}

		return true;
	}

	private void handleCleanup(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null || !meta.hasMaxStackSize())
			return;

		meta.setMaxStackSize(null);
		stack.setItemMeta(meta);
	}
}