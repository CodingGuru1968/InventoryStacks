package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerInteract implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (VersionUtil.v1_20_R4.isServerVersionHigher())
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getClickedBlock().getType() != XMaterialUtil.JUKEBOX.parseMaterial())
			return;

		if (e.getItem() == null || e.getItem().getType() == XMaterialUtil.AIR.parseMaterial())
			return;

		ItemStack holding = e.getItem();

		if (!isMusicDisc(holding.getType()))
			return;

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		Jukebox jukebox = (Jukebox) e.getClickedBlock().getState();

		if (jukebox.hasRecord())
			return;

		e.setCancelled(true);
		holding.setAmount(holding.getAmount() - 1);
		ItemStack clone = holding.clone();
		clone.setAmount(1);
		jukebox.setRecord(clone);
		jukebox.update();
	}

	private boolean isMusicDisc(Material type) {
		return type == XMaterialUtil.MUSIC_DISC_11.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_13.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_5.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_BLOCKS.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_CAT.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_CHIRP.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_FAR.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_MALL.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_MELLOHI.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_OTHERSIDE.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_PIGSTEP.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_RELIC.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_STAL.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_STRAD.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_WAIT.parseMaterial()
				|| type == XMaterialUtil.MUSIC_DISC_WARD.parseMaterial();
	}
}