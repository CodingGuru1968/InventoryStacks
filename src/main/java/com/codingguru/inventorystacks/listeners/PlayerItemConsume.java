package com.codingguru.inventorystacks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerItemConsume implements Listener {

	private final long itemChangeDelay;
	
	public PlayerItemConsume(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (VersionUtil.v1_21_R1.isServerVersionHigher())
			return;

		if (e.getItem() == null)
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (e.getItem().getType() == XMaterialUtil.MILK_BUCKET.parseMaterial()) {
			ItemUtil.addItem(e.getPlayer(), new ItemStack(XMaterialUtil.BUCKET.parseMaterial()));
			return;
		}

		if (e.getItem().getType() != XMaterialUtil.RABBIT_STEW.parseMaterial()
				&& e.getItem().getType() != XMaterialUtil.SUSPICIOUS_STEW.parseMaterial()
				&& e.getItem().getType() != XMaterialUtil.MUSHROOM_STEW.parseMaterial()
				&& e.getItem().getType() != XMaterialUtil.BEETROOT_SOUP.parseMaterial())
			return;

		ItemStack clone = e.getItem().clone();
		clone.setAmount(e.getItem().getAmount() - 1);

		ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), clone,
				new ItemStack(XMaterialUtil.BOWL.parseMaterial()), XMaterialUtil.BOWL.parseMaterial());
		changeItemTask.runTaskLater(itemChangeDelay);
	}

}