package com.codingguru.inventorystacks.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;

public final class ReflectionUtil {

	public static int getDefaultStackValue(Class<?> clas, Material mat, String fieldName, String name) {
		int defaultSize = 1; // More safe than using 64 as could be tools, armour, etc

		Field currentField;

		try {
			currentField = clas.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to retreive the max item stack size of: " + name + ".");
			return defaultSize;
		}

		currentField.setAccessible(true);

		try {
			defaultSize = currentField.getInt(mat);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to retreive the max item stack size of: " + name + ".");
			defaultSize = 1;
			return defaultSize;
		}

		currentField.setAccessible(false);

		return defaultSize;
	}

	public static boolean setClassField(Class<?> clas, Object instance, String fieldName, int value, String name) {
		Field currentField;
		try {
			currentField = clas.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + name + " (" + clas.getName() + ")");
			return false;
		}

		currentField.setAccessible(true);

		try {
			currentField.set(instance, Integer.valueOf(value));
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + name + " (" + clas.getName() + ")");
			return false;
		}

		currentField.setAccessible(false);
		ConsoleUtil.info("Successfully set class " + name + " stack size to: " + value + " (" + clas.getName() + ")");
		return true;
	}

	public static boolean setItemField(String materialName, int stack) {
		Field itemField;
		Object item;

		try {
			itemField = VersionUtil.v1_18_R1.isServerVersionHigher() ? findItemClassFromName(materialName)
					: ItemHandler.getInstance().getItemsClass().getDeclaredField(materialName.toUpperCase());
		} catch (SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}

		try {
			item = itemField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}

		if (VersionUtil.v1_20_R4.isServerVersionHigher()) {
			return setComponentField(item, materialName, stack);
		}

		return setClassField(ItemHandler.getInstance().getItemClass(), item,
				ItemHandler.getInstance().getServerVersion().getFieldName(), stack, materialName);
	}

	private static Field findItemClassFromName(String materialName) {
		for (Field field : ItemHandler.getInstance().getItemsClass().getFields()) {
			Object found;

			try {
				found = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
				continue;
			}

			if (found == null)
				continue;

			String name;

			try {
				name = (String) ItemHandler.getInstance().getItemNameMethod().invoke(found);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassCastException e) {
				e.printStackTrace();
				continue;
			}

			String[] split = name.split("\\.");

			name = split[split.length - 1];

			if (name.equals(materialName)) {
				return field;
			}
		}
		return null;
	}

	public static void updateAllItems(List<String> exemptMaterials, int stackSize) {
		for (Field field : ItemHandler.getInstance().getItemsClass().getFields()) {
			Object found;

			try {
				found = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
				continue;
			}

			if (found == null)
				continue;

			String name;

			try {
				name = (String) ItemHandler.getInstance().getItemNameMethod().invoke(found);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassCastException e) {
				e.printStackTrace();
				continue;
			}

			String[] split = name.split("\\.");

			name = split[split.length - 1];

			if (name.equalsIgnoreCase("air"))
				continue;

			if (exemptMaterials.contains(name)) {
				ConsoleUtil.info("Material: " + name + " is whitelisted. Skipping this entry.");
				continue;
			}

			Optional<XMaterialUtil> foundMaterial = XMaterialUtil.matchXMaterial(name);

			if (!foundMaterial.isPresent()) {
				ConsoleUtil
						.warning("Could not find a valid material with the name: " + name + ". Skipping this entry.");
				continue;
			}

			XMaterialUtil xMaterial = foundMaterial.get();
			Material material = xMaterial.parseMaterial();
			int defaultSize = getDefaultStackValue(Material.class, material, "maxStack", name);
			ItemHandler.getInstance().getCachedMaterialSizes().put(xMaterial, defaultSize);
			setClassField(Material.class, material, "maxStack", stackSize, name);

			if (!InventoryStacks.getInstance().getConfig().getBoolean("only-stack-via-command")) {
				setItemField(name, stackSize);
			}
		}
	}

	public static boolean setComponentField(Object item, String materialName, int stack) {
		try {
			Object components = ItemHandler.getInstance().getItemComponentsField().get(item);
			ItemHandler.getInstance().getComponentMapPutMethod().invoke(
					ItemHandler.getInstance().getComponentsMapField().get(components),
					new Object[] { ItemHandler.getInstance().getMaxStackSize(), Integer.valueOf(stack) });
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}
		ConsoleUtil.info("Successfully set component " + materialName + " stack size to: " + stack);
		return true;
	}
}