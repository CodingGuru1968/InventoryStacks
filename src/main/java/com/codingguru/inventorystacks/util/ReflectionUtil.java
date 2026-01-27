package com.codingguru.inventorystacks.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.cryptomorin.xseries.XMaterial;

@SuppressWarnings("deprecation")
public final class ReflectionUtil {

	private final static VersionUtil SERVER_VERSION = ItemHandler.getInstance().getServerVersion();
	private final static InventoryStacks PLUGIN = InventoryStacks.getInstance();

	private ReflectionUtil() {
	}

	public static void applyStackSizeToMaterial(Object nmsItem, Material material, int size) {
		try {
			if (nmsItem == null) {
				ConsoleUtil.info(ChatColor.GRAY + "Skipping " + material.name() + " (no item form)");
				return;
			}

			ItemHandler.getInstance().applyStackSizeToNmsItem(nmsItem, size);
			ConsoleUtil.info(ChatColor.YELLOW + "Successfully set " + material.name() + " stack size to: " + size);

			if (VersionUtil.v1_21.isServerVersionHigher()) // DON'T SET BUKKIT MAX STACK
				return;

			setClassField(Material.class, material, "maxStack", size, material.name());
		} catch (Throwable t) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to set " + material.name() + " stack size to: " + size);
			t.printStackTrace();
		}
	}

	public static void applyConfiguredStacks() {
		Map<String, Integer> resolvedStacks = new HashMap<String, Integer>();

		if (PLUGIN.getConfig().isSet("items")) {
			resolvedStacks = resolveConfiguredItems(PLUGIN.getConfig().getConfigurationSection("items").getKeys(true));

			for (Map.Entry<String, Integer> e : resolvedStacks.entrySet()) {
				String matName = e.getKey();
				int size = e.getValue();

				XMaterial xMat = XMaterial.matchXMaterial(matName).orElse(null);

				if (xMat == null) {
					ConsoleUtil.warning(ChatColor.RED + "The Item: " + matName
							+ " does not exist. Check MATERIAL_LIST.txt for all up to date item names.");
					continue;
				}

				Material mat = xMat.get();

				if (mat == null) {
					continue;
				}

				Object nmsItem = hasItemForm(mat);

				if (nmsItem == null) {
					continue;
				}

				ItemHandler.getInstance().cacheMaterial(xMat, mat.getMaxStackSize());
				applyStackSizeToMaterial(nmsItem, mat, size);
			}
		}

		if (PLUGIN.getConfig().getBoolean("max-stack-for-all-items.enabled")) {
			int stackSize = validateStackSize(PLUGIN.getConfig().getInt("max-stack-for-all-items.amount"), "ALL");

			List<String> configExemptList = PLUGIN.getConfig().getStringList("max-stack-for-all-items.whitelist");

			Set<String> exemptMaterials = configExemptList.stream().filter(Objects::nonNull).map(s -> s.toUpperCase())
					.collect(Collectors.toSet());

			resolvedStacks.keySet().forEach(name -> exemptMaterials.add(name.toUpperCase()));

			updateAllItems(exemptMaterials, stackSize);
		}
	}

	private static Map<String, Integer> resolveConfiguredItems(Collection<String> keys) {
		Map<String, Integer> resolved = new HashMap<>();
		XMaterial[] allMaterials = XMaterial.VALUES;

		for (String key : keys) {
			if (key == null)
				continue;

			if (key.isEmpty())
				continue;

			if (!PLUGIN.getConfig().isInt("items." + key))
				continue;

			int stackSize = validateStackSize(PLUGIN.getConfig().getInt("items." + key), key);

			final Pattern pattern;

			try {
				pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException ex) {
				ConsoleUtil.warning(ChatColor.RED + "Invalid regex in items: '" + key + "': " + ex.getDescription());
				continue;
			}

			for (XMaterial material : allMaterials) {
				String name = material.name();

				if (pattern.matcher(name).matches()) {
					resolved.putIfAbsent(name.toUpperCase(), stackSize);
				}
			}
		}

		return resolved;
	}

	private static void updateAllItems(Set<String> exemptMaterials, int stackSize) {
		Set<Material> processed = new HashSet<>();

		for (XMaterial xMat : XMaterial.VALUES) {
			if (!xMat.isSupported())
				continue;

			Material mat = xMat.get();

			if (mat == null)
				continue;

			if (exemptMaterials.contains(mat.name().toUpperCase())
					|| exemptMaterials.contains(xMat.name().toUpperCase()))
				continue;

			if (!processed.add(mat))
				continue;

			Object nmsItem = hasItemForm(mat);

			if (nmsItem == null)
				continue;

			ItemHandler.getInstance().cacheMaterial(xMat, mat.getMaxStackSize());
			applyStackSizeToMaterial(nmsItem, mat, stackSize);
		}
	}

	public static Object hasItemForm(Material mat) {
		try {
			return getNMSItem(mat);
		} catch (Throwable t) {
			return null;
		}
	}

	private static Object getNMSItem(Material material) throws Exception {
		String cbPkg = Bukkit.getServer().getClass().getPackage().getName();
		String base = cbPkg.startsWith("org.bukkit.craftbukkit") ? cbPkg : "org.bukkit.craftbukkit";

		Class<?> craftMagicNumbers;
		try {
			craftMagicNumbers = Class.forName(base + ".util.CraftMagicNumbers");
		} catch (ClassNotFoundException ex) {
			craftMagicNumbers = Class.forName("org.bukkit.craftbukkit.util.CraftMagicNumbers");
		}

		Method getItem = craftMagicNumbers.getMethod("getItem", Material.class);
		return getItem.invoke(null, material);
	}

	private static boolean setClassField(Class<?> clazz, Object instance, String fieldName, int value, String name) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.setInt(instance, value);

			ConsoleUtil.info("Successfully set Bukkit Material " + name + " max stack to: " + value);
			return true;
		} catch (Throwable t) {
			ConsoleUtil.warning("Failed to set Bukkit Material max stack of: " + name);
			t.printStackTrace();
			return false;
		}
	}

	private static int validateStackSize(int stackSize, String itemName) {
		int absoluteMaxStackSize = SERVER_VERSION.getAbsoluteMaxStackSize();

		if (stackSize > absoluteMaxStackSize) {
			ConsoleUtil.warning("Stack size: " + stackSize + " can not be set for " + itemName + " item(s) over "
					+ absoluteMaxStackSize + ". Defaulting to max value...");
			return absoluteMaxStackSize;
		}

		if (stackSize < 1) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to set stack size to: " + stackSize + " for " + itemName
					+ " item(s). Defaulting to 1...");
			stackSize = 1;
		}

		return stackSize;
	}
}