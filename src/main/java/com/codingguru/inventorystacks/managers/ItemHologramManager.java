package com.codingguru.inventorystacks.managers;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.GroundStackUtil;
import com.codingguru.inventorystacks.util.ServerTypeUtil;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ItemHologramManager {

	private static final int DEFAULT_DESPAWN_TICKS = 6000;
	private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
	private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
	private static final Method COMPONENT_CUSTOM_NAME_METHOD = resolveComponentCustomNameMethod();

	private final Plugin plugin;
	private final Map<UUID, UUID> itemToHologram = new ConcurrentHashMap<>();
	private BukkitTask bukkitTask;
	private ScheduledTask foliaTask;
	private boolean enabled;
	private long updateIntervalTicks;
	private double yOffset;
	private boolean showDespawnTimer;
	private int despawnTicks;
	private String format;

	public ItemHologramManager(Plugin plugin) {
		this.plugin = plugin;
	}

	public void enable() {
		reload();
	}

	public void disable() {
		stopTask();
		clearAll();
	}

	public void reload() {
		stopTask();
		loadConfig();

		if (!enabled) {
			clearAll();
			return;
		}

		startTask();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void track(Item item) {
		if (!enabled || !isTrackable(item))
			return;

		UUID itemId = item.getUniqueId();
		ArmorStand stand = resolveStand(itemToHologram.get(itemId));

		if (stand == null) {
			stand = spawnHologram(item);
			if (stand == null)
				return;

			itemToHologram.put(itemId, stand.getUniqueId());
		}

		updateHologram(item, stand);
	}

	public void untrack(Item item) {
		if (item == null)
			return;

		UUID standId = itemToHologram.remove(item.getUniqueId());
		removeStand(standId);
	}

	public void merge(Item source, Item target) {
		untrack(source);
		track(target);
	}

	private void loadConfig() {
		enabled = plugin.getConfig().getBoolean("item-hologram.enabled", false);
		updateIntervalTicks = Math.max(1L, plugin.getConfig().getLong("item-hologram.update-interval-ticks", 10L));
		yOffset = plugin.getConfig().getDouble("item-hologram.y-offset", 0.6D);
		showDespawnTimer = plugin.getConfig().getBoolean("item-hologram.show-despawn-timer", true);
		despawnTicks = Math.max(20, plugin.getConfig().getInt("item-hologram.despawn-ticks", DEFAULT_DESPAWN_TICKS));
		format = plugin.getConfig().getString("item-hologram.format", "&e%amount%x &f%item% &7(%time%s)");
	}

	private void startTask() {
		if (ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA) {
			foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> updateAll(),
					updateIntervalTicks, updateIntervalTicks);
		} else {
			bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, updateIntervalTicks,
					updateIntervalTicks);
		}
	}

	private void stopTask() {
		if (bukkitTask != null) {
			bukkitTask.cancel();
			bukkitTask = null;
		}

		if (foliaTask != null) {
			foliaTask.cancel();
			foliaTask = null;
		}
	}

	private void updateAll() {
		if (!enabled)
			return;

		itemToHologram.entrySet().removeIf(entry -> {
			Item item = resolveItem(entry.getKey());
			ArmorStand stand = resolveStand(entry.getValue());

			if (!isTrackable(item)) {
				removeStand(entry.getValue());
				return true;
			}

			if (stand == null) {
				stand = spawnHologram(item);
				if (stand == null)
					return true;
				entry.setValue(stand.getUniqueId());
			}

			updateHologram(item, stand);
			return false;
		});
	}

	private boolean isTrackable(Item item) {
		if (item == null || !item.isValid() || item.isDead())
			return false;

		ItemStack stack = item.getItemStack();
		return stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0;
	}

	private ArmorStand spawnHologram(Item item) {
		try {
			Location spawnLocation = item.getLocation().clone().add(0D, yOffset, 0D);
			return item.getWorld().spawn(spawnLocation, ArmorStand.class, stand -> {
				stand.setVisible(false);
				stand.setSmall(true);
				stand.setMarker(true);
				stand.setGravity(false);
				stand.setInvulnerable(true);
				stand.setCanPickupItems(false);
				stand.setBasePlate(false);
				stand.setCustomNameVisible(true);
				stand.setPersistent(false);
				stand.setCollidable(false);
			});
		} catch (Exception ignored) {
			return null;
		}
	}

	private void updateHologram(Item item, ArmorStand stand) {
		stand.teleport(item.getLocation().clone().add(0D, yOffset, 0D));
		setCustomNameCompat(stand, formatLine(item));
	}

	@SuppressWarnings("deprecation")
	private void setCustomNameCompat(ArmorStand stand, String line) {
		if (COMPONENT_CUSTOM_NAME_METHOD != null) {
			try {
				COMPONENT_CUSTOM_NAME_METHOD.invoke(stand, LEGACY_AMPERSAND.deserialize(line));
				return;
			} catch (Exception ignored) {
			}
		}

		stand.setCustomName(LEGACY_SECTION.serialize(LEGACY_AMPERSAND.deserialize(line)));
	}

	private static Method resolveComponentCustomNameMethod() {
		try {
			Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
			return ArmorStand.class.getMethod("customName", componentClass);
		} catch (Exception ignored) {
			return null;
		}
	}

	private String formatLine(Item item) {
		ItemStack stack = item.getItemStack();
		int displayAmount = GroundStackUtil.getTotal(item);
		String timeString = formatDuration(Math.max(0, (despawnTicks - item.getTicksLived()) / 20));
		String line = format.replace("%amount%", Integer.toString(displayAmount)).replace("%item%",
				toPrettyName(stack.getType()));

		line = line.replace("%time%s", timeString);
		line = line.replace("%time%", timeString);

		if (!showDespawnTimer) {
			line = line.replace("(%time%)", "").replace("(%time%s)", "").replace("%time%s", "").replace("%time%", "");
		}

		return line.trim();
	}

	private String formatDuration(int totalSeconds) {
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		StringBuilder out = new StringBuilder();

		if (hours > 0) {
			out.append(hours).append("h ");
		}

		if (hours > 0 || minutes > 0) {
			out.append(minutes).append("m ");
		}

		out.append(seconds).append("s");
		return out.toString();
	}

	private String toPrettyName(Material material) {
		String[] parts = material.name().toLowerCase(Locale.ENGLISH).split("_");
		StringBuilder out = new StringBuilder();

		for (String part : parts) {
			if (part.isEmpty())
				continue;

			if (!out.toString().isEmpty()) {
				out.append(' ');
			}

			out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}

		return out.toString();
	}

	private Item resolveItem(UUID itemId) {
		if (itemId == null)
			return null;

		Entity entity = Bukkit.getEntity(itemId);
		return entity instanceof Item ? (Item) entity : null;
	}

	private ArmorStand resolveStand(UUID standId) {
		if (standId == null)
			return null;

		Entity entity = Bukkit.getEntity(standId);
		return entity instanceof ArmorStand ? (ArmorStand) entity : null;
	}

	private void removeStand(UUID standId) {
		ArmorStand stand = resolveStand(standId);
		if (stand != null) {
			stand.remove();
		}
	}

	private void clearAll() {
		for (UUID standId : itemToHologram.values()) {
			removeStand(standId);
		}
		itemToHologram.clear();
	}
}