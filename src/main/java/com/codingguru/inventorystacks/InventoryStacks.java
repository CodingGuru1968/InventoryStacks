package com.codingguru.inventorystacks;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.inventorystacks.commands.ReloadCmd;
import com.codingguru.inventorystacks.commands.StackCmd;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.handlers.ManagerHandler;
import com.codingguru.inventorystacks.hooks.WorldGuardHook;
import com.codingguru.inventorystacks.listeners.correction.BlockDispense;
import com.codingguru.inventorystacks.listeners.correction.BundleFix;
import com.codingguru.inventorystacks.listeners.correction.FurnaceBurn;
import com.codingguru.inventorystacks.listeners.correction.InventoryClick;
import com.codingguru.inventorystacks.listeners.correction.InventoryMoveItem;
import com.codingguru.inventorystacks.listeners.correction.PlayerBucketEmpty;
import com.codingguru.inventorystacks.listeners.correction.PlayerInteract;
import com.codingguru.inventorystacks.listeners.correction.PlayerItemConsume;
import com.codingguru.inventorystacks.listeners.general.AnvilStack;
import com.codingguru.inventorystacks.listeners.general.BlockPlace;
import com.codingguru.inventorystacks.listeners.general.Commands;
import com.codingguru.inventorystacks.listeners.general.DroppedItemMerge;
import com.codingguru.inventorystacks.listeners.general.ItemHologram;
import com.codingguru.inventorystacks.listeners.general.PlayerItemDamage;
import com.codingguru.inventorystacks.listeners.itemmeta.UpdateItemMeta;
import com.codingguru.inventorystacks.managers.ItemHologramManager;
import com.codingguru.inventorystacks.managers.LanguageManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.tchristofferson.configupdater.ConfigUpdater;

public class InventoryStacks extends JavaPlugin {

	private static InventoryStacks INSTANCE;

	public void onEnable() {
		INSTANCE = this;

		ConsoleUtil.sendPluginStartSetup();

		saveDefaultConfig();

		try {
			ConfigUpdater.update(this, "config.yml", new File(getDataFolder(), "config.yml"),  "items");
		} catch (IOException e) {
			e.printStackTrace();
		}

		reloadConfig();
		
		if (!ItemHandler.getInstance().setup())
			return;
		
		WorldGuardHook.setupWorldGuard();

		registerManagers();

		ManagerHandler.getInstance().startAll();

		registerHooksAndListeners();

		ConsoleUtil.sendPluginEndSetup();
	}

	public void onDisable() {
		ManagerHandler.getInstance().stopAll();
	}

	public void reload() {
		ManagerHandler.getInstance().stopAll();
		reloadConfig();
		WorldGuardHook.setupWorldGuard();
		ManagerHandler.getInstance().startAll();
		ItemHandler.getInstance().reloadInventoryStacks();
	}
	
	private void registerManagers() {
		ManagerHandler managerRegistry = ManagerHandler.getInstance();
		managerRegistry.register(LanguageManager.class, new LanguageManager(this));
		managerRegistry.register(ItemHologramManager.class, new ItemHologramManager(this));
	}

	private void registerHooksAndListeners() {
		getCommand("stack").setExecutor(new StackCmd(this));
		getCommand("stacks").setExecutor(new ReloadCmd(this));
		getCommand("inventorystacks").setExecutor(new ReloadCmd(this));
		
		long itemChangeDelay = getConfig().getLong("item-change-delay", 2L);

		getServer().getPluginManager().registerEvents(new Commands(), this);
		getServer().getPluginManager().registerEvents(new PlayerItemDamage(this, itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new BlockPlace(this, itemChangeDelay), this);
		getServer().getPluginManager().registerEvents(new ItemHologram(), this);
		getServer().getPluginManager().registerEvents(new DroppedItemMerge(this), this);
		getServer().getPluginManager().registerEvents(new BundleFix(), this);
		getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
		getServer().getPluginManager().registerEvents(new AnvilStack(this), this);

		if (ItemHandler.getInstance().isUsingModernAPI()) {
			getServer().getPluginManager().registerEvents(new UpdateItemMeta(this), this);
		} else { // LEGACY SUPPORT
			getServer().getPluginManager().registerEvents(new PlayerBucketEmpty(this, itemChangeDelay), this);
			getServer().getPluginManager().registerEvents(new PlayerItemConsume(this, itemChangeDelay), this);
			getServer().getPluginManager().registerEvents(new InventoryMoveItem(this), this);
			getServer().getPluginManager().registerEvents(new FurnaceBurn(), this);
			getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
			getServer().getPluginManager().registerEvents(new BlockDispense(), this);
		}
	}

	public static InventoryStacks getInstance() {
		return INSTANCE;
	}

}