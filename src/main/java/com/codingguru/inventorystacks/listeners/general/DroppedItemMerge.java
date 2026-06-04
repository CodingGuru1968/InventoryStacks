package com.codingguru.inventorystacks.listeners.general;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.scheduler.Schedule;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.GroundStackUtil;

public class DroppedItemMerge implements Listener {

	private static final double DEFAULT_MERGE_RADIUS_BLOCKS = 16.0D;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		int mergeCap = getMergeCap();
		double mergeRadius = getMergeRadius();
		Item spawnedItem = e.getEntity();

		if (mergeCap <= 64) {
			debug("spawn-skip cap<=64 item=%s cap=%d", simpleItem(spawnedItem), mergeCap);
			return;
		}

		debug("spawn item=%s cap=%d radius=%.2f", simpleItem(spawnedItem), mergeCap, mergeRadius);

		new Schedule() {
			@Override
			public void run() {
				debug("spawn-deferred item=%s cap=%d radius=%.2f", simpleItem(spawnedItem), mergeCap, mergeRadius);
				mergeIntoNearby(spawnedItem, mergeCap, mergeRadius);
			}
		}.runTaskLater(1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		int mergeCap = getMergeCap();

		if (mergeCap <= 64) {
			debug("event-merge-skip cap<=64 source=%s target=%s", simpleItem(e.getEntity()), simpleItem(e.getTarget()));
			return;
		}

		Item source = e.getEntity();
		Item target = e.getTarget();

		if (source == null || target == null) {
			debug("event-merge-skip null sourceOrTarget");
			return;
		}

		ItemStack sourceStack = source.getItemStack();
		ItemStack targetStack = target.getItemStack();

		if (!isMergeCandidate(targetStack, sourceStack)) {
			debug("event-merge-skip not-similar source=%s target=%s", simpleItem(source), simpleItem(target));
			return;
		}

		if (GroundStackUtil.getTotal(target) >= mergeCap) {
			debug("event-merge-skip target-at-cap target=%s cap=%d", simpleItem(target), mergeCap);
			e.setCancelled(true);
			return;
		}

		e.setCancelled(true);
		boolean merged = mergeSourceIntoTarget(source, target, mergeCap);
		debug("event-merge-result merged=%s source=%s target=%s cap=%d", Boolean.toString(merged), simpleItem(source),
				simpleItem(target), mergeCap);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityPickup(EntityPickupItemEvent e) {
		int mergeCap = getMergeCap();

		if (mergeCap <= 64)
			return;

		if (!(e.getEntity() instanceof Player))
			return;

		Item item = e.getItem();
		int totalAmount = GroundStackUtil.getTotal(item);
		ItemStack stack = item.getItemStack();

		if (stack == null) {
			debug("pickup-player-skip null-stack item=%s", simpleItem(item));
			return;
		}

		if (totalAmount <= stack.getAmount())
			return;

		e.setCancelled(true);
		Player player = (Player) e.getEntity();
		int inserted = addToInventory(player.getInventory(), stack, totalAmount);
		debug("pickup-player player=%s item=%s total=%d inserted=%d remaining=%d", player.getName(), simpleItem(item),
				totalAmount, inserted, Math.max(0, totalAmount - inserted));

		if (inserted <= 0)
			return;

		handlePickupResult(item, totalAmount - inserted);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryPickup(InventoryPickupItemEvent e) {
		int mergeCap = getMergeCap();

		if (mergeCap <= 64)
			return;

		Item item = e.getItem();
		int totalAmount = GroundStackUtil.getTotal(item);
		ItemStack stack = item.getItemStack();

		if (stack == null) {
			debug("pickup-inv-skip null-stack item=%s", simpleItem(item));
			return;
		}

		if (totalAmount <= stack.getAmount())
			return;

		e.setCancelled(true);
		int inserted = addToInventory(e.getInventory(), stack, totalAmount);
		debug("pickup-inv holder=%s item=%s total=%d inserted=%d remaining=%d", e.getInventory().getType().name(),
				simpleItem(item), totalAmount, inserted, Math.max(0, totalAmount - inserted));

		if (inserted <= 0)
			return;

		handlePickupResult(item, totalAmount - inserted);
	}

	private int getMergeCap() {
		int configured = InventoryStacks.getInstance().getConfig().getInt("item-hologram.ground-merge-max-amount", 64);
		return Math.max(1, configured);
	}

	private boolean isDebugEnabled() {
		return InventoryStacks.getInstance().getConfig().getBoolean("item-hologram.ground-merge-debug", false);
	}

	private double getMergeRadius() {
		double configured = InventoryStacks.getInstance().getConfig()
				.getDouble("item-hologram.ground-merge-radius-blocks", DEFAULT_MERGE_RADIUS_BLOCKS);
		return Math.max(0.5D, configured);
	}

	private void mergeIntoNearby(Item target, int mergeCap, double mergeRadius) {
		if (target == null || !target.isValid() || target.isDead()) {
			debug("nearby-merge-skip invalid-target item=%s", simpleItem(target));
			return;
		}

		ItemStack targetStack = target.getItemStack();

		if (targetStack == null || targetStack.getType().isAir()) {
			debug("nearby-merge-skip target-air item=%s", simpleItem(target));
			return;
		}

		int currentAmount = GroundStackUtil.getTotal(target);

		if (currentAmount >= mergeCap) {
			debug("nearby-merge-skip target-at-cap item=%s total=%d cap=%d", simpleItem(target), currentAmount,
					mergeCap);
			return;
		}

		int remaining = mergeCap - currentAmount;
		List<Item> mergedTargets = new ArrayList<>();

		List<Item> nearbyItems = target.getNearbyEntities(mergeRadius, mergeRadius, mergeRadius).stream()
				.filter(entity -> entity instanceof Item).map(entity -> (Item) entity)
				.filter(item -> item.isValid() && !item.isDead())
				.filter(item -> !item.getUniqueId().equals(target.getUniqueId()))
				.filter(item -> isMergeCandidate(targetStack, item.getItemStack()))
				.sorted(Comparator.comparingDouble(item -> item.getLocation().distanceSquared(target.getLocation())))
				.collect(Collectors.toList());

		for (Item source : nearbyItems) {
			if (remaining <= 0)
				break;

			if (mergeSourceIntoTarget(source, target, mergeCap)) {
				mergedTargets.add(source);
				currentAmount = GroundStackUtil.getTotal(target);
				remaining = mergeCap - currentAmount;
				debug("nearby-merge-step target=%s source=%s targetTotal=%d remaining=%d cap=%d", simpleItem(target),
						simpleItem(source), currentAmount, remaining, mergeCap);
			}
		}

		if (mergedTargets.isEmpty()) {
			debug("nearby-merge-none target=%s candidates=%d", simpleItem(target), nearbyItems.size());
			return;
		}

		debug("nearby-merge-complete target=%s mergedCount=%d finalTotal=%d", simpleItem(target), mergedTargets.size(),
				GroundStackUtil.getTotal(target));

		if (InventoryStacks.getInstance().getItemHologramManager() != null) {
			for (Item mergedItem : mergedTargets) {
				if (!mergedItem.isValid() || mergedItem.isDead()) {
					InventoryStacks.getInstance().getItemHologramManager().untrack(mergedItem);
				}
			}
			InventoryStacks.getInstance().getItemHologramManager().track(target);
		}
	}

	private boolean mergeSourceIntoTarget(Item source, Item target, int mergeCap) {
		if (source == null || target == null) {
			debug("merge-source-skip null sourceOrTarget");
			return false;
		}

		if (!source.isValid() || source.isDead() || !target.isValid() || target.isDead()) {
			debug("merge-source-skip invalid source=%s target=%s", simpleItem(source), simpleItem(target));
			return false;
		}

		ItemStack sourceStack = source.getItemStack();
		ItemStack targetStack = target.getItemStack();

		if (!isMergeCandidate(targetStack, sourceStack)) {
			debug("merge-source-skip not-similar source=%s target=%s", simpleItem(source), simpleItem(target));
			return false;
		}

		int targetAmount = GroundStackUtil.getTotal(target);

		if (targetAmount >= mergeCap) {
			debug("merge-source-skip target-at-cap target=%s total=%d cap=%d", simpleItem(target), targetAmount,
					mergeCap);
			return false;
		}

		int sourceAmount = GroundStackUtil.getTotal(source);

		if (sourceAmount <= 0) {
			debug("merge-source-skip source-empty source=%s", simpleItem(source));
			return false;
		}

		int transferAmount = Math.min(mergeCap - targetAmount, sourceAmount);

		if (transferAmount <= 0) {
			debug("merge-source-skip transfer<=0 source=%s target=%s", simpleItem(source), simpleItem(target));
			return false;
		}

		GroundStackUtil.setTotal(target, targetAmount + transferAmount);
		target.setTicksLived(1);

		if (transferAmount >= sourceAmount) {
			GroundStackUtil.clearTotal(source);
			source.remove();
		} else {
			GroundStackUtil.setTotal(source, sourceAmount - transferAmount);
			source.setTicksLived(1);
		}

		debug("merge-source-done source=%s target=%s transfer=%d sourceBefore=%d targetBefore=%d targetAfter=%d cap=%d",
				simpleItem(source), simpleItem(target), transferAmount, sourceAmount, targetAmount,
				GroundStackUtil.getTotal(target), mergeCap);

		if (InventoryStacks.getInstance().getItemHologramManager() != null) {
			if (!source.isValid() || source.isDead()) {
				InventoryStacks.getInstance().getItemHologramManager().untrack(source);
			}
			InventoryStacks.getInstance().getItemHologramManager().track(target);
		}

		return true;
	}

	private int addToInventory(Inventory inventory, ItemStack baseStack, int amount) {
		if (inventory == null || baseStack == null)
			return 0;

		int remaining = amount;
		int inserted = 0;
		int maxChunk = Math.max(1, baseStack.getMaxStackSize());

		while (remaining > 0) {
			int chunk = Math.min(maxChunk, remaining);
			ItemStack toInsert = baseStack.clone();
			toInsert.setAmount(chunk);

			Map<Integer, ItemStack> leftovers = inventory.addItem(toInsert);
			int leftoverAmount = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
			int added = chunk - leftoverAmount;

			if (added <= 0)
				break;

			inserted += added;
			remaining -= added;
		}

		return inserted;
	}

	private void handlePickupResult(Item item, int remainingAmount) {
		if (remainingAmount <= 0) {
			debug("pickup-result remove item=%s", simpleItem(item));
			GroundStackUtil.clearTotal(item);
			item.remove();
			if (InventoryStacks.getInstance().getItemHologramManager() != null) {
				InventoryStacks.getInstance().getItemHologramManager().untrack(item);
			}
			return;
		}

		GroundStackUtil.setTotal(item, remainingAmount);
		item.setTicksLived(1);
		debug("pickup-result keep item=%s remaining=%d", simpleItem(item), remainingAmount);

		if (InventoryStacks.getInstance().getItemHologramManager() != null) {
			InventoryStacks.getInstance().getItemHologramManager().track(item);
		}
	}

	private boolean isMergeCandidate(ItemStack base, ItemStack other) {
		if (base == null)
			return false;

		if (other == null)
			return false;

		if (other.getType().isAir())
			return false;

		ItemStack normalizedBase = normalizeForMerge(base);
		ItemStack normalizedOther = normalizeForMerge(other);
		return normalizedBase.isSimilar(normalizedOther);
	}

	private void debug(String format, Object... args) {
		if (!isDebugEnabled())
			return;

		String message = String.format(format, args);
		ConsoleUtil.info("[ground-merge-debug] " + message);
	}

	private String simpleItem(Item item) {
		if (item == null)
			return "null";

		ItemStack stack = item.getItemStack();
		if (stack == null)
			return item.getUniqueId() + "(null-stack)";

		return item.getUniqueId() + "(" + stack.getType().name() + "x" + stack.getAmount() + "/total="
				+ GroundStackUtil.getTotal(item) + ")";
	}

	private ItemStack normalizeForMerge(ItemStack original) {
		ItemStack normalized = original.clone();

		if (!normalized.hasItemMeta())
			return normalized;

		ItemMeta meta = normalized.getItemMeta();

		if (meta == null)
			return normalized;

		if (!meta.hasMaxStackSize())
			return normalized;

		meta.setMaxStackSize(null);
		normalized.setItemMeta(meta);
		return normalized;
	}

}