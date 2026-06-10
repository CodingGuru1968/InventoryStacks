package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.VersionUtil;

public class TotemFix implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onShiftClick(InventoryClickEvent e) {
		if (!VersionUtil.v1_9_R1.isServerVersionHigher())
			return;

		if (!(e.getClickedInventory() instanceof PlayerInventory))
			return;

		if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)
			return;

		if (!ItemHandler.getInstance().hasEditedStackSize(Material.TOTEM_OF_UNDYING))
			return;

		Player player = (Player) e.getWhoClicked();
		int slot = e.getSlot();

		if (slot < 0 || slot > 35)
			return;

		ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType() != Material.TOTEM_OF_UNDYING)
			return;

		ItemStack offhand = player.getInventory().getItemInOffHand();
		int offhandAmount = offhand == null || offhand.getType() == Material.AIR ? 0 : offhand.getAmount();
		int maxStack = clicked.hasItemMeta() && clicked.getItemMeta().hasMaxStackSize()
				? clicked.getItemMeta().getMaxStackSize()
				: 1;

		if (offhand != null && offhand.getType() == Material.TOTEM_OF_UNDYING && offhand.getAmount() >= maxStack)
			return;

		e.setCancelled(true);
		int total = offhandAmount + clicked.getAmount();

		ItemStack newOffhand = clicked.clone();
		if (total <= maxStack) {
			newOffhand.setAmount(total);
			player.getInventory().setItemInOffHand(newOffhand);
			e.setCurrentItem(null);
		} else {
			newOffhand.setAmount(maxStack);
			player.getInventory().setItemInOffHand(newOffhand);
			clicked.setAmount(total - maxStack);
			e.setCurrentItem(clicked);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onRegularClick(InventoryClickEvent e) {
		if (!VersionUtil.v1_9_R1.isServerVersionHigher())
			return;

		if (e.getSlot() != 40)
			return;

		if (e.getClick() != ClickType.LEFT)
			return;

		if (!ItemHandler.getInstance().hasEditedStackSize(Material.TOTEM_OF_UNDYING))
			return;

		ItemStack cursor = e.getCursor();

		if (cursor == null || cursor.getType() != Material.TOTEM_OF_UNDYING)
			return;

		switch (e.getAction()) {
		case PLACE_ALL:
		case SWAP_WITH_CURSOR:
			break;
		default:
			return;
		}

		Player player = (Player) e.getWhoClicked();

		ItemStack offhand = player.getInventory().getItemInOffHand();
		int offhandAmount = offhand == null ? 0 : offhand.getAmount();
		int cursorAmount = cursor.getAmount();
		int total = offhandAmount + cursorAmount;
		int maxStack = cursor.hasItemMeta() && cursor.getItemMeta().hasMaxStackSize()
				? cursor.getItemMeta().getMaxStackSize()
				: 1;

		ItemStack newOffhand = cursor.clone();

		if (total <= maxStack) {
			newOffhand.setAmount(total);
			e.setCancelled(true);
			player.getInventory().setItemInOffHand(newOffhand);
			player.setItemOnCursor(null);
		} else {
			newOffhand.setAmount(maxStack);
			ItemStack remaining = cursor.clone();
			remaining.setAmount(total - maxStack);
			e.setCancelled(true);
			player.getInventory().setItemInOffHand(newOffhand);
			player.setItemOnCursor(remaining);
		}
	}
}