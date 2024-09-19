package com.codingguru.inventorystacks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class InventoryClick implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onBrewingStandClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getCurrentItem().getAmount() <= 1)
			return;

		if (VersionUtil.v1_18_R1.isServerVersionHigher())
			return;

		if (e.getInventory().getType() != InventoryType.BREWING)
			return;

		if (e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(XMaterialUtil.matchXMaterial(e.getCurrentItem().getType())))
			return;

		e.setCancelled(true);

		ItemStack newItem = e.getCurrentItem().clone();
		newItem.setAmount(1);
		addItemToBrewingStand(e.getInventory(), e.getCurrentItem(), newItem);
	}

	private void addItemToBrewingStand(Inventory inventory, ItemStack original, ItemStack item) {
		ItemStack slot1 = inventory.getItem(0);
		ItemStack slot2 = inventory.getItem(1);
		ItemStack slot3 = inventory.getItem(2);

		if (slot1 == null || slot1.getType() == Material.AIR) {
			inventory.setItem(0, item);
			original.setAmount(original.getAmount() - 1);
			return;
		} else if (slot2 == null || slot2.getType() == Material.AIR) {
			inventory.setItem(1, item);
			original.setAmount(original.getAmount() - 1);
			return;
		} else if (slot3 == null || slot3.getType() == Material.AIR) {
			inventory.setItem(2, item);
			original.setAmount(original.getAmount() - 1);
			return;
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

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(XMaterialUtil.matchXMaterial(e.getCurrentItem().getType())))
			return;

		Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), () -> {
			((Player) e.getWhoClicked()).updateInventory();
		}, 2L);
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