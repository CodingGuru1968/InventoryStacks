package com.codingguru.trailpaths.scheduler;

import org.bukkit.Bukkit;

import com.codingguru.trailpaths.TrailPaths;
import com.codingguru.trailpaths.utils.ServerTypeUtil;

public abstract class Schedule implements Runnable {

	private final boolean USING_FOLIA = TrailPaths.getInstance().getServerType() == ServerTypeUtil.FOLIA;

	public void runTask() {
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().execute(TrailPaths.getInstance(), this);
		} else {
			Bukkit.getScheduler().runTask(TrailPaths.getInstance(), this);
		}
	}

	public void runTaskLater(long delay) {
		if (USING_FOLIA) {
			Bukkit.getGlobalRegionScheduler().runDelayed(TrailPaths.getInstance(), t -> this.run(), delay);
		} else {
			Bukkit.getScheduler().runTaskLater(TrailPaths.getInstance(), this, delay);
		}
	}
	
}