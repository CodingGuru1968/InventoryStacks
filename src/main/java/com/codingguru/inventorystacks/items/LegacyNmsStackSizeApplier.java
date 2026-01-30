package com.codingguru.inventorystacks.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.ReflectionLegacyUtil;

public class LegacyNmsStackSizeApplier extends StackSizeApplier {

	@Override
	public void setup() {
		ReflectionLegacyUtil.setup();
	}

	@Override
	public void apply(ItemStack stack, int size) {
		Material material = stack.getType();
		Object nmsItem = ReflectionLegacyUtil.hasItemForm(material);

		if (nmsItem == null)
			return;

		ReflectionLegacyUtil.applyStackSizeToMaterial(nmsItem, material, size);
		ReflectionLegacyUtil.applyStackSizeToNmsItem(nmsItem, material, size);
	}

	@Override
	public boolean isModernApi() {
		return false;
	}
}
