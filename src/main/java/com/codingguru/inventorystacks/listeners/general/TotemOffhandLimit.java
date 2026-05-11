package com.codingguru.inventorystacks.listeners.general;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public class TotemOffhandLimit implements Listener {

	private static final int OFFHAND_SLOT = 40;

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		if (!isStackedTotemEnabled())
			return;

		Player player = (Player) e.getWhoClicked();

		if (e.getClick() == ClickType.SWAP_OFFHAND) {
			handleSwapOffhandClick(e, player);
			return;
		}

		if (isOffhandSlotClick(e)) {
			handleDirectOffhandClick(e, player);
			return;
		}

		if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			handleShiftMoveToOffhand(e, player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		if (!isStackedTotemEnabled())
			return;

		for (Map.Entry<Integer, ItemStack> entry : e.getNewItems().entrySet()) {
			if (!isOffhandRawSlot(e, entry.getKey()))
				continue;

			ItemStack item = entry.getValue();
			if (isTotem(item) && item.getAmount() > 1) {
				e.setCancelled(true);
				return;
			}
		}
	}

	private void handleSwapOffhandClick(InventoryClickEvent e, Player player) {
		ItemStack moving = e.getCurrentItem();

		if (!isTotem(moving) || moving.getAmount() <= 1)
			return;

		e.setCancelled(true);

		PlayerInventory inventory = player.getInventory();
		if (!isEmpty(inventory.getItemInOffHand()))
			return;

		inventory.setItemInOffHand(singleTotem(moving));
		decreaseCurrentItem(e, moving);
		player.updateInventory();
	}

	private void handleDirectOffhandClick(InventoryClickEvent e, Player player) {
		ItemStack cursor = e.getCursor();
		ItemStack current = e.getCurrentItem();

		if (!isTotem(cursor))
			return;

		if (cursor.getAmount() <= 1 && isEmpty(current))
			return;

		if (cursor.getAmount() > 1 && isEmpty(current) && e.getClick() == ClickType.RIGHT)
			return;

		e.setCancelled(true);

		if (!isEmpty(current))
			return;

		player.getInventory().setItemInOffHand(singleTotem(cursor));
		decreaseCursor(e, cursor);
		player.updateInventory();
	}

	private void handleShiftMoveToOffhand(InventoryClickEvent e, Player player) {
		ItemStack moving = e.getCurrentItem();

		if (!isTotem(moving) || moving.getAmount() <= 1)
			return;

		if (!isEmpty(player.getInventory().getItemInOffHand()))
			return;

		e.setCancelled(true);
		player.getInventory().setItemInOffHand(singleTotem(moving));
		decreaseCurrentItem(e, moving);
		player.updateInventory();
	}

	private boolean isOffhandSlotClick(InventoryClickEvent e) {
		return e.getClickedInventory() instanceof PlayerInventory && e.getSlot() == OFFHAND_SLOT;
	}

	private boolean isOffhandRawSlot(InventoryDragEvent e, int rawSlot) {
		Inventory topInventory = e.getView().getTopInventory();
		return rawSlot >= topInventory.getSize() && e.getView().convertSlot(rawSlot) == OFFHAND_SLOT;
	}

	private boolean isStackedTotemEnabled() {
		return ItemHandler.getInstance().hasEditedStackSize(Material.TOTEM_OF_UNDYING);
	}

	private boolean isTotem(ItemStack item) {
		return item != null && item.getType() == Material.TOTEM_OF_UNDYING;
	}

	private boolean isEmpty(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	private ItemStack singleTotem(ItemStack source) {
		ItemStack one = source.clone();
		one.setAmount(1);
		return one;
	}

	private void decreaseCurrentItem(InventoryClickEvent e, ItemStack source) {
		int newAmount = source.getAmount() - 1;

		if (newAmount <= 0) {
			e.setCurrentItem(null);
			return;
		}

		ItemStack remaining = source.clone();
		remaining.setAmount(newAmount);
		e.setCurrentItem(remaining);
	}

	private void decreaseCursor(InventoryClickEvent e, ItemStack source) {
		int newAmount = source.getAmount() - 1;

		if (newAmount <= 0) {
			e.setCursor(null);
			return;
		}

		ItemStack remaining = source.clone();
		remaining.setAmount(newAmount);
		e.setCursor(remaining);
	}
}
