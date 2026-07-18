package com.codingguru.inventorystacks.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum MessagesUtil {

	GIVEN_ITEM("Gave %amount% [%item%] to %id%"),
	HAND_ITEMS_STACKED("&aYou have successfully stacked all items in your hand."),
	ALL_ITEMS_STACKED("&aYou have successfully stacked all items in your inventory."),
	INVALID_STACK_TYPE("&c%type% is not a valid stack type. Use 'HAND' or 'ALL'."),
	DISALLOW_ANVIL_STACK("&cYou cannot use multiple stacked items in an anvil."),
	PREVENT_SHIFT_COMBINING_DAMAGEABLE_ITEMS("&cYou cannot combine this item WITH SHIFT due to a durability item glitch."),
	COMMAND_DISABLED("&cThis command has been disabled."),
	BUNDLE_FIX("&cYou cannot put stacked damageable items into bundles due to client limitations."),
	RELOAD("&aYou have successfully reloaded all configuration files."),
	INCORRECT_USAGE("&cCorrect Usage: %command%"), 
	IN_GAME("&cYou can only execute this in game."),
	NUMBER_EXCEPTION("&cYou must enter a correct amount."),
	PLAYER_NOT_FOUND("&cNo entity was found with the id: %id%"),
	NO_PERMISSION("&cYou do not have permission to execute this command.");

	private String defaultValue;

	MessagesUtil(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefault() {
		return this.defaultValue;
	}

	public String getPath() {
		return this.name();
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