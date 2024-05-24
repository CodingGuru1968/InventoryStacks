package com.codingguru.inventorystacks.util;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public enum VersionUtil {

	v1_7("maxStackSize", 1),
	v1_8("maxStackSize", 2),
	v1_9("maxStackSize", 3),
	v1_10("maxStackSize", 4),
	v1_11("maxStackSize", 5),
	v1_12("maxStackSize", 6),
	v1_13("maxStackSize", 7),
	v1_14("maxStackSize", 8),
	v1_15("maxStackSize", 9),
	v1_16("maxStackSize", 10),
	v1_17("c", 11),
	v1_18("d", 12),
	v1_19("d", 13),
	v1_20("d", 14);

	private final String fieldName;
	private final int value;

	VersionUtil(String fieldName, int value) {
		this.fieldName = fieldName;
		this.value = value;
	}

	public boolean isServerVersionHigher() {
		return ItemHandler.getInstance().getServerVersion().getValue() >= getValue();
	}

	public int getValue() {
		return value;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public String toString() {
		return name();
	}
}