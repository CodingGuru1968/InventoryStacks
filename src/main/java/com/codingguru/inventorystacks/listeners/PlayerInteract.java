package com.codingguru.inventorystacks.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;

public class PlayerInteract implements Listener {

	private List<Material> removableMaterials = new ArrayList<>(Arrays
			.asList(new Material[] { Material.TALL_GRASS, Material.DANDELION, Material.RED_MUSHROOM, Material.ROSE_BUSH,
					Material.SUNFLOWER, Material.BROWN_MUSHROOM, Material.NETHER_WART, Material.WHEAT, Material.CARROT,
					Material.POTATO, Material.BEETROOT, Material.WATER, Material.LAVA, Material.AIR }));

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;

		if (e.getItem().getType() != Material.WATER_BUCKET && e.getItem().getType() != Material.LAVA_BUCKET)
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(Material.LAVA_BUCKET)
				&& !ItemHandler.getInstance().getCachedMaterialSizes().containsKey(Material.WATER_BUCKET))
			return;

		e.setCancelled(true);

		Block block;

		if (this.removableMaterials.contains(e.getClickedBlock().getType())) {
			block = e.getClickedBlock();
		} else {
			block = e.getClickedBlock().getRelative(e.getBlockFace());
		}

		if (!this.removableMaterials.contains(block.getType()))
			return;

		if (e.getItem().getType() == Material.LAVA_BUCKET) {
			block.setType(Material.LAVA);
		} else {
			block.setType(Material.WATER);
		}

		e.getItem().setAmount(e.getItem().getAmount() - 1);
		ItemUtil.addItem(e.getPlayer(), new ItemStack(Material.BUCKET));
	}
}