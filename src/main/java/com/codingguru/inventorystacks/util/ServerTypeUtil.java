package com.codingguru.inventorystacks.util;

public enum ServerTypeUtil {
	
	SPIGOT("MAX_STACK_SIZE", "components", "map"), 
	PAPER("MAX_STACK_SIZE", "components", "map"),
	FOLIA("MAX_STACK_SIZE", "components", "map");

	private final String dataComponentKeyField;
	private final String itemComponentsField;
	private final String internalMapField;

	ServerTypeUtil(String key, String components, String map) {
		this.dataComponentKeyField = key;
		this.itemComponentsField = components;
		this.internalMapField = map;
	}

	public String getDataComponentKeyField() {
		return dataComponentKeyField;
	}

	public String getItemComponentsField() {
		return itemComponentsField;
	}

	public String getInternalMapField() {
		return internalMapField;
	}
}
