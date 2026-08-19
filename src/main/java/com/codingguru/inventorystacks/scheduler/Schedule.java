package com.codingguru.inventorystacks.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ServerTypeUtil;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public abstract class Schedule implements Runnable {

	protected final InventoryStacks plugin;
	private final boolean USING_FOLIA;

	private BukkitTask bukkitTask;
	private ScheduledTask foliaTask;

	public Schedule(InventoryStacks plugin) {
		this.plugin = plugin;
		this.USING_FOLIA = ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA;
	}

	public void cancel() {
		if (USING_FOLIA) {
			if (foliaTask != null) {
				foliaTask.cancel();
				foliaTask = null;
			}
		} else {
			if (bukkitTask != null) {
				bukkitTask.cancel();
				bukkitTask = null;
			}
		}
	}

	public void runTaskTimer(long delay, long period) {
		if (USING_FOLIA) {
			foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
				this.foliaTask = t;
				this.run();
			}, Math.max(1L, delay), period);
		} else {
			bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
		}
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