package com.codingguru.trailpaths.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.codingguru.trailpaths.TrailPaths;
import com.codingguru.trailpaths.handlers.PathHandler;
import com.codingguru.trailpaths.scheduler.ReplaceOldMaterialThread;

public class PlayerMove implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;

		if (PathHandler.getInstance().isPathDisabled(e.getPlayer().getUniqueId()))
			return;

		Block block = e.getPlayer().getLocation().subtract(0, 1, 0).getBlock();
		Material steppedOnBlockType = block.getType();

		if (PathHandler.getInstance().contains(steppedOnBlockType)) {
			block.setType(PathHandler.getInstance().getChangedMaterial(steppedOnBlockType));

			if (TrailPaths.getInstance().getConfig().getInt("path-timer") == -1)
				return;

			ReplaceOldMaterialThread thread = new ReplaceOldMaterialThread(block.getLocation(), steppedOnBlockType);
			thread.runTaskLater(TrailPaths.getInstance().getConfig().getInt("path-timer") * 20);
		}
	}
}