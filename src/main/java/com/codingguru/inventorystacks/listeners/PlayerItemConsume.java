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

public class PlayerItemConsume implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (e.getItem() == null)
			return;

		if (e.getItem().getType() != Material.MUSHROOM_STEW)
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(Material.MUSHROOM_STEW))
			return;

		Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), () -> {
			ItemStack clone = e.getItem().clone();
			clone.setAmount(e.getItem().getAmount() - 1);
			clone.setType(Material.MUSHROOM_STEW);
			setMushroomStewType(e.getPlayer(), clone, Material.BOWL);
			ItemUtil.addItem(e.getPlayer(), new ItemStack(Material.BOWL));
		}, 2L);
	}

	private void setMushroomStewType(Player player, ItemStack item, Material mat) {
		if (player.getInventory().getItemInMainHand().getType() == mat) {
			player.getInventory().setItemInMainHand(item);
		} else if (player.getInventory().getItemInOffHand().getType() == mat) {
			player.getInventory().setItemInOffHand(item);
		}
	}
}