package com.codingguru.inventorystacks.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandTask;
import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerBucketEmpty implements Listener {

	private final long itemChangeDelay;
	
	public PlayerBucketEmpty(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
		Player player = e.getPlayer();
		ItemStack holding;

		if (e.getHand() == EquipmentSlot.HAND) {
			holding = player.getInventory().getItemInMainHand();
		} else {
			holding = player.getInventory().getItemInOffHand();
		}

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		ItemStack clone = holding.clone();
		clone.setAmount(amount - 1);

		if (!VersionUtil.v1_21_R1.isServerVersionHigher()) {
			ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), clone,
					new ItemStack(XMaterialUtil.BUCKET.parseMaterial()), XMaterialUtil.BUCKET.parseMaterial());
			changeItemTask.runTaskLater(itemChangeDelay);
		} else {
			ChangeItemInHandTask changeItemTask = new ChangeItemInHandTask(e.getPlayer(), clone,
					XMaterialUtil.BUCKET.parseMaterial());
			changeItemTask.runTaskLater(itemChangeDelay);
		}
	}
}