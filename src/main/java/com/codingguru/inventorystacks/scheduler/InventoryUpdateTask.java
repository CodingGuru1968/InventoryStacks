package com.codingguru.inventorystacks.scheduler;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;

import com.codingguru.inventorystacks.InventoryStacks;

public class InventoryUpdateTask extends Schedule {

	private final WeakReference<Player> player;

	public InventoryUpdateTask(InventoryStacks plugin, Player player) {
		super(plugin);
		this.player = new WeakReference<Player>(player);
	}

	@Override
	public void run() {
		Player player = this.player.get();
		player.updateInventory();
	}
}