package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;

public class InventoryClick implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onEnchantedBookClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getInventory().getType() != InventoryType.ANVIL)
			return;

		if (e.getSlotType() != SlotType.RESULT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(Material.ENCHANTED_BOOK))
			return;

		ItemStack craftedItem = e.getInventory().getContents()[1];

		if (craftedItem.getType() != Material.ENCHANTED_BOOK)
			return;

		if (craftedItem.getAmount() <= 1)
			return;

		ItemStack newItem = craftedItem.clone();
		newItem.setAmount(craftedItem.getAmount() - 1);
		ItemUtil.addItem((Player) e.getWhoClicked(), newItem);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onToolStackedClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getInventory().getType() != InventoryType.ANVIL)
			return;

		if (e.getSlotType() != SlotType.RESULT)
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("disallow-stacked-anvil-items"))
			return;

		ItemStack craftedItem = e.getInventory().getContents()[0];

		if (craftedItem.getAmount() <= 1)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(craftedItem.getType()))
			return;

		e.getWhoClicked().closeInventory();
		e.setCancelled(true);
		MessagesUtil.sendMessage(e.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
	}
}