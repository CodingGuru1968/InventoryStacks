
package com.codingguru.inventorystacks.listeners.correction;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.scheduler.Schedule;
import com.codingguru.inventorystacks.util.LangDefaults;
import com.codingguru.inventorystacks.util.MessageBuilder;

public class ShulkerBoxFix implements Listener {

    private final InventoryStacks plugin;

    public ShulkerBoxFix(InventoryStacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();

        if (inventory.getType() != InventoryType.SHULKER_BOX)
            return;

        scheduleFix(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getView().getTopInventory();

        if (inventory.getType() != InventoryType.SHULKER_BOX)
            return;

        if (isMovingIntoShulker(e)) {
            ItemStack movingItem = getMovingItem(e);

            if (isInvalidStackedDamageable(movingItem)) {
                sendDisallowMessage(e.getWhoClicked());
                e.setCancelled(true);
                return;
            }

            if (hasCustomStackSize(movingItem) && isDamageable(movingItem)) {
                removeMaxStackSize(movingItem);
                return;
            }
        }

        scheduleFix(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        Inventory inventory = e.getView().getTopInventory();

        if (inventory.getType() != InventoryType.SHULKER_BOX)
            return;

        boolean movingIntoShulker = false;

        for (Integer rawSlot : e.getRawSlots()) {
            if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
                movingIntoShulker = true;
                break;
            }
        }

        if (!movingIntoShulker)
            return;

        ItemStack cursor = e.getOldCursor();

        if (isInvalidStackedDamageable(cursor)) {
            sendDisallowMessage(e.getWhoClicked());
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

        scheduleFix(inventory);
    }

    private boolean isMovingIntoShulker(InventoryClickEvent e) {
        if (e.getClickedInventory() == null)
            return false;

        if (e.getClickedInventory() == e.getView().getTopInventory())
            return false;

        return e.getClickedInventory() == e.getView().getBottomInventory();
    }

    @SuppressWarnings("removal")
    private ItemStack getMovingItem(InventoryClickEvent e) {
        switch (e.getAction()) {
        case PLACE_ALL:
        case PLACE_ONE:
        case PLACE_SOME:
        case SWAP_WITH_CURSOR:
            return e.getCursor();

        case HOTBAR_SWAP:
        case HOTBAR_MOVE_AND_READD:
            int button = e.getHotbarButton();

            if (button >= 0)
                return e.getWhoClicked().getInventory().getItem(button);

            return e.getWhoClicked().getInventory().getItemInOffHand();

        case MOVE_TO_OTHER_INVENTORY:
            return e.getCurrentItem();

        default:
            return null;
        }
    }

    private boolean isInvalidStackedDamageable(ItemStack item) {
        return hasCustomStackSize(item) && isDamageable(item) && item.getAmount() > 1;
    }

    private boolean hasCustomStackSize(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();
        return meta.hasMaxStackSize() && meta.getMaxStackSize() > 1;
    }

    private boolean isDamageable(ItemStack item) {
        return item != null && item.getType().getMaxDurability() > 0;
    }

    private void sendDisallowMessage(HumanEntity entity) {
        new MessageBuilder.Builder("disallow-shulker-stack-damageable",
                LangDefaults.DISALLOW_SHULKER_STACK_DAMAGEABLE).send(entity);
    }

    private void removeMaxStackSize(ItemStack item) {
        if (item == null)
            return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasMaxStackSize())
            return;

        meta.setMaxStackSize(null);
        item.setItemMeta(meta);
    }

    private void scheduleFix(Inventory inventory) {
        new Schedule(plugin) {
            @Override
            public void run() {
                boolean changed = false;

                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack item = inventory.getItem(slot);

                    if (item == null || item.getType() == Material.AIR)
                        continue;

                    if (!hasCustomStackSize(item) || !isDamageable(item))
                        continue;

                    if (item.getAmount() > 1)
                        continue;

                    removeMaxStackSize(item);
                    inventory.setItem(slot, item);
                    changed = true;
                }

                if (changed)
                    updateViewers(inventory);
            }
        }.runTask();
    }

    private void updateViewers(Inventory inventory) {
        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player)
                ((Player) viewer).updateInventory();
        }
    }
}