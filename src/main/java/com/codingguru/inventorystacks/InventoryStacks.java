package com.codingguru.inventorystacks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.inventorystacks.commands.ReloadCmd;
import com.codingguru.inventorystacks.commands.StackCmd;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.listeners.Commands;
import com.codingguru.inventorystacks.listeners.InventoryClick;
import com.codingguru.inventorystacks.listeners.PlayerInteract;
import com.codingguru.inventorystacks.listeners.PlayerItemConsume;
import com.codingguru.inventorystacks.listeners.PlayerItemDamage;
import com.codingguru.inventorystacks.managers.SettingsManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;

public class InventoryStacks extends JavaPlugin {

	private static InventoryStacks INSTANCE;
	private SettingsManager settingsManager;

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

		getCommand("stack").setExecutor(new StackCmd());
		getCommand("stacks").setExecutor(new ReloadCmd());
		getCommand("inventorystacks").setExecutor(new ReloadCmd());
		
		getServer().getPluginManager().registerEvents(new InventoryClick(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsume(), this);
		getServer().getPluginManager().registerEvents(new PlayerItemDamage(), this);
		getServer().getPluginManager().registerEvents(new Commands(), this);

		settingsManager = new SettingsManager();
		settingsManager.setup(this);

		ItemHandler.getInstance().setupReflectionClasses();
		ItemHandler.getInstance().setupLoadedMaterials();

		ConsoleUtil.sendPluginEndSetup();
	}
	
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public static InventoryStacks getInstance() {
		return INSTANCE;
	}

}