package com.codingguru.inventorystacks.util;

public enum ServerTypeUtil {

	SPIGOT("c", "c", "c"),
	FOLIA("c", "c", "c"),
	PAPER("MAX_STACK_SIZE", "components", "map");
	// PURPUR,

	private String maxStackField;
	private String itemComponentsField;
	private String componentsMapField;

	ServerTypeUtil(String maxStackField, String itemComponentsField, String componentsMapField) {
		this.maxStackField = maxStackField;
		this.itemComponentsField = itemComponentsField;
		this.componentsMapField = componentsMapField;
	}

	public String getMaxStackField() {
		return maxStackField;
	}

	public String getItemComponentsField() {
		return itemComponentsField;
	}

	public String getComponentsMapField() {
		return componentsMapField;
	}
}