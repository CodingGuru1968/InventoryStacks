package com.codingguru.inventorystacks.scheduler;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.ItemUtil;

public class DamageItemTask extends Schedule {

	private final WeakReference<Player> player;
	private final ItemStack item;
	private final ItemStack clone;

	public DamageItemTask(Player player, ItemStack item, ItemStack clone) {
		this.player = new WeakReference<Player>(player);
		this.item = item;
		this.clone = clone;
	}

	@Override
	public void run() {
		Player player = this.player.get();
		item.setAmount(1);
		ItemUtil.addItem(player, clone);
		player.updateInventory();
	}
}