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
import com.codingguru.inventorystacks.util.MessagesUtil;

public class AnvilStack implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!InventoryStacks.getInstance().getConfig().getBoolean("disallow-stacked-anvil-items")) {
			return;
		}

		if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) {
			return;
		}

		int rawSlot = event.getRawSlot();
		InventoryAction action = event.getAction();
		AnvilInventory anvil = (AnvilInventory) event.getView().getTopInventory();

		boolean isAnvilInputSlot = (rawSlot == 0 || rawSlot == 1);
		boolean isAnvilResultSlot = (rawSlot == 2);
		boolean isPlayerInventoryClick = (rawSlot > 2);

		if (isAnvilResultSlot) {
			ItemStack input1 = anvil.getItem(0);
			ItemStack input2 = anvil.getItem(1);
			if ((input1 != null && isStackedToolOrBook(input1)) || (input2 != null && isStackedToolOrBook(input2))) {
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
				MessagesUtil.sendMessage(event.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
				return;
			}
		}

		if (isPlayerInventoryClick && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			ItemStack clickedItem = event.getCurrentItem();
			if (clickedItem != null && clickedItem.getType() != Material.AIR) {
				if (isStackedToolOrBook(clickedItem)) {
					event.getWhoClicked().closeInventory();
					event.setCancelled(true);
					MessagesUtil.sendMessage(event.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
					return;
				}
			}
		}

		if (isAnvilInputSlot) {
			ItemStack cursorItem = event.getCursor();
			ItemStack clickedItem = event.getCurrentItem();

			boolean shouldBlock = false;

			if (cursorItem != null && cursorItem.getType() != Material.AIR) {
				if (isStackedToolOrBook(cursorItem)) {
					shouldBlock = true;
				}
			}

			if (!shouldBlock && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				if (clickedItem != null && clickedItem.getType() != Material.AIR) {
					if (isStackedToolOrBook(clickedItem)) {
						shouldBlock = true;
					}
				}
			}

			if (!shouldBlock && action.name().contains("HOTBAR")) {
				int hotbarButton = event.getHotbarButton();
				if (hotbarButton >= 0) {
					ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarButton);
					if (hotbarItem != null && isStackedToolOrBook(hotbarItem)) {
						shouldBlock = true;
					}
				}
			}

			if (shouldBlock) {
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
				MessagesUtil.sendMessage(event.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
			}
		}
	}

	private boolean isStackedToolOrBook(ItemStack item) {
		if (item == null || item.getAmount() <= 1) {
			return false;
		}

		if (!ItemHandler.getInstance().hasEditedStackSize(item.getType())) {
			return false;
		}

		Material type = item.getType();
		String name = type.name();

		boolean isToolOrArmor = name.endsWith("_SWORD") || name.endsWith("_PICKAXE") || name.endsWith("_AXE")
				|| name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_HELMET")
				|| name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
				|| type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT
				|| type == Material.SHIELD || type == Material.ELYTRA || type == Material.FISHING_ROD
				|| type == Material.SHEARS;

		boolean isBook = type == Material.BOOK || type == Material.ENCHANTED_BOOK || type == Material.WRITTEN_BOOK
				|| type == Material.WRITABLE_BOOK;

		return isToolOrArmor || isBook;
	}
}