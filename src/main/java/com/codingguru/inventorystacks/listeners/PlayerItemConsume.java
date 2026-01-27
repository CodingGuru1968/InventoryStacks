package com.codingguru.inventorystacks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;

public class PlayerItemConsume implements Listener {

	private final long itemChangeDelay;

	public PlayerItemConsume(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (VersionUtil.v1_21.isServerVersionHigher())
			return;

		if (e.getItem() == null)
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (e.getItem().getType() == XMaterial.MILK_BUCKET.get()) {
			ItemUtil.addItem(e.getPlayer(), new ItemStack(XMaterial.BUCKET.get()));
			return;
		}

		if (e.getItem().getType() != XMaterial.RABBIT_STEW.get()
				&& e.getItem().getType() != XMaterial.SUSPICIOUS_STEW.get()
				&& e.getItem().getType() != XMaterial.MUSHROOM_STEW.get()
				&& e.getItem().getType() != XMaterial.BEETROOT_SOUP.get())
			return;

		ItemStack clone = e.getItem().clone();
		clone.setAmount(e.getItem().getAmount() - 1);

		ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), clone,
				new ItemStack(XMaterial.BOWL.get()), XMaterial.BOWL.get());
		changeItemTask.runTaskLater(itemChangeDelay);
	}

}