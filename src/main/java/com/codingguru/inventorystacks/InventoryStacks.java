package com.codingguru.inventorystacks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.inventorystacks.commands.ReloadCmd;
import com.codingguru.inventorystacks.commands.StackCmd;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.listeners.BlockPlace;
import com.codingguru.inventorystacks.listeners.Commands;
import com.codingguru.inventorystacks.listeners.FurnaceBurn;
import com.codingguru.inventorystacks.listeners.InventoryClick;
import com.codingguru.inventorystacks.listeners.InventoryMoveItem;
import com.codingguru.inventorystacks.listeners.PlayerBucketEmpty;
import com.codingguru.inventorystacks.listeners.PlayerInteract;
import com.codingguru.inventorystacks.listeners.PlayerItemConsume;
import com.codingguru.inventorystacks.listeners.PlayerItemDamage;
import com.codingguru.inventorystacks.managers.SettingsManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class InventoryStacks extends JavaPlugin {

	private static InventoryStacks INSTANCE;
	private SettingsManager settingsManager;
	private BukkitAudiences adventureAPI;

	public void onEnable() {
		INSTANCE = this;

		boolean setupSuccessful = ItemHandler.getInstance().setupServerVersion();

		if (!setupSuccessful) {
			String packageVersion = Bukkit.getServer().getClass().getPackage().getName();
			String versionFound = packageVersion.substring(packageVersion.lastIndexOf('.') + 1);
			ConsoleUtil.warning("THE VERSION: " + versionFound
					+ " IS CURRENTLY UNSUPPORTED. PLEASE CONTACT CODINGGURU ON SPIGOT. DISABLING PLUGIN...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig();

		ConsoleUtil.sendPluginStartSetup();

		getCommand("stack").setExecutor(new StackCmd());
		getCommand("stacks").setExecutor(new ReloadCmd());
		getCommand("inventorystacks").setExecutor(new ReloadCmd());

		long itemChangeDelay = InventoryStacks.getInstance().getConfig().getLong("item-change-delay", 2L);

		getServer().getPluginManager().registerEvents(new PlayerBucketEmpty(itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsume(itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new PlayerItemDamage(itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new BlockPlace(itemChangeDelay), this);

		getServer().getPluginManager().registerEvents(new InventoryClick(), this);
		getServer().getPluginManager().registerEvents(new Commands(), this);
		getServer().getPluginManager().registerEvents(new InventoryMoveItem(), this);
		getServer().getPluginManager().registerEvents(new FurnaceBurn(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(), this);

		settingsManager = new SettingsManager();
		settingsManager.setup(this);

		if (getConfig().getBoolean("use-mini-message")) {
			this.adventureAPI = BukkitAudiences.create(this);
		}

		ItemHandler.getInstance().setupReflectionClasses();
		ItemHandler.getInstance().setupLoadedMaterials();

		ConsoleUtil.sendPluginEndSetup();
	}

	public BukkitAudiences getAdventure() {
		return this.adventureAPI;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public static InventoryStacks getInstance() {
		return INSTANCE;
	}

}