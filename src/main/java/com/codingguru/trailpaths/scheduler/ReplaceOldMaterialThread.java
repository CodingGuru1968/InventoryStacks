package com.codingguru.trailpaths.scheduler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ReplaceOldMaterialThread extends Schedule {

	private final Location location;
	private final Material oldMaterial;

	public ReplaceOldMaterialThread(Location location, Material oldMaterial) {
		this.location = location;
		this.oldMaterial = oldMaterial;
	}

	@Override
	public void run() {
		Block block = location.getWorld().getBlockAt(location);
		block.setType(oldMaterial);
	}
}