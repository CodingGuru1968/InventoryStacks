package com.codingguru.inventorystacks.handlers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.ReflectionUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import com.google.common.collect.Maps;

public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();
	private final Map<Material, Integer> loadedMaterials;
	private VersionUtil serverVersion;
	private Class<?> itemClass;
	private Class<?> itemsClass;
	private Method getItemNameMethod;

	private ItemHandler() {
		loadedMaterials = Maps.newHashMap();
	}

	public boolean setupServerVersion() {
		String versionFound = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		for (VersionUtil version : VersionUtil.values()) {
			if (versionFound.startsWith(version.name())) {
				serverVersion = version;
				return true;
			}
		}

		return false;
	}

	public void setupReflectionClasses() {
		String version = ItemHandler.getInstance().getServerVersion().toString();

		try {
			itemClass = Class.forName(VersionUtil.v1_17.isServerVersionHigher() ? "net.minecraft.world.item.Item"
					: ("net.minecraft.server." + version + ".Item"));
			itemsClass = Class.forName(VersionUtil.v1_17.isServerVersionHigher() ? "net.minecraft.world.item.Items"
					: ("net.minecraft.server." + version + ".Items"));
			getItemNameMethod = itemClass.getMethod(VersionUtil.v1_18.isServerVersionHigher() ? "a" : "getName");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public void setupLoadedMaterials() {
		for (String materialName : InventoryStacks.getInstance().getConfig().getConfigurationSection("ITEMS")
				.getKeys(false)) {

			Optional<XMaterialUtil> foundMaterial = XMaterialUtil.matchXMaterial(materialName);

			if (!foundMaterial.isPresent()) {
				ConsoleUtil.warning(
						"Could not find a valid material with the name: " + materialName + ". Skipping this entry.");
				continue;
			}

			Material material = foundMaterial.get().parseMaterial();

			int stackSize = InventoryStacks.getInstance().getConfig().getInt("ITEMS." + materialName);

			if (stackSize > 127) {
				stackSize = 127;
				ConsoleUtil.warning("Could not set " + materialName + " stack size over 127. Defaulting to max value.");
			}

			changeStackSize(material, foundMaterial.get().name(), stackSize);
			loadedMaterials.put(material, stackSize);
		}
	}

	private void changeStackSize(Material material, String name, int stackSize) {
		boolean materialClassChange = ReflectionUtil.setClassField(Material.class, material, "maxStack", stackSize);
		boolean itemClassChange = ReflectionUtil.setItemField(name, stackSize);
		if (materialClassChange && itemClassChange) {
			ConsoleUtil.info("Successfully set " + name + " stack size to: " + stackSize);
		} else {
			ConsoleUtil.warning("Failed to set the max item stack size of: " + name + ".");
		}
	}

	public Method getItemNameMethod() {
		return getItemNameMethod;
	}

	public Class<?> getItemClass() {
		return itemClass;
	}

	public Class<?> getItemsClass() {
		return itemsClass;
	}

	public VersionUtil getServerVersion() {
		return serverVersion;
	}

	public Set<Material> getLoadedMaterials() {
		return loadedMaterials.keySet();
	}

	public static ItemHandler getInstance() {
		return INSTANCE;
	}
}
