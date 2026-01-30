package com.codingguru.trailpaths.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import com.codingguru.trailpaths.TrailPaths;

@SuppressWarnings("deprecation")
public class ConsoleUtil {

	private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

	public static void sendPluginSetup() {
		boolean isUpdateAvailable = new UpdateUtil().hasNewUpdate();
		CONSOLE.sendMessage(ChatColor.GREEN + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		CONSOLE.sendMessage(ChatColor.GREEN + "Plugin Name: " + ChatColor.YELLOW + "TrailPaths");
		CONSOLE.sendMessage(ChatColor.GREEN + "Plugin Version: " + ChatColor.YELLOW
				+ TrailPaths.getInstance().getDescription().getVersion());
		CONSOLE.sendMessage(ChatColor.GREEN + "Server Version: " + ChatColor.YELLOW + Bukkit.getBukkitVersion());
		CONSOLE.sendMessage(ChatColor.GREEN + "Server Type: " + ChatColor.YELLOW
				+ TrailPaths.getInstance().getServerType().toString());
		CONSOLE.sendMessage(ChatColor.GREEN + "Author: " + ChatColor.YELLOW + "CodingGuru");
		CONSOLE.sendMessage(ChatColor.GREEN + "Discord: " + ChatColor.YELLOW + "https://discord.gg/CbJxH5NPvX");
		CONSOLE.sendMessage(ChatColor.GREEN + "Updates: " + ChatColor.YELLOW
				+ (isUpdateAvailable ? ChatColor.YELLOW + "A new update was found! Please update this version."
						: ChatColor.YELLOW + "No new updates were found for this plugin."));
		CONSOLE.sendMessage(ChatColor.GREEN + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}

	public static void info(String message) {
		CONSOLE.sendMessage(message);
	}

	public static void warning(String message) {
		CONSOLE.sendMessage("[WARNING] " + message);
	}
}
