package com.codingguru.inventorystacks.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.ReflectionUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();
	private Map<XMaterialUtil, Integer> cachedMaterials;

	private boolean isRunningPaper;
	private VersionUtil serverVersion;

	private Class<?> itemClass;
	private Class<?> itemsClass;
	private Class<?> dataComponentsClass;

	private Method getItemNameMethod;
	private Method componentMapPutMethod;

	private Field itemsComponentsField;
	private Field componentsMapField;

	private Object maxStackSize;

	public ItemHandler() {
		cachedMaterials = Maps.newHashMap();
	}

	public void reloadInventoryStacks() {
		resetMaterialsToDefaultValues();
		setupLoadedMaterials();
	}

	public void resetMaterialsToDefaultValues() {
		for (XMaterialUtil xMat : cachedMaterials.keySet()) {
			Material mat = xMat.parseMaterial();
			int defaultAmount = cachedMaterials.get(xMat);
			String name = xMat.name();
			ReflectionUtil.setClassField(Material.class, mat, "maxStack", defaultAmount, name.toLowerCase());
			ReflectionUtil.setItemField(name.toLowerCase(), defaultAmount);
		}
		cachedMaterials.clear();
	}

	public boolean setupServerVersion() {
		String packageVersion = Bukkit.getServer().getClass().getPackage().getName();
		String versionFound = packageVersion.substring(packageVersion.lastIndexOf('.') + 1);

		checkIfRunningPaper();

		if (versionFound.equalsIgnoreCase("craftbukkit")) {
			if (Bukkit.getBukkitVersion().startsWith("1.21")) {
				serverVersion = VersionUtil.v1_21_R1;
				return true;
			} else if (Bukkit.getBukkitVersion().startsWith("1.20")) {
				serverVersion = VersionUtil.v1_21_R1;
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

	public void checkIfRunningPaper() {
		try {
			Class.forName("io.papermc.paperclip.Paperclip"); // Main class
			isRunningPaper = true;
		} catch (Exception e) {
		}
	}

	public void setupReflectionClasses() {
		String version = ItemHandler.getInstance().getServerVersion().toString();

		try {
			itemClass = Class.forName(VersionUtil.v1_17_R1.isServerVersionHigher() ? "net.minecraft.world.item.Item"
					: ("net.minecraft.server." + version + ".Item"));
			itemsClass = Class.forName(VersionUtil.v1_17_R1.isServerVersionHigher() ? "net.minecraft.world.item.Items"
					: ("net.minecraft.server." + version + ".Items"));
			getItemNameMethod = itemClass.getMethod(VersionUtil.v1_18_R1.isServerVersionHigher() ? "a" : "getName");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		if (VersionUtil.v1_20_R4.isServerVersionHigher()) {
			try {
				dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
				maxStackSize = dataComponentsClass.getField(isRunningPaper ? "MAX_STACK_SIZE" : "c").get(null);
				itemsComponentsField = itemClass.getDeclaredField(isRunningPaper ? "components" : "c");
				itemsComponentsField.setAccessible(true);

				Object itemComponents = itemsComponentsField.get(itemsClass.getFields()[0].get(null));

				componentsMapField = itemComponents.getClass().getDeclaredField(isRunningPaper ? "map" : "c");
				componentsMapField.setAccessible(true);
				componentMapPutMethod = componentsMapField.getType().getMethod("put",
						new Class[] { Object.class, Object.class });
			} catch (SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public void setupLoadedMaterials() {
		Map<String, Integer> materialListRegex = getFoundMaterialList(Lists.newArrayList(
				InventoryStacks.getInstance().getConfig().getConfigurationSection("items").getKeys(true)));

		for (String materialName : materialListRegex.keySet()) {
			Optional<XMaterialUtil> foundMaterial = XMaterialUtil.matchXMaterial(materialName);

			if (!foundMaterial.isPresent()) {
				ConsoleUtil.warning(
						"Could not find a valid material with the name: " + materialName + ". Skipping this entry.");
				continue;
			}

			XMaterialUtil xMaterial = foundMaterial.get();
			Material material = xMaterial.parseMaterial();
			
			if (material == Material.AIR) {
				ConsoleUtil.warning(
						"Could not find a valid material with the name: " + materialName + ". Skipping this entry.");
				continue;
			}

			
			int stackSize = materialListRegex.get(materialName);

			String name = xMaterial.name();
			int defaultSize = ReflectionUtil.getDefaultStackValue(Material.class, material, "maxStack",
					name.toLowerCase());
			getCachedMaterialSizes().put(xMaterial, defaultSize);
			ReflectionUtil.setClassField(Material.class, material, "maxStack", stackSize, name.toLowerCase());
			ReflectionUtil.setItemField(name.toLowerCase(), stackSize);
		}

		if (InventoryStacks.getInstance().getConfig().getBoolean("max-stack-for-all-items.enabled")) {
			int stackSize = InventoryStacks.getInstance().getConfig().getInt("max-stack-for-all-items.amount");

			if (stackSize > getServerVersion().getAbsoluteMaxStackSize()) {
				stackSize = getServerVersion().getAbsoluteMaxStackSize();
				ConsoleUtil.warning("Could not set stack size over " + getServerVersion().getAbsoluteMaxStackSize()
						+ ". Defaulting to max value for ALL items.");
			}

			List<String> configExemptList = InventoryStacks.getInstance().getConfig()
					.isSet("max-stack-for-all-items.whitelist")
							? InventoryStacks.getInstance().getConfig()
									.getStringList("max-stack-for-all-items.whitelist")
							: InventoryStacks.getInstance().getConfig()
									.getStringList("max-stack-for-all-items.blacklist");

			List<String> exemptMaterials = configExemptList.stream().map(String::toLowerCase)
					.collect(Collectors.toList());

			materialListRegex.keySet().forEach(name -> exemptMaterials.add(name.toLowerCase()));

			ReflectionUtil.updateAllItems(exemptMaterials, stackSize);
		}
	}

	public Map<String, Integer> getFoundMaterialList(List<String> materialNames) {
		Map<String, Integer> foundTypes = Maps.newHashMap();

		for (String regexPattern : materialNames) {
			Pattern pattern = Pattern.compile(regexPattern);

			for (XMaterialUtil material : XMaterialUtil.VALUES) {
				String name = material.name();
				Matcher m = pattern.matcher(name);

				while (m.find()) {
					if (m.group().trim().length() > 0) {
						foundTypes.put(m.group(), getStackSize(regexPattern));
					}
				}
			}
		}

		return foundTypes;
	}

	public int getStackSize(String itemPath) {
		int stackSize = InventoryStacks.getInstance().getConfig().getInt("items." + itemPath);

		if (stackSize > getServerVersion().getAbsoluteMaxStackSize()) {
			stackSize = getServerVersion().getAbsoluteMaxStackSize();
			ConsoleUtil.warning("Could not set " + itemPath + " stack size over "
					+ getServerVersion().getAbsoluteMaxStackSize() + ". Defaulting to max value.");
		}

		return stackSize;
	}

	public Map<XMaterialUtil, Integer> getCachedMaterialSizes() {
		return cachedMaterials;
	}

	public Object getMaxStackSize() {
		return maxStackSize;
	}

	public Field getComponentsMapField() {
		return componentsMapField;
	}

	public Field getItemComponentsField() {
		return itemsComponentsField;
	}

	public Method getComponentMapPutMethod() {
		return componentMapPutMethod;
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

	public static ItemHandler getInstance() {
		return INSTANCE;
	}
}
