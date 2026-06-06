package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.VersionUtil;

public class BundleFix implements Listener {

	@EventHandler
	public void onBundleClick(InventoryClickEvent e) {
		if (!VersionUtil.v1_21.isServerVersionHigher())
			return;
		
		ItemStack currentItem = e.getCurrentItem();
		ItemStack cursorItem = e.getCursor();

		boolean isCurrentBundle = isBundle(currentItem);
		boolean isCursorBundle = isBundle(cursorItem);

		if (!isCurrentBundle && !isCursorBundle)
			return;

		ItemStack targetItem = isCurrentBundle ? cursorItem : currentItem;

		if (targetItem == null || targetItem.getType().isAir())
			return;

		if (targetItem.getType().getMaxDurability() <= 0 || targetItem.getMaxStackSize() <= 1)
			return;

		e.setCancelled(true);
		MessagesUtil.sendMessage(e.getWhoClicked(), MessagesUtil.BUNDLE_FIX.toString());
	}

	private boolean isBundle(ItemStack item) {
		if (item == null)
			return false;
		Material type = item.getType();
		return type == Material.BUNDLE || type.name().endsWith("_BUNDLE");
	}

}
