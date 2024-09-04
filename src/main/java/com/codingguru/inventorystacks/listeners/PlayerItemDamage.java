package com.codingguru.inventorystacks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;

public class PlayerItemDamage implements Listener {

	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent e) {
		if (!VersionUtil.v1_21_R1.isServerVersionHigher())
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(e.getItem().getType()))
			return;

		int originalAmount = e.getItem().getAmount();

		if (originalAmount <= 1)
			return;

		ItemStack clone = e.getItem().clone();
		clone.setAmount(originalAmount - 1);

		e.getItem().setAmount(1);

		Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), () -> {
			ItemUtil.addItem(e.getPlayer(), clone);
		}, 2L);
	}

}