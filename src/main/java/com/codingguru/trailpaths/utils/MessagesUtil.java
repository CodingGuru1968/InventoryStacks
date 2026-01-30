package com.codingguru.trailpaths.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.codingguru.trailpaths.TrailPaths;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum MessagesUtil {

	HELP_TITLE("&c&lTrailPaths &r&7- Help Menu", false),
	HELP_COMMAND("&c%command% &7- %description%", false),
	TOGGLE_PATH_ON("&eYou have toggled your trail paths: &aON", true),
	TOGGLE_PATH_OFF("&eYou have toggled your trail paths: &cOFF", true),
	RELOAD("&aYou have successfully reloaded all configuration files.", true),
	NO_PERMISSION("&cYou do not have permission to execute this command.", true),
	IN_GAME_ONLY("&cThis command can only be executed in game.", true),
	INCORRECT_USAGE("&cCorrect Usage: %command%", true);

	private String defaultValue;
	private boolean usePrefix;

	MessagesUtil(String defaultValue, boolean usePrefix) {
		this.defaultValue = defaultValue;
		this.usePrefix = usePrefix;
	}

	public String getDefault() {
		return this.defaultValue;
	}

	public String getPath() {
		return this.name();
	}

	public boolean usePrefix() {
		return this.usePrefix;
	}

	@Override
	public String toString() {
		String message;

		if (TrailPaths.getInstance().getSettingsManager().getLang().isSet(this.getPath())) {
			message = TrailPaths.getInstance().getSettingsManager().getLang().getString(this.getPath());
		} else {
			message = defaultValue;
		}
		
		if (!TrailPaths.getInstance().getConfig().getBoolean("use-mini-message")) {
			message = ColorUtil.replace(message);
		}

		return message;
	}

	public static void broadcast(String message) {
		Bukkit.getOnlinePlayers().stream().forEach(player -> sendMessage(player, message));
	}
	
	public static void sendMiniMessage(CommandSender sender, String replacedString) {
		Audience audience = TrailPaths.getInstance().getAdventure().sender(sender);
		MiniMessage mm = MiniMessage.miniMessage();
		Component replacedMessage = mm.deserialize(replacedString);
		audience.sendMessage(replacedMessage);
	}

	public static void sendMessage(CommandSender sender, String replacedString) {
		if (replacedString.equalsIgnoreCase(""))
			return;

		if (TrailPaths.getInstance().getConfig().getBoolean("use-mini-message")) {
			sendMiniMessage(sender, replacedString);
			return;
		}

		String[] message = replacedString.split("\\\\n");

		for (String msg : message) {
			sender.sendMessage(msg.replace("\\n", ""));
		}
	}
}