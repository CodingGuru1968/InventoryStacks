package com.codingguru.inventorystacks.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;

@SuppressWarnings("deprecation")
public final class ReflectionLegacyUtil {

	private final static VersionUtil SERVER_VERSION = ItemHandler.getInstance().getServerVersion();

	private static Field legacyMaxStackField;

	private static Object maxStackComponentKey;
	private static Method itemComponentsAccessor;
	private static Field itemComponentsField;

	private static Method builderFactory;
	private static Method builderAddAll;
	private static Method builderSet;
	private static Method builderBuild;

	private ReflectionLegacyUtil() {
	}

	public static void setup() {
		try {
			Class<?> itemClass = Class.forName(SERVER_VERSION.getItemClass());

			if (!SERVER_VERSION.usesDataComponents()) {
				legacyMaxStackField = itemClass.getDeclaredField(SERVER_VERSION.getLegacyStackField());
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

	public static void applyStackSizeToMaterial(Object nmsItem, Material material, int size) {
		try {
			if (nmsItem == null) {
				ConsoleUtil.info(ChatColor.GRAY + "Skipping " + material.name() + " (no item form)");
				return;
			}

			if (VersionUtil.v1_21.isServerVersionHigher()) // DON'T SET BUKKIT MAX STACK
				return;

			setClassField(Material.class, material, "maxStack", size, material.name());
		} catch (Throwable t) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to set " + material.name() + " stack size to: " + size);
			t.printStackTrace();
		}
	}

	public static void applyStackSizeToNmsItem(Object nmsItem, Material material, int size) {
		try {
			if (!SERVER_VERSION.usesDataComponents()) {
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
			return true;
		} catch (Throwable t) {
			ConsoleUtil.warning("Failed to set Bukkit Material max stack of: " + name);
			t.printStackTrace();
			return false;
		}
	}
}