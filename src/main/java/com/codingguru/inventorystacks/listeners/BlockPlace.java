package com.codingguru.inventorystacks.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;

public class BlockPlace implements Listener {

	private final long itemChangeDelay;

	public BlockPlace(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!VersionUtil.v1_17_R1.isServerVersionHigher())
			return;

		Player player = e.getPlayer();
		ItemStack holding;

		if (e.getHand() == EquipmentSlot.HAND) {
			holding = player.getInventory().getItemInMainHand();
		} else {
			holding = player.getInventory().getItemInOffHand();
		}

		if (holding.getType() != XMaterial.POWDER_SNOW_BUCKET.get())
			return;

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		ItemStack clone = holding.clone();
		clone.setAmount(amount - 1);

		ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), clone,
				new ItemStack(XMaterial.BUCKET.get()), XMaterial.BUCKET.get());
		changeItemTask.runTaskLater(itemChangeDelay);
	}
}