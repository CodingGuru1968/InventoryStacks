package com.codingguru.inventorystacks.listeners.general;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.DamageableUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;

public class AnvilStack implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!InventoryStacks.getInstance().getConfig().getBoolean("disallow-stacked-anvil-items")) 
			return;

		if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) 
			return;

		int rawSlot = event.getRawSlot();
		InventoryAction action = event.getAction();
		AnvilInventory anvil = (AnvilInventory) event.getView().getTopInventory();

		boolean isAnvilInputSlot = (rawSlot == 0 || rawSlot == 1);
		boolean isAnvilResultSlot = (rawSlot == 2);
		boolean isPlayerInventoryClick = (rawSlot > 2);

		if (isAnvilResultSlot) {
			ItemStack input1 = anvil.getItem(0);
			ItemStack input2 = anvil.getItem(1);
			if ((input1 != null && input1.getAmount() > 1) || (input2 != null && input2.getAmount() > 1)) {
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
				MessagesUtil.sendMessage(event.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
				return;
			}
		}

		if (isPlayerInventoryClick && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			ItemStack clickedItem = event.getCurrentItem();
			if (clickedItem != null && clickedItem.getType() != Material.AIR) {
				if (isCustomStackable(clickedItem)) {
					if (clickedItem.getAmount() > 1) {
						blockAndNotify(event);
						return;
					}
					if (wouldMergeIntoAnvil(anvil, clickedItem)) {
						blockAndNotify(event);
						return;
					}
				}
			}
		}

		if (isAnvilInputSlot) {
			if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				return;
			}

			ItemStack cursorItem = event.getCursor();
			ItemStack targetSlotItem = anvil.getItem(rawSlot);

			boolean shouldBlock = false;

			if (cursorItem != null && cursorItem.getType() != Material.AIR) {
				if (isCustomStackable(cursorItem)) {
					int existingAmount = (targetSlotItem != null && targetSlotItem.getType() != Material.AIR
							&& targetSlotItem.isSimilar(cursorItem)) ? targetSlotItem.getAmount() : 0;

					if (action == InventoryAction.PLACE_ALL
							|| (action == InventoryAction.PLACE_ONE && existingAmount >= 1) || (existingAmount
									+ (action == InventoryAction.PLACE_ONE ? 1 : cursorItem.getAmount()) > 1)) {
						if (existingAmount >= 1 || cursorItem.getAmount() > 1) {
							shouldBlock = true;
						}
					}

					if (existingAmount == 0 && cursorItem.getAmount() > 1 && action == InventoryAction.PLACE_ALL) {
						shouldBlock = true;
					}
				}
			}

			if (!shouldBlock && action.name().contains("HOTBAR")) {
				int hotbarButton = event.getHotbarButton();
				if (hotbarButton >= 0) {
					ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarButton);
					if (hotbarItem != null && isCustomStackable(hotbarItem)) {
						if (hotbarItem.getAmount() > 1) {
							shouldBlock = true;
						} else if (targetSlotItem != null && targetSlotItem.isSimilar(hotbarItem)) {
							shouldBlock = true;
						}
					}
				}
			}

			if (shouldBlock) {
				blockAndNotify(event);
			}
		}
	}

	private boolean wouldMergeIntoAnvil(AnvilInventory anvil, ItemStack item) {
		ItemStack slot1 = anvil.getItem(0);
		ItemStack slot2 = anvil.getItem(1);
		boolean slot1Matches = (slot1 != null && slot1.isSimilar(item));
		boolean slot2Matches = (slot2 != null && slot2.isSimilar(item));
		return slot1Matches || slot2Matches;
	}

	private void blockAndNotify(InventoryClickEvent event) {
		event.getWhoClicked().closeInventory();
		event.setCancelled(true);
		MessagesUtil.sendMessage(event.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
	}

	private boolean isCustomStackable(ItemStack item) {
		if (item == null) 
			return false;

		if (!ItemHandler.getInstance().hasEditedStackSize(item.getType())) 
			return false;

		Material type = item.getType();
		boolean isToolOrArmor = DamageableUtil.isDamageable(type);
		boolean isBook = type == Material.BOOK || type == Material.ENCHANTED_BOOK || type == Material.WRITTEN_BOOK
				|| type == Material.WRITABLE_BOOK;
		return isToolOrArmor || isBook;
	}
}