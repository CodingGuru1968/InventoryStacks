package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.scheduler.Schedule;
import com.codingguru.inventorystacks.util.LangDefaults;
import com.codingguru.inventorystacks.util.MessageBuilder;

public class SmithingTableFix implements Listener {

	private static final int INPUT_SLOTS = 3;
	private final InventoryStacks plugin;

	public SmithingTableFix(InventoryStacks plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		InventoryView view = e.getView();

		if (view.getTopInventory().getType() != InventoryType.SMITHING)
			return;

		int rawSlot = e.getRawSlot();

		if (isSmithingInputSlot(rawSlot)) {
			ItemStack movingItem = getMovingItem(e);

			if (movingItem == null || movingItem.getType() == Material.AIR)
				return;

			if (isOverVanillaStackSize(movingItem)) {
				sendDisallowMessage64(e.getWhoClicked());
				e.setCancelled(true);
				return;
			}

			if (isInvalidStackedDamageable(movingItem)) {
				sendDisallowMessageDamageable(e.getWhoClicked());
				e.setCancelled(true);
				return;
			}

			if (hasCustomStackSize(movingItem) && isDamageable(movingItem)) {
				removeMaxStackSize(movingItem);
				return;
			}

			if (hasCustomStackSize(movingItem))
				scheduleRemoveMaxStackSize(view.getTopInventory(), rawSlot, e.getView().getPlayer());

			return;
		}

		if (e.getClickedInventory() != view.getBottomInventory()
				|| e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)
			return;

		ItemStack movingItem = e.getCurrentItem();

		if (movingItem == null || movingItem.getType() == Material.AIR)
			return;

		if (isOverVanillaStackSize(movingItem)) {
			sendDisallowMessage64(e.getWhoClicked());
			e.setCancelled(true);
			return;
		}

		if (isInvalidStackedDamageable(movingItem)) {
			sendDisallowMessageDamageable(e.getWhoClicked());
			e.setCancelled(true);
			return;
		}

		if (hasCustomStackSize(movingItem) && isDamageable(movingItem)) {
			removeMaxStackSize(movingItem);
			return;
		}

		if (!hasCustomStackSize(movingItem))
			return;

		ItemStack[] before = new ItemStack[INPUT_SLOTS];

		for (int slot = 0; slot < INPUT_SLOTS; slot++) {
			ItemStack item = view.getTopInventory().getItem(slot);
			before[slot] = item == null ? null : item.clone();
		}

		scheduleRemoveChangedSlots(view.getTopInventory(), before, e.getView().getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent e) {
		InventoryView view = e.getView();

		if (view.getTopInventory().getType() != InventoryType.SMITHING)
			return;

		ItemStack cursor = e.getOldCursor();

		if (cursor == null || cursor.getType() == Material.AIR)
			return;

		boolean movingIntoSmithing = false;

		for (Integer rawSlot : e.getRawSlots()) {
			if (isSmithingInputSlot(rawSlot)) {
				movingIntoSmithing = true;
				break;
			}
		}

		if (!movingIntoSmithing)
			return;

		if (isOverVanillaStackSize(cursor)) {
			sendDisallowMessage64(e.getWhoClicked());
			e.setCancelled(true);
			return;
		}

		if (isInvalidStackedDamageable(cursor)) {
			sendDisallowMessageDamageable(e.getWhoClicked());
			e.setCancelled(true);
			return;
		}

		if (hasCustomStackSize(cursor) && isDamageable(cursor)) {
			for (ItemStack item : e.getNewItems().values())
				removeMaxStackSize(item);

			ItemStack newCursor = e.getCursor();

			if (newCursor != null)
				removeMaxStackSize(newCursor);

			return;
		}

		if (!hasCustomStackSize(cursor))
			return;

		scheduleRemoveMaxStackSize(view.getTopInventory(), e.getNewItems().keySet(), e.getView().getPlayer());
	}

	@SuppressWarnings("removal")
	private ItemStack getMovingItem(InventoryClickEvent event) {
		switch (event.getAction()) {
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
		case SWAP_WITH_CURSOR:
			return event.getCursor();

		case HOTBAR_SWAP:
		case HOTBAR_MOVE_AND_READD:
			int button = event.getHotbarButton();

			if (button >= 0)
				return event.getWhoClicked().getInventory().getItem(button);

			return event.getWhoClicked().getInventory().getItemInOffHand();

		default:
			return null;
		}
	}

	private boolean isSmithingInputSlot(int rawSlot) {
		return rawSlot >= 0 && rawSlot < INPUT_SLOTS;
	}

	private boolean isOverVanillaStackSize(ItemStack item) {
		return item != null && item.getAmount() > 64;
	}

	private boolean hasCustomStackSize(ItemStack item) {
		if (item == null || !item.hasItemMeta())
			return false;

		ItemMeta meta = item.getItemMeta();
		return meta.hasMaxStackSize() && meta.getMaxStackSize() > 1;
	}

	private boolean isInvalidStackedDamageable(ItemStack item) {
		return hasCustomStackSize(item) && isDamageable(item) && item.getAmount() > 1;
	}

	private boolean isDamageable(ItemStack item) {
		return item != null && item.getType().getMaxDurability() > 0;
	}

	private void sendDisallowMessageDamageable(HumanEntity entity) {
		new MessageBuilder.Builder("disallow-smithing-stack-damageable",
				LangDefaults.DISALLOW_SMITHING_STACK_DAMAGEABLE).send(entity);
	}

	private void sendDisallowMessage64(HumanEntity entity) {
		new MessageBuilder.Builder("disallow-smithing-stack-64", LangDefaults.DISALLOW_SMITHING_STACK_64).send(entity);
	}

	private void scheduleRemoveMaxStackSize(Inventory inventory, int rawSlot, HumanEntity viewer) {
		new Schedule(plugin) {
			@Override
			public void run() {
				ItemStack item = inventory.getItem(rawSlot);

				if (item == null || item.getType() == Material.AIR)
					return;

				if (!hasCustomStackSize(item) || isDamageable(item))
					return;

				removeMaxStackSize(item);
				inventory.setItem(rawSlot, item);
				updateInventory(viewer);
			}
		}.runTask();
	}

	private void scheduleRemoveMaxStackSize(Inventory inventory, Iterable<Integer> rawSlots, HumanEntity viewer) {
		new Schedule(plugin) {
			@Override
			public void run() {
				boolean changed = false;

				for (Integer rawSlot : rawSlots) {
					if (!isSmithingInputSlot(rawSlot))
						continue;

					ItemStack item = inventory.getItem(rawSlot);

					if (item == null || item.getType() == Material.AIR)
						continue;

					if (!hasCustomStackSize(item) || isDamageable(item))
						continue;

					removeMaxStackSize(item);
					inventory.setItem(rawSlot, item);
					changed = true;
				}

				if (changed)
					updateInventory(viewer);
			}
		}.runTask();
	}

	private void scheduleRemoveChangedSlots(Inventory inventory, ItemStack[] before, HumanEntity viewer) {
		new Schedule(plugin) {
			@Override
			public void run() {
				boolean changed = false;

				for (int slot = 0; slot < INPUT_SLOTS; slot++) {
					ItemStack after = inventory.getItem(slot);
					ItemStack previous = before[slot];

					if (!hasChanged(previous, after))
						continue;

					if (after == null || after.getType() == Material.AIR)
						continue;

					if (!hasCustomStackSize(after) || isDamageable(after))
						continue;

					removeMaxStackSize(after);
					inventory.setItem(slot, after);
					changed = true;
				}

				if (changed)
					updateInventory(viewer);
			}
		}.runTask();
	}

	private boolean hasChanged(ItemStack before, ItemStack after) {
		if (before == null || before.getType() == Material.AIR)
			return after != null && after.getType() != Material.AIR;

		if (after == null || after.getType() == Material.AIR)
			return true;

		return !before.isSimilar(after) || before.getAmount() != after.getAmount();
	}

	private void removeMaxStackSize(ItemStack item) {
		ItemMeta meta = item.getItemMeta();

		if (meta == null || !meta.hasMaxStackSize())
			return;

		meta.setMaxStackSize(null);
		item.setItemMeta(meta);
	}

	private void updateInventory(HumanEntity viewer) {
		if (viewer instanceof Player) {
			Player player = (Player) viewer;
			player.updateInventory();
		}
	}
}