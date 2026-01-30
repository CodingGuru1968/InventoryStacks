package com.codingguru.trailpaths;

import org.bukkit.plugin.java.JavaPlugin;

import com.codingguru.trailpaths.commands.MainCommand;
import com.codingguru.trailpaths.listeners.PlayerMove;
import com.codingguru.trailpaths.listeners.PlayerQuit;
import com.codingguru.trailpaths.managers.SettingsManager;
import com.codingguru.trailpaths.utils.ConsoleUtil;
import com.codingguru.trailpaths.utils.ServerTypeUtil;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class TrailPaths extends JavaPlugin {

	private static TrailPaths INSTANCE;
	private SettingsManager settingsManager;
	private BukkitAudiences adventureAPI;
	private ServerTypeUtil serverType;

	public void onEnable() {
		INSTANCE = this;

		setupServerType();
		
		ConsoleUtil.sendPluginSetup();

		saveDefaultConfig();

		settingsManager = new SettingsManager();
		settingsManager.setup(this);
		
		if (getConfig().getBoolean("use-mini-message")) {
			this.adventureAPI = BukkitAudiences.create(this);
		}

		getCommand("trails").setExecutor(new MainCommand());

		getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
		getServer().getPluginManager().registerEvents(new PlayerMove(), this);
	}
	
	private void setupServerType() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			serverType = ServerTypeUtil.FOLIA;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		try {
			Class.forName("io.papermc.paper.ServerBuildInfo");
			serverType = ServerTypeUtil.PAPER;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		serverType = ServerTypeUtil.SPIGOT;
	}
	
	public ServerTypeUtil getServerType() {
		return serverType;
	}
	
	public BukkitAudiences getAdventure() {
		return this.adventureAPI;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public static TrailPaths getInstance() {
		return INSTANCE;
	}

}