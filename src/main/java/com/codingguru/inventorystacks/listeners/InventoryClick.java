package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.InventoryUpdateTask;
import com.codingguru.inventorystacks.util.InventoryUtil;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class InventoryClick implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onVillagerTradingClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getCurrentItem().getAmount() <= 1)
			return;

		if (e.getInventory().getType() != InventoryType.MERCHANT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(XMaterialUtil.matchXMaterial(e.getCurrentItem().getType())))
			return;

		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBrewingStandClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getCurrentItem().getAmount() <= 1)
			return;

		if (VersionUtil.v1_20_R4.isServerVersionHigher())
			return;

		if (e.getInventory().getType() != InventoryType.BREWING)
			return;

		if (e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT)
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("one-potion-per-slot"))
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(XMaterialUtil.matchXMaterial(e.getCurrentItem().getType())))
			return;

		if (e.getSlot() < InventoryUtil.getTopInventory(e).getSize())
			return;

		e.setCancelled(true);

		ItemStack newItem = e.getCurrentItem().clone();
		newItem.setAmount(1);

		if (ItemUtil.addItemToBrewingStand(e.getInventory(), newItem)) {
			e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onUpdate(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (VersionUtil.v1_18_R1.isServerVersionHigher())
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("update-inventory-on-merge"))
			return;

		if (e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT
				&& !(e.getCursor() != null && e.getCursor().getType() != Material.AIR))
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(XMaterialUtil.matchXMaterial(e.getCurrentItem().getType())))
			return;

		InventoryUpdateTask updateInventoryTask = new InventoryUpdateTask((Player) e.getWhoClicked());
		updateInventoryTask.runTaskLater(2L);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEnchantedBookClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getInventory().getType() != InventoryType.ANVIL)
			return;

		if (e.getSlotType() != SlotType.RESULT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterialUtil.ENCHANTED_BOOK))
			return;

		ItemStack craftedItem = e.getInventory().getContents()[1];

		if (craftedItem == null)
			return;

		if (craftedItem.getType() != Material.ENCHANTED_BOOK)
			return;

		if (craftedItem.getAmount() <= 1)
			return;

		ItemStack newItem = craftedItem.clone();
		newItem.setAmount(craftedItem.getAmount() - 1);
		ItemUtil.addItem((Player) e.getWhoClicked(), newItem);
	}

	@EventHandler(ignoreCancelled = true)
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

		XMaterialUtil xMaterial = XMaterialUtil.matchXMaterial(craftedItem.getType());

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(xMaterial))
			return;

		e.getWhoClicked().closeInventory();
		e.setCancelled(true);
		MessagesUtil.sendMessage(e.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
	}
}