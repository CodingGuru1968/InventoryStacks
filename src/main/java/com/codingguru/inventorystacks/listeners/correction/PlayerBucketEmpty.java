package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandTask;
import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;

public class PlayerBucketEmpty implements Listener {

	private final long itemChangeDelay;

	public PlayerBucketEmpty(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
		Player player = e.getPlayer();
		ItemStack holding;
		boolean legacy = !VersionUtil.v1_9_R1.isServerVersionHigher();

		if (legacy) {
			holding = player.getItemInHand();
		} else {
			EquipmentSlot hand = e.getHand();
			holding = (hand == EquipmentSlot.HAND) ? player.getInventory().getItemInMainHand()
					: player.getInventory().getItemInOffHand();
		}

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		ItemStack clone = holding.clone();
		clone.setAmount(amount - 1);

		if (!VersionUtil.v1_21.isServerVersionHigher()) {
			ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), clone,
					new ItemStack(XMaterial.BUCKET.get()), XMaterial.BUCKET.get());
			changeItemTask.runTaskLater(itemChangeDelay);
		} else {
			ChangeItemInHandTask changeItemTask = new ChangeItemInHandTask(e.getPlayer(), clone,
					XMaterial.BUCKET.get());
			changeItemTask.runTaskLater(itemChangeDelay);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onCauldronBucketUseLegacy(PlayerInteractEvent e) {
		if (VersionUtil.v1_9_R1.isServerVersionHigher())
			return;
		
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (e.getClickedBlock() == null)
			return;

		if (e.getClickedBlock().getType() != Material.CAULDRON)
			return;

		Player player = e.getPlayer();
		ItemStack holding = player.getItemInHand();
		if (holding == null || holding.getType() == Material.AIR)
			return;

		if (holding.getType() != Material.WATER_BUCKET)
			return;

		int amount = holding.getAmount();
		if (amount <= 1)
			return;

		ItemStack remaining = holding.clone();
		remaining.setAmount(amount - 1);

		ChangeItemInHandWithItemTask task = new ChangeItemInHandWithItemTask(player, remaining,
				new ItemStack(Material.BUCKET), XMaterial.BUCKET.get());
		task.runTaskLater(itemChangeDelay);
	}

}