package com.codingguru.trailpaths.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.codingguru.trailpaths.handlers.PathHandler;

public class PlayerQuit implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		PathHandler.getInstance().enablePath(e.getPlayer().getUniqueId());
	}

}