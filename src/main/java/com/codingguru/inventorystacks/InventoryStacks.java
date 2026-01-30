package com.codingguru.inventorystacks;

import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.inventorystacks.commands.ReloadCmd;
import com.codingguru.inventorystacks.commands.StackCmd;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.listeners.correction.BlockDispense;
import com.codingguru.inventorystacks.listeners.correction.FurnaceBurn;
import com.codingguru.inventorystacks.listeners.correction.InventoryClick;
import com.codingguru.inventorystacks.listeners.correction.InventoryMoveItem;
import com.codingguru.inventorystacks.listeners.correction.PlayerBucketEmpty;
import com.codingguru.inventorystacks.listeners.correction.PlayerInteract;
import com.codingguru.inventorystacks.listeners.correction.PlayerItemConsume;
import com.codingguru.inventorystacks.listeners.general.BlockPlace;
import com.codingguru.inventorystacks.listeners.general.Commands;
import com.codingguru.inventorystacks.listeners.general.PlayerItemDamage;
import com.codingguru.inventorystacks.listeners.itemmeta.UpdateItemMetaListener;
import com.codingguru.inventorystacks.managers.SettingsManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class InventoryStacks extends JavaPlugin {

	private static InventoryStacks INSTANCE;
	private SettingsManager settingsManager;
	private BukkitAudiences adventureAPI;

	public void onEnable() {
		INSTANCE = this;

		ConsoleUtil.sendPluginStartSetup();

		saveDefaultConfig();
		
		ItemHandler.getInstance().setup();

		getCommand("stack").setExecutor(new StackCmd());
		getCommand("stacks").setExecutor(new ReloadCmd());
		getCommand("inventorystacks").setExecutor(new ReloadCmd());

		settingsManager = new SettingsManager();
		settingsManager.setup(this);

		if (getConfig().getBoolean("use-mini-message")) {
			this.adventureAPI = BukkitAudiences.create(this);
		}
		
		long itemChangeDelay = InventoryStacks.getInstance().getConfig().getLong("item-change-delay", 2L);

		getServer().getPluginManager().registerEvents(new Commands(), this);
		getServer().getPluginManager().registerEvents(new PlayerItemDamage(itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new BlockPlace(itemChangeDelay), this);

		if (ItemHandler.getInstance().isUsingModernAPI()) {
			getServer().getPluginManager().registerEvents(new UpdateItemMetaListener(), this);
		} else { // LEGACY SUPPORT
			getServer().getPluginManager().registerEvents(new PlayerBucketEmpty(itemChangeDelay), this);
			getServer().getPluginManager().registerEvents(new PlayerItemConsume(itemChangeDelay), this);
			getServer().getPluginManager().registerEvents(new InventoryClick(), this);
			getServer().getPluginManager().registerEvents(new InventoryMoveItem(), this);
			getServer().getPluginManager().registerEvents(new FurnaceBurn(), this);
			getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
			getServer().getPluginManager().registerEvents(new BlockDispense(), this);
		}

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