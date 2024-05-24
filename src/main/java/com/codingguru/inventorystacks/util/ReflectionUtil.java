package com.codingguru.inventorystacks.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public final class ReflectionUtil {

	public static boolean setClassField(Class<?> clas, Object instance, String fieldName, int value) {
		Field currentField;

		try {
			currentField = clas.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return false;
		}

		currentField.setAccessible(true);

		try {
			currentField.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
			return false;
		}

		currentField.setAccessible(false);
		return true;
	}

	public static boolean setItemField(String materialName, int stack) {
		Field itemField;

		try {
			itemField = VersionUtil.v1_17.isServerVersionHigher() ? locateCorrectItemField(materialName)
					: ItemHandler.getInstance().getItemsClass().getDeclaredField(materialName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return false;
		}

		Object item;

		try {
			item = itemField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		return setClassField(ItemHandler.getInstance().getItemClass(), item,
				ItemHandler.getInstance().getServerVersion().getFieldName(), stack);
	}

	private static Field locateCorrectItemField(String materialName) {
		Method getItemNameMethod = ItemHandler.getInstance().getItemNameMethod();
		Class<?> itemClass = ItemHandler.getInstance().getItemClass();

		for (Field field : ItemHandler.getInstance().getItemsClass().getFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Object found;

			try {
				found = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
				continue;
			}

			if (found == null)
				continue;

			if (!itemClass.isInstance(found))
				continue;

			String name;

			try {
				name = (String) getItemNameMethod.invoke(found);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassCastException e) {
				e.printStackTrace();
				continue;
			}

			String[] split = name.split("\\.");

			name = split[split.length - 1];

			if (name.equalsIgnoreCase(materialName)) {
				return field;
			}
		}
		return null;
	}
}