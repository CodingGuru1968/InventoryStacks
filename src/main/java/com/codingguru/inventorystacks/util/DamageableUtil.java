package com.codingguru.inventorystacks.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public final class DamageableUtil {

	private static final Class<?> DAMAGEABLE_CLASS = findDamageableClass();

	private DamageableUtil() {
	}

	private static Class<?> findDamageableClass() {
		try {
			return Class.forName("org.bukkit.inventory.meta.Damageable");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static boolean isDamageable(Material material) {
		if (material == null) {
			return false;
		}

		if (DAMAGEABLE_CLASS == null) {
			return material.getMaxDurability() > 0;
		}

		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
		return meta != null && DAMAGEABLE_CLASS.isInstance(meta);
	}
}