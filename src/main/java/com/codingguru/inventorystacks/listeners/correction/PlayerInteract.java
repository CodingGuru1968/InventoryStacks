package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerInteract implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (VersionUtil.v1_20.isServerVersionHigher() && !ItemHandler.getInstance().useLegacyReflection())
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getClickedBlock().getType() != XMaterialUtil.JUKEBOX.get())
			return;

		if (e.getItem() == null || e.getItem().getType() == XMaterialUtil.AIR.get())
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
		return type == XMaterialUtil.MUSIC_DISC_11.get() || type == XMaterialUtil.MUSIC_DISC_13.get()
				|| type == XMaterialUtil.MUSIC_DISC_5.get() || type == XMaterialUtil.MUSIC_DISC_BLOCKS.get()
				|| type == XMaterialUtil.MUSIC_DISC_CAT.get() || type == XMaterialUtil.MUSIC_DISC_CHIRP.get()
				|| type == XMaterialUtil.MUSIC_DISC_FAR.get() || type == XMaterialUtil.MUSIC_DISC_MALL.get()
				|| type == XMaterialUtil.MUSIC_DISC_MELLOHI.get() || type == XMaterialUtil.MUSIC_DISC_OTHERSIDE.get()
				|| type == XMaterialUtil.MUSIC_DISC_PIGSTEP.get() || type == XMaterialUtil.MUSIC_DISC_RELIC.get()
				|| type == XMaterialUtil.MUSIC_DISC_STAL.get() || type == XMaterialUtil.MUSIC_DISC_STRAD.get()
				|| type == XMaterialUtil.MUSIC_DISC_WAIT.get() || type == XMaterialUtil.MUSIC_DISC_WARD.get();
	}
}