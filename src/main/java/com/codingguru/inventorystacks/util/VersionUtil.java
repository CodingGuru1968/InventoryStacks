package com.codingguru.inventorystacks.util;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public enum VersionUtil {

    v1_8_R1("net.minecraft.server.v1_8_R1.Item", "net.minecraft.server.v1_8_R1.Items", "maxStackSize", 127, 1),
    v1_8_R2("net.minecraft.server.v1_8_R2.Item", "net.minecraft.server.v1_8_R2.Items", "maxStackSize", 127, 2),
    v1_8_R3("net.minecraft.server.v1_8_R3.Item", "net.minecraft.server.v1_8_R3.Items", "maxStackSize", 127, 3),

    v1_9_R1("net.minecraft.server.v1_9_R1.Item", "net.minecraft.server.v1_9_R1.Items", "maxStackSize", 127, 4),
    v1_9_R2("net.minecraft.server.v1_9_R2.Item", "net.minecraft.server.v1_9_R2.Items", "maxStackSize", 127, 5),

    v1_10_R1("net.minecraft.server.v1_10_R1.Item", "net.minecraft.server.v1_10_R1.Items", "maxStackSize", 127, 6),
    v1_11_R1("net.minecraft.server.v1_11_R1.Item", "net.minecraft.server.v1_11_R1.Items", "maxStackSize", 127, 7),
    v1_12_R1("net.minecraft.server.v1_12_R1.Item", "net.minecraft.server.v1_12_R1.Items", "maxStackSize", 127, 8),

    v1_13_R1("net.minecraft.server.v1_13_R1.Item", "net.minecraft.server.v1_13_R1.Items", "maxStackSize", 127, 9),
    v1_13_R2("net.minecraft.server.v1_13_R2.Item", "net.minecraft.server.v1_13_R2.Items", "maxStackSize", 127, 10),

    v1_14_R1("net.minecraft.server.v1_14_R1.Item", "net.minecraft.server.v1_14_R1.Items", "maxStackSize", 127, 11),
    v1_15_R1("net.minecraft.server.v1_15_R1.Item", "net.minecraft.server.v1_15_R1.Items", "maxStackSize", 127, 12),

    v1_16_R1("net.minecraft.server.v1_16_R1.Item", "net.minecraft.server.v1_16_R1.Items", "maxStackSize", 127, 13),
    v1_16_R2("net.minecraft.server.v1_16_R2.Item", "net.minecraft.server.v1_16_R2.Items", "maxStackSize", 127, 14),
    v1_16_R3("net.minecraft.server.v1_16_R3.Item", "net.minecraft.server.v1_16_R3.Items", "maxStackSize", 127, 15),

    v1_17_R1("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "c", 127, 16),
    v1_18_R1("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 127, 17),
    v1_18_R2("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 127, 18),

    v1_19_R1("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 127, 19),
    v1_19_R2("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 127, 20),
    v1_19_R3("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 127, 21),

    v1_20("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "d", 99, 22),

    v1_21("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", null, 99, 23);

    private final String itemClass;
    private final String itemsClass;
    private final String legacyStackField;
    private final int absoluteMaxStackSize;
    private final int value;

    VersionUtil(String itemClass,
            String itemsClass,
            String legacyStackField,
            int absoluteMaxStackSize,
            int value) {
        this.itemClass = itemClass;
        this.itemsClass = itemsClass;
        this.legacyStackField = legacyStackField;
        this.absoluteMaxStackSize = absoluteMaxStackSize;
        this.value = value;
    }

    public String getItemClass() {
        return itemClass;
    }

    public String getItemsClass() {
        return itemsClass;
    }

    /** Only valid for <= 1.20.4 */
    public String getLegacyStackField() {
        return legacyStackField;
    }

    public int getAbsoluteMaxStackSize() {
        return absoluteMaxStackSize;
    }

    public int getValue() {
        return value;
    }
    
	public boolean isServerVersionHigher() {
		return ItemHandler.getInstance().getServerVersion().getValue() >= getValue();
	}

    /** True if this version requires data components */
    public boolean usesDataComponents() {
        return legacyStackField == null;
    }
}