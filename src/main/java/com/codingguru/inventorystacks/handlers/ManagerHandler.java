package com.codingguru.inventorystacks.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.codingguru.inventorystacks.api.PluginManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;

public class ManagerHandler {

	private ManagerHandler() {
	}

	private static ManagerHandler INSTANCE = new ManagerHandler();
	private final Map<Class<?>, PluginManager> managers = new LinkedHashMap<>();

	public <T extends PluginManager> void register(Class<T> clazz, T manager) {
		if (manager != null) {
			managers.put(clazz, manager);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginManager> T get(Class<T> clazz) {
		return (T) managers.get(clazz);
	}

	public void startAll() {
		for (Map.Entry<Class<?>, PluginManager> entry : managers.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				ConsoleUtil.warning("[FakePlayers] Failed to start manager: " + entry.getKey().getSimpleName());
				e.printStackTrace();
			}
		}
	}

	public void stopAll() {
		List<PluginManager> list = new ArrayList<>(managers.values());
		Collections.reverse(list);

		for (PluginManager manager : list) {
			try {
				manager.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static ManagerHandler getInstance() {
		return INSTANCE;
	}

}