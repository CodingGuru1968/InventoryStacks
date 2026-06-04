package com.codingguru.inventorystacks.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.codingguru.inventorystacks.InventoryStacks;

public final class GroundStackUtil {

	private static NamespacedKey groundStackTotalKey;

	private GroundStackUtil() {
	}

	public static int getTotal(Item item) {
		if (item == null)
			return 0;

		int physicalAmount = getPhysicalAmount(item.getItemStack());
		PersistentDataContainer pdc = item.getPersistentDataContainer();
		Integer stored = pdc.get(getGroundStackTotalKey(), PersistentDataType.INTEGER);

		if (stored == null || stored < physicalAmount)
			return physicalAmount;

		return stored;
	}

	public static void setTotal(Item item, int totalAmount) {
		if (item == null)
			return;

		ItemStack stack = item.getItemStack();

		if (stack == null || stack.getType().isAir())
			return;

		int clampedTotal = Math.max(1, totalAmount);
		int physicalCap = Math.max(1, stack.getMaxStackSize());
		int physicalAmount = Math.min(clampedTotal, physicalCap);

		if (stack.getAmount() != physicalAmount) {
			stack = stack.clone();
			stack.setAmount(physicalAmount);
			item.setItemStack(stack);
		}

		if (clampedTotal > physicalAmount) {
			item.getPersistentDataContainer().set(getGroundStackTotalKey(), PersistentDataType.INTEGER, clampedTotal);
		} else {
			clearTotal(item);
		}
	}

	public static void clearTotal(Item item) {
		if (item == null)
			return;

		item.getPersistentDataContainer().remove(getGroundStackTotalKey());
	}

	private static int getPhysicalAmount(ItemStack stack) {
		if (stack == null || stack.getType().isAir())
			return 0;

		return Math.max(0, stack.getAmount());
	}

	private static NamespacedKey getGroundStackTotalKey() {
		if (groundStackTotalKey == null) {
			groundStackTotalKey = new NamespacedKey(InventoryStacks.getInstance(), "ground_stack_total");
		}

		return groundStackTotalKey;
	}
}