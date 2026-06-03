package com.codingguru.inventorystacks.hooks;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public final class WorldGuardHook {

	private static boolean isEnabled = false;

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static boolean setupWorldGuard() {
		isEnabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null
				&& InventoryStacks.getInstance().getConfig().getBoolean("worldguard.enabled", false);

		if (!VersionUtil.v1_20.isServerVersionHigher()) {
			ConsoleUtil
					.warning("WorldGuard support was found and enabled but cannot be used on old server version API.");
			return false;
		}

		return isEnabled;
	}

	public static boolean isInTargetRegion(Location loc) {
		if (!isEnabled())
			return true;

		if (loc == null)
			return false;

		List<String> targetRegions = InventoryStacks.getInstance().getConfig()
				.getStringList("worldguard.enabled-regions");

		if (targetRegions == null || targetRegions.isEmpty())
			return true;

		try {
			return checkWorldGuard(loc, targetRegions);
		} catch (Exception | NoClassDefFoundError e) {
			return true;
		}
	}

	private static boolean checkWorldGuard(Location loc, List<String> targetRegions) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));

		if (regions == null)
			return true;

		return regions.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).getRegions().stream()
				.anyMatch(region -> targetRegions.stream().anyMatch(target -> target.equalsIgnoreCase(region.getId())));
	}

	public static Location getLocationFromInventory(Inventory inv) {
		if (inv == null)
			return null;

		try {
			if (inv.getLocation() != null) {
				return inv.getLocation();
			}
		} catch (Exception ignored) {
		}

		if (inv.getHolder() instanceof org.bukkit.block.BlockState) {
			return ((org.bukkit.block.BlockState) inv.getHolder()).getLocation();
		}

		if (inv.getHolder() instanceof org.bukkit.entity.Entity) {
			return ((org.bukkit.entity.Entity) inv.getHolder()).getLocation();
		}

		return null;
	}
}