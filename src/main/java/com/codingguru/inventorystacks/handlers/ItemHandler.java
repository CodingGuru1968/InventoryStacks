package com.codingguru.inventorystacks.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.ReflectionUtil;
import com.codingguru.inventorystacks.util.ServerTypeUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Maps;

@SuppressWarnings("deprecation")
public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();

	private final Map<XMaterial, Integer> cachedMaterials = Maps.newHashMap();

	private VersionUtil serverVersion;
	private ServerTypeUtil serverType;

	private Field legacyMaxStackField;

	private Object maxStackComponentKey;
	private Method itemComponentsAccessor;
	private Field itemComponentsField;

	private Method builderFactory;
	private Method builderAddAll;
	private Method builderSet;
	private Method builderBuild;

	private ItemHandler() {
	}

	public static ItemHandler getInstance() {
		return INSTANCE;
	}

	public void setup() {
		if (!setupServerVersion()) {
			String pkg = Bukkit.getServer().getClass().getPackage().getName();
			String versionFound = pkg.substring(pkg.lastIndexOf('.') + 1);
			ConsoleUtil.warning("THE VERSION: " + versionFound + " IS CURRENTLY UNSUPPORTED. DISABLING PLUGIN...");
			Bukkit.getPluginManager().disablePlugin(InventoryStacks.getInstance());
			return;
		}

		setupServerType();
		setupReflection();
	}

	public boolean setupServerVersion() {
		String pkg = Bukkit.getServer().getClass().getPackage().getName();
		String versionFound = pkg.substring(pkg.lastIndexOf('.') + 1);

		String cleanVersion = Bukkit.getBukkitVersion().split("-")[0];
		String[] parts = cleanVersion.split("\\.");
		if (parts.length < 2)
			return false;

		String majorMinor = parts[0] + "." + parts[1];

		if (versionFound.equalsIgnoreCase("craftbukkit")) {
			if (majorMinor.equals("1.21")) {
				serverVersion = VersionUtil.v1_21;
				return true;
			}
			if (majorMinor.equals("1.20")) {
				serverVersion = VersionUtil.v1_20;
				return true;
			}
			return false;
		}

		for (VersionUtil version : VersionUtil.values()) {
			if (versionFound.contains(version.name())) {
				serverVersion = version;
				return true;
			}
		}
		return false;
	}

	private void setupServerType() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			serverType = ServerTypeUtil.FOLIA;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		try {
			Class.forName("io.papermc.paper.ServerBuildInfo");
			serverType = ServerTypeUtil.PAPER;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		serverType = ServerTypeUtil.SPIGOT;
	}

	private void setupReflection() {
		try {
			Class<?> itemClass = Class.forName(serverVersion.getItemClass());

			if (!serverVersion.usesDataComponents()) {
				legacyMaxStackField = itemClass.getDeclaredField(serverVersion.getLegacyStackField());
				legacyMaxStackField.setAccessible(true);
				return;
			}

			Class<?> dataComponents = Class.forName("net.minecraft.core.component.DataComponents");
			Field maxStackField;

			try {
				maxStackField = dataComponents.getField("MAX_STACK_SIZE");
			} catch (NoSuchFieldException e) {
				maxStackField = dataComponents.getField("c");
			}

			maxStackComponentKey = maxStackField.get(null);

			itemComponentsAccessor = itemClass.getDeclaredMethod("f");
			itemComponentsAccessor.setAccessible(true);

			itemComponentsField = itemClass.getDeclaredField("c");
			itemComponentsField.setAccessible(true);

			Class<?> dataComponentMapClass = Class.forName("net.minecraft.core.component.DataComponentMap");
			Class<?> builderClass = Class.forName("net.minecraft.core.component.DataComponentMap$a");

			builderFactory = dataComponentMapClass.getDeclaredMethod("a");
			builderFactory.setAccessible(true);

			builderAddAll = builderClass.getDeclaredMethod("a", dataComponentMapClass);
			builderAddAll.setAccessible(true);

			Class<?> keyType = Class.forName("net.minecraft.core.component.DataComponentType");
			builderSet = builderClass.getDeclaredMethod("a", keyType, Object.class);
			builderSet.setAccessible(true);

			builderBuild = builderClass.getDeclaredMethod("a");
			builderBuild.setAccessible(true);
		} catch (Throwable t) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to setup reflection values. Disabling plugin...");
			t.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(InventoryStacks.getInstance());
		}
	}

	public void applyStackSizeToNmsItem(Object nmsItem, int size) {
		try {
			if (!serverVersion.usesDataComponents()) {
				legacyMaxStackField.setInt(nmsItem, size);
				return;
			}

			Object currentMap = itemComponentsAccessor.invoke(nmsItem);

			Object builder = builderFactory.invoke(null);
			builderAddAll.invoke(builder, currentMap);
			builderSet.invoke(builder, maxStackComponentKey, Integer.valueOf(size));
			Object newMap = builderBuild.invoke(builder);

			itemComponentsField.set(nmsItem, newMap);
		} catch (Throwable t) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to set " + nmsItem.toString() + " stack size to: " + size);
			t.printStackTrace();
		}
	}

	public void cacheMaterial(XMaterial mat, int defaultSize) {
		cachedMaterials.putIfAbsent(mat, defaultSize);
	}

	public Map<XMaterial, Integer> getCachedMaterialSizes() {
		return cachedMaterials;
	}

	public void reloadInventoryStacks() {
		resetMaterialsToDefaultValues();
		cachedMaterials.clear();
		ReflectionUtil.applyConfiguredStacks();
	}

	private void resetMaterialsToDefaultValues() {
		for (Map.Entry<XMaterial, Integer> entry : cachedMaterials.entrySet()) {
			Material mat = entry.getKey().get();

			Object nmsItem = ReflectionUtil.hasItemForm(mat);

			if (nmsItem == null) {
				continue;
			}

			int defaultSize = entry.getValue();
			ReflectionUtil.applyStackSizeToMaterial(nmsItem, mat, defaultSize);
		}
	}

	public VersionUtil getServerVersion() {
		return serverVersion;
	}

	public ServerTypeUtil getServerType() {
		return serverType;
	}
}
