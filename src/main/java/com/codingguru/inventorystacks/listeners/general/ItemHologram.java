package com.codingguru.inventorystacks.listeners.general;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.managers.ItemHologramManager;

public class ItemHologram implements Listener {

	private final ItemHologramManager manager;

	public ItemHologram() {
		this.manager = InventoryStacks.getInstance().getItemHologramManager();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		manager.track(e.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent e) {
		manager.untrack(e.getItem());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemDespawn(ItemDespawnEvent e) {
		manager.untrack(e.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		manager.merge(e.getEntity(), e.getTarget());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntitiesLoad(EntitiesLoadEvent e) {
		e.getEntities().forEach(entity -> {
			if (entity instanceof org.bukkit.entity.Item) {
				manager.track((org.bukkit.entity.Item) entity);
			}
		});
	}
}