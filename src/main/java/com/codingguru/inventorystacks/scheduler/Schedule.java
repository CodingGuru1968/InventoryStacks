package com.codingguru.inventorystacks.scheduler;

import org.bukkit.Bukkit;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ServerTypeUtil;

public abstract class Schedule implements Runnable {

	protected final InventoryStacks plugin;
	private final boolean USING_FOLIA;

	public Schedule(InventoryStacks plugin) {
		this.plugin = plugin;
		this.USING_FOLIA = ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA;
	}

	public void runTask() {
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().execute(plugin, this);
		} else {
			Bukkit.getScheduler().runTask(plugin, this);
		}
	}

	public void runTaskLater(long delay) {	
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> this.run(), delay);
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, this, delay);
		}
	}

}