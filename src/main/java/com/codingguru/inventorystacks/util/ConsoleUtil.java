package com.codingguru.inventorystacks.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;

@SuppressWarnings("deprecation")
public class ConsoleUtil {

	private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

	public static void sendPluginStartSetup() {
		boolean isUpdateAvailable = new UpdateUtil().hasNewUpdate();
		CONSOLE.sendMessage(ChatColor.GREEN + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		CONSOLE.sendMessage(ChatColor.GREEN + "Plugin Name: " + ChatColor.YELLOW + "InventoryStacks");
		CONSOLE.sendMessage(ChatColor.GREEN + "Plugin Version: " + ChatColor.YELLOW
				+ InventoryStacks.getInstance().getDescription().getVersion());
		CONSOLE.sendMessage(ChatColor.GREEN + "Server Version: " + ChatColor.YELLOW + Bukkit.getBukkitVersion());
		CONSOLE.sendMessage(ChatColor.GREEN + "Loaded Version: " + ChatColor.YELLOW
				+ ItemHandler.getInstance().getServerVersion().toString());
		CONSOLE.sendMessage(ChatColor.GREEN + "Server Type: " + ChatColor.YELLOW
				+ ItemHandler.getInstance().getServerType().toString());
		CONSOLE.sendMessage(ChatColor.GREEN + "Author: " + ChatColor.YELLOW + "CodingGuru");
		CONSOLE.sendMessage(ChatColor.GREEN + "Discord: " + ChatColor.YELLOW + "https://discord.gg/CbJxH5NPvX");
		CONSOLE.sendMessage(ChatColor.GREEN + "Updates: " + ChatColor.YELLOW
				+ (isUpdateAvailable ? ChatColor.YELLOW + "A new update was found! Please update this version."
						: ChatColor.YELLOW + "No new updates were found for this plugin."));
		CONSOLE.sendMessage("");
		CONSOLE.sendMessage(ChatColor.GREEN + "Loading Materials...");
	}

	public static void sendPluginEndSetup() {
		CONSOLE.sendMessage("");
		CONSOLE.sendMessage(ChatColor.GREEN + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}

	public static void info(String message) {
		CONSOLE.sendMessage(ChatColor.YELLOW + message);
	}

	public static void warning(String message) {
		CONSOLE.sendMessage(ChatColor.RED + message);
	}
}
