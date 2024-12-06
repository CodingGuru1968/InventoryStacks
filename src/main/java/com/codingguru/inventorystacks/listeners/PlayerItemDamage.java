package com.codingguru.inventorystacks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.DamageItemTask;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerItemDamage implements Listener {

	private final long itemChangeDelay;
	
	public PlayerItemDamage(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerItemDamage(PlayerItemDamageEvent e) {
		int originalAmount = e.getItem().getAmount();

		if (originalAmount <= 1)
			return;

		XMaterialUtil xMat = XMaterialUtil.matchXMaterial(e.getItem().getType());

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(xMat))
			return;

		ItemStack clone = e.getItem().clone();
		clone.setAmount(originalAmount - 1);

		DamageItemTask damageItemTask = new DamageItemTask(e.getPlayer(), e.getItem(), clone);
		damageItemTask.runTaskLater(itemChangeDelay);
	}
}