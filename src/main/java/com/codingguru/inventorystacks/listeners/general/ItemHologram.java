package com.codingguru.inventorystacks.listeners.general;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import com.codingguru.inventorystacks.handlers.ManagerHandler;
import com.codingguru.inventorystacks.managers.ItemHologramManager;

public class ItemHologram implements Listener {

	private final ItemHologramManager itemHologramManager;

	public ItemHologram() {
		itemHologramManager = ManagerHandler.getInstance().get(ItemHologramManager.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		itemHologramManager.track(e.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent e) {
		itemHologramManager.untrack(e.getItem());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemDespawn(ItemDespawnEvent e) {
		itemHologramManager.untrack(e.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		itemHologramManager.merge(e.getEntity(), e.getTarget());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntitiesLoad(EntitiesLoadEvent e) {
		e.getEntities().forEach(entity -> {
			if (entity instanceof Item) {
				itemHologramManager.track((Item) entity);
			}
		});
	}
}