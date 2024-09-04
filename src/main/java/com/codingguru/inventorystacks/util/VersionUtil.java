package com.codingguru.inventorystacks.util;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public enum VersionUtil {

	v1_7_R1("maxStackSize", 127, 1),
	v1_7_R2("maxStackSize", 127, 2),
	v1_7_R3("maxStackSize", 127, 3),
	v1_7_R4("maxStackSize", 127, 4),

	v1_8_R1("maxStackSize", 127, 5),
	v1_8_R2("maxStackSize", 127, 6),
	v1_8_R3("maxStackSize", 127, 7),

	v1_9_R1("maxStackSize", 127, 8),
	v1_9_R2("maxStackSize", 127, 9),

	v1_10_R1("maxStackSize", 127, 10),

	v1_11_R1("maxStackSize", 127, 11),

	v1_12_R1("maxStackSize", 127, 12),

	v1_13_R1("maxStackSize", 127, 13),
	v1_13_R2("maxStackSize", 127, 14),

	v1_14_R1("maxStackSize", 127, 15),

	v1_15_R1("maxStackSize", 127, 16),

	v1_16_R1("maxStackSize", 127, 17),
	v1_16_R2("maxStackSize", 127, 18),
	v1_16_R3("maxStackSize", 127, 19),

	v1_17_R1("c", 127, 20),

	v1_18_R1("d", 127, 21),
	v1_18_R2("d", 127, 22),

	v1_19_R1("d", 127, 23),
	v1_19_R2("d", 127, 24),
	v1_19_R3("d", 127, 25),

	v1_20_R1("d", 127, 26),
	v1_20_R2("d", 127, 27),
	v1_20_R3("d", 127, 28),

	v1_20_R4("", 99, 29),

	v1_21_R1("", 99, 30),
	v1_21_R2("", 99, 31);

	private final String fieldName;
	private final int value;
	private final int absoluteMaxStackSize;

	VersionUtil(String fieldName, int absoluteMaxStackSize, int value) {
		this.fieldName = fieldName;
		this.value = value;
		this.absoluteMaxStackSize = absoluteMaxStackSize;
	}

	public boolean isServerVersionHigher() {
		return ItemHandler.getInstance().getServerVersion().getValue() >= getValue();
	}

	public int getAbsoluteMaxStackSize() {
		return absoluteMaxStackSize;
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