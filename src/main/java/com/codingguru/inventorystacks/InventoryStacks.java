package com.codingguru.inventorystacks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ConsoleUtil;

public class InventoryStacks extends JavaPlugin {

	private static InventoryStacks INSTANCE;

	public void onEnable() {
		INSTANCE = this;

		boolean setupSuccessful = ItemHandler.getInstance().setupServerVersion();

		if (!setupSuccessful) {
			ConsoleUtil.warning(
					"THIS VERSION IS CURRENTLY UNSUPPORTED. PLEASE CONTACT CODINGGURU ON SPIGOT. DISABLING PLUGIN...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig();

		ConsoleUtil.sendPluginStartSetup();

		ItemHandler.getInstance().setupReflectionClasses();
		ItemHandler.getInstance().setupLoadedMaterials();

		if (ItemHandler.getInstance().getLoadedMaterials().isEmpty()) {
			ConsoleUtil.warning("There are currently no materials setup and loaded. Disabling plugin...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		ConsoleUtil.sendPluginEndSetup();
	}

	public static InventoryStacks getInstance() {
		return INSTANCE;
	}

}