package com.codingguru.inventorystacks.listeners;

import java.util.Map;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;

public class BlockDispense implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onDispenserWaterBottle(BlockDispenseEvent e) {
		// Only 1.19+ (mud exists)
		if (!VersionUtil.v1_19_R1.isServerVersionHigher())
			return;

		ItemStack stack = e.getItem();
		if (!isWaterBottle(stack))
			return;

		Block dispenserBlock = e.getBlock();
		if (dispenserBlock == null)
			return;

		BlockFace facing = getDispenserFacing(dispenserBlock);
		if (facing == null)
			return;

		Block target = dispenserBlock.getRelative(facing);

		// Only mud conversion logic
		if (!canBecomeMud(target))
			return;

		e.setCancelled(true);

		Material mud = XMaterial.matchXMaterial("MUD").map(XMaterial::get).orElse(null);
		if (mud == null)
			return;

		target.setType(mud, true);

		Dispenser dispenser = (Dispenser) dispenserBlock.getState();
		Inventory inv = dispenser.getInventory();

		int slot = findMatchingSlot(inv, stack);
		if (slot == -1)
			return;

		ItemStack inSlot = inv.getItem(slot);
		if (inSlot == null)
			return;

		inSlot.setAmount(inSlot.getAmount() - 1);
		inv.setItem(slot, inSlot.getAmount() > 0 ? inSlot : null);

		Material glassBottle = XMaterial.matchXMaterial("GLASS_BOTTLE").map(XMaterial::get).orElse(null);
		if (glassBottle != null) {
			Map<Integer, ItemStack> overflow = inv.addItem(new ItemStack(glassBottle, 1));
			overflow.values().forEach(item -> dispenserBlock.getWorld()
					.dropItemNaturally(dispenserBlock.getLocation().add(0.5, 0.5, 0.5), item));
		}
	}

	@SuppressWarnings({ "removal", "deprecation" })
	private boolean isWaterBottle(ItemStack item) {
		if (item == null)
			return false;

		if (item.getType() != XMaterial.matchXMaterial("POTION").map(XMaterial::get).orElse(null))
			return false;

		if (!(item.getItemMeta() instanceof PotionMeta))
			return false;

		PotionMeta meta = (PotionMeta) item.getItemMeta();

		if (meta.hasCustomEffects())
			return false;

		return meta.getBasePotionData().getType() == PotionType.WATER;
	}

	private boolean canBecomeMud(Block block) {
		Material type = block.getType();
		return type == Material.DIRT || type == Material.COARSE_DIRT || type == Material.ROOTED_DIRT;
	}

	private int findMatchingSlot(Inventory inv, ItemStack needle) {
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack s = contents[i];

			if (s == null)
				continue;

			if (s.getType() != needle.getType())
				continue;

			if (!Objects.equals(s.getItemMeta(), needle.getItemMeta()))
				continue;

			return i;
		}
		return -1;
	}

	private BlockFace getDispenserFacing(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Directional) {
			return ((Directional) data).getFacing();
		}
		return null;
	}
}