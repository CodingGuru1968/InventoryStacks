package com.codingguru.inventorystacks.listeners;

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
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import com.google.common.collect.Lists;

public class PlayerInteract implements Listener {

	private final List<XMaterialUtil> removableMaterials;

	public PlayerInteract() {
		removableMaterials = Lists.newArrayList();

		// add basic materials
		removableMaterials.add(XMaterialUtil.WATER);
		removableMaterials.add(XMaterialUtil.LAVA);
		removableMaterials.add(XMaterialUtil.AIR);
		removableMaterials.add(XMaterialUtil.NETHER_WART);
		removableMaterials.add(XMaterialUtil.WHEAT);
		removableMaterials.add(XMaterialUtil.POTATO);
		removableMaterials.add(XMaterialUtil.CARROT);
		removableMaterials.add(XMaterialUtil.RED_MUSHROOM);
		removableMaterials.add(XMaterialUtil.BROWN_MUSHROOM);
		removableMaterials.add(XMaterialUtil.DANDELION);

		if (VersionUtil.v1_7_R2.isServerVersionHigher()) {
			removableMaterials.add(XMaterialUtil.SUNFLOWER);
		}

		if (VersionUtil.v1_8_R1.isServerVersionHigher()) {
			removableMaterials.add(XMaterialUtil.TALL_GRASS);
			removableMaterials.add(XMaterialUtil.ROSE_BUSH);
		}

		if (VersionUtil.v1_9_R1.isServerVersionHigher()) {
			removableMaterials.add(XMaterialUtil.BEETROOT);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;

		if (e.getItem().getType() != XMaterialUtil.LAVA_BUCKET.parseMaterial()
				&& e.getItem().getType() != XMaterialUtil.WATER_BUCKET.parseMaterial())
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getItem().getAmount() <= 1)
			return;

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterialUtil.LAVA_BUCKET)
				&& !ItemHandler.getInstance().getCachedMaterialSizes().containsKey(XMaterialUtil.WATER_BUCKET))
			return;

		e.setCancelled(true);

		XMaterialUtil clickedType = XMaterialUtil.matchXMaterial(e.getClickedBlock().getType());

		Block block;

		if (this.removableMaterials.contains(clickedType)) {
			block = e.getClickedBlock();
		} else {
			block = e.getClickedBlock().getRelative(e.getBlockFace());
		}

		if (!this.removableMaterials.contains(XMaterialUtil.matchXMaterial(block.getType())))
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