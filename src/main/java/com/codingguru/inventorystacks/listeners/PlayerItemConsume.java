package com.codingguru.inventorystacks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerItemConsume implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (e.getItem() == null)
			return;

		if (e.getItem().getType() != XMaterialUtil.MUSHROOM_STEW.parseMaterial())
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterialUtil.MUSHROOM_STEW))
			return;

		Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), () -> {
			ItemStack clone = e.getItem().clone();
			clone.setAmount(e.getItem().getAmount() - 1);
			clone.setType(XMaterialUtil.MUSHROOM_STEW.parseMaterial());
			setMushroomStewType(e.getPlayer(), clone, XMaterialUtil.BOWL.parseMaterial());
			ItemUtil.addItem(e.getPlayer(), new ItemStack(XMaterialUtil.BOWL.parseMaterial()));
		}, 2L);
	}

	@SuppressWarnings("deprecation")
	private void setMushroomStewType(Player player, ItemStack item, Material mat) {
		if (!VersionUtil.v1_9_R1.isServerVersionHigher()) {
			player.getInventory().setItemInHand(item);
			return;
		}
		
		if (player.getInventory().getItemInMainHand().getType() == mat) {
			player.getInventory().setItemInMainHand(item);
		} else if (player.getInventory().getItemInOffHand().getType() == mat) {
			player.getInventory().setItemInOffHand(item);
		}
	}
}