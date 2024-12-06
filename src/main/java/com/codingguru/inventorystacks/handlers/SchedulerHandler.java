package com.codingguru.inventorystacks.handlers;

import org.bukkit.Bukkit;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ServerTypeUtil;

public class SchedulerHandler {

	private static final SchedulerHandler INSTANCE = new SchedulerHandler();
	private final boolean USING_FOLIA = ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA;

	public void runTask(Runnable runnable) {
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().execute(InventoryStacks.getInstance(), runnable);
		} else {
			Bukkit.getScheduler().runTask(InventoryStacks.getInstance(), runnable);
		}
	}

	public void runTaskLater(Runnable runnable, long delay) {
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().runDelayed(InventoryStacks.getInstance(), t -> runnable.run(), delay);
		} else {
			Bukkit.getScheduler().runTaskLater(InventoryStacks.getInstance(), runnable, delay);
		}
	}

	public static SchedulerHandler getInstance() {
		return INSTANCE;
	}
}