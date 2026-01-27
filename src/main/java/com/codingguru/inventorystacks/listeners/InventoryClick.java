package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.InventoryUpdateTask;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;

public class InventoryClick implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onBrewingStandShiftMove(InventoryClickEvent e) {
		if (!InventoryStacks.getInstance().getConfig().getBoolean("one-potion-per-slot"))
			return;

		if (!(e.getInventory() instanceof BrewerInventory))
			return;

		if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)
			return;

		if (!(e.getClickedInventory() instanceof PlayerInventory))
			return;

		ItemStack stack = e.getCurrentItem();

		if (stack == null || stack.getType() == Material.AIR)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterial.matchXMaterial(stack.getType())))
			return;

		Inventory brewingInv = e.getInventory();

		int empty = firstEmptyBottleSlot(brewingInv);

		if (empty == -1) {
			e.setCancelled(true);
			return;
		}

		e.setCancelled(true);

		ItemStack one = stack.clone();
		one.setAmount(1);
		brewingInv.setItem(empty, one);

		int newAmount = stack.getAmount() - 1;

		if (newAmount <= 0) {
			e.setCurrentItem(null);
		} else {
			stack.setAmount(newAmount);
			e.setCurrentItem(stack);
		}
	}

	private int firstEmptyBottleSlot(Inventory brewing) {
		for (int i = 0; i < 3; i++) {
			ItemStack it = brewing.getItem(i);
			if (it == null || it.getType() == Material.AIR)
				return i;
		}
		return -1;
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
				.containsKey(XMaterial.matchXMaterial(e.getCurrentItem().getType())))
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

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterial.ENCHANTED_BOOK))
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

		XMaterial xMaterial = XMaterial.matchXMaterial(craftedItem.getType());

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(xMaterial))
			return;

		e.getWhoClicked().closeInventory();
		e.setCancelled(true);
		MessagesUtil.sendMessage(e.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onPreventDurabilityDowngradeShiftMerge(InventoryClickEvent e) {
		if (VersionUtil.v1_17_R1.isServerVersionHigher())
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("prevent-shift-damageable-items-stack"))
			return;

		if (!e.isShiftClick())
			return;

		ItemStack moving = e.getCurrentItem();

		if (moving == null || moving.getType() == Material.AIR)
			return;

		Material type = moving.getType();

		if (type.getMaxDurability() <= 0)
			return;
		if (moving.getAmount() <= 1)
			return;

		if (moving.getDurability() != 0)
			return;

		Inventory playerInv = e.getWhoClicked().getInventory();
		Inventory clickedInv = e.getInventory();

		if (hasDamagedMergeTarget(playerInv, moving)
				|| (clickedInv != null && clickedInv != playerInv && hasDamagedMergeTarget(clickedInv, moving))) {
			e.setCancelled(true);
			MessagesUtil.sendMessage(e.getWhoClicked(),
					MessagesUtil.PREVENT_SHIFT_COMBINING_DAMAGEABLE_ITEMS.toString());
		}
	}

	@SuppressWarnings("deprecation")
	private boolean hasDamagedMergeTarget(Inventory inv, ItemStack moving) {
		Material type = moving.getType();

		for (ItemStack it : inv.getContents()) {
			if (it == null || it.getType() != type)
				continue;

			if (it.getDurability() == 0)
				continue;

			if (!sameMetaIgnoringDurability(moving, it))
				continue;

			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean sameMetaIgnoringDurability(ItemStack a, ItemStack b) {
		ItemMeta am = a.getItemMeta();
		ItemMeta bm = b.getItemMeta();

		if (am == null && bm == null)
			return true;
		if (am == null || bm == null)
			return false;

		if (am.hasDisplayName() != bm.hasDisplayName())
			return false;
		if (am.hasDisplayName() && !am.getDisplayName().equals(bm.getDisplayName()))
			return false;

		if (am.hasLore() != bm.hasLore())
			return false;
		if (am.hasLore() && !am.getLore().equals(bm.getLore()))
			return false;

		return am.getEnchants().equals(bm.getEnchants());
	}
}