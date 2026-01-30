package com.codingguru.inventorystacks.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum MessagesUtil {

	GIVEN_ITEM("Gave %amount% [%item%] to %id%", false),
	HAND_ITEMS_STACKED("&aYou have successfully stacked all items in your hand.", true),
	ALL_ITEMS_STACKED("&aYou have successfully stacked all items in your inventory.", true),
	INVALID_STACK_TYPE("&c%type% is not a valid stack type. Use 'HAND' or 'ALL'.", true),
	DISALLOW_ANVIL_STACK("&cYou cannot use multiple stacked items in an anvil.", true),
	PREVENT_SHIFT_COMBINING_DAMAGEABLE_ITEMS("&cYou cannot combine this item WITH SHIFT due to a durability item glitch.", true),
	COMMAND_DISABLED("&cThis command has been disabled.", false),
	RELOAD("&aYou have successfully reloaded all configuration files.", true),
	INCORRECT_USAGE("&cCorrect Usage: %command%", true),
	IN_GAME("&cYou can only execute this in game.", false),
	NUMBER_EXCEPTION("&cYou must enter a correct amount.", false),
	PLAYER_NOT_FOUND("&cNo entity was found with the id: %id%", false),
	NO_PERMISSION("&cYou do not have permission to execute this command.", true);

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

		if (InventoryStacks.getInstance().getSettingsManager().getLang().isSet(this.getPath())) {
			message = InventoryStacks.getInstance().getSettingsManager().getLang().getString(this.getPath());
		} else {
			message = defaultValue;
		}
		
		if (!InventoryStacks.getInstance().getConfig().getBoolean("use-mini-message")) {
			message = ColorUtil.replace(message);
		}
		
		return message;
	}
	
	public static void broadcast(String message) {
		Bukkit.getOnlinePlayers().stream().forEach(player -> sendMessage(player, message));
	}
	
	public static void sendMiniMessage(CommandSender sender, String replacedString) {
		Audience audience = InventoryStacks.getInstance().getAdventure().sender(sender);
		MiniMessage mm = MiniMessage.miniMessage();
		Component replacedMessage = mm.deserialize(replacedString);
		audience.sendMessage(replacedMessage);
	}

	public static void sendMessage(CommandSender sender, String replacedString) {
		if (replacedString.equalsIgnoreCase(""))
			return;
		
		if (InventoryStacks.getInstance().getConfig().getBoolean("use-mini-message")) {			
			sendMiniMessage(sender, replacedString);
			return;
		}
		
		String[] message = replacedString.split("\\\\n");

		for (String msg : message) {
			sender.sendMessage(msg.replace("\\n", ""));
		}
	}
}