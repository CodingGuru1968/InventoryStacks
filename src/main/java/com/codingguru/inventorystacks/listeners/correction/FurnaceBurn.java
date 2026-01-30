package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.cryptomorin.xseries.XMaterial;

public class FurnaceBurn implements Listener {

	@EventHandler
	public void onFurnaceBurn(FurnaceBurnEvent e) {
		if (e.getFuel() == null)
			return;

		if (e.getFuel().getType() != XMaterial.LAVA_BUCKET.get())
			return;

		if (e.getFuel().getAmount() <= 1)
			return;

		if (!ItemHandler.getInstance().hasEditedStackSize(XMaterial.LAVA_BUCKET))
			return;

		e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(Material.BUCKET));
	}
}