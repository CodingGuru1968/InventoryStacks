package com.codingguru.inventorystacks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerItemDamage implements Listener {
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerItemDamage(PlayerItemDamageEvent e) {
		int originalAmount = e.getItem().getAmount();

		if (originalAmount <= 1)
			return;

		XMaterialUtil xMat = XMaterialUtil.matchXMaterial(e.getItem().getType());

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(xMat))
			return;
		
		ItemStack clone = e.getItem().clone();
		clone.setAmount(originalAmount - 1);

		Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), () -> {
			e.getItem().setAmount(1);
			ItemUtil.addItem(e.getPlayer(), clone);
			e.getPlayer().updateInventory();
		}, 2L);
	}
}