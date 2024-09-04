package com.codingguru.inventorystacks.util;

import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;

public enum MessagesUtil {

	GIVEN_ITEM("Gave %amount% [%item%] to %id%", false),
	HAND_ITEMS_STACKED("&aYou have successfully stacked all items in your hand.", true),
	ALL_ITEMS_STACKED("&aYou have successfully stacked all items in your inventory.", true),
	INVALID_STACK_TYPE("&c%type% is not a valid stack type. Use 'HAND' or 'ALL'.", true),
	DISALLOW_ANVIL_STACK("&cYou cannot use multiple stacked items in an anvil.", true),
	COMMAND_DISABLED("&cThis command has been disabled.", false),
	RELOAD("&aYou have successfully reloaded all configuration files.", true),
	INCORRECT_USAGE("&cCorrect Usage: %command%", true),
	IN_GAME("&cYou can only execute this in game.", false),
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
			message = ColorUtil
					.replace(InventoryStacks.getInstance().getSettingsManager().getLang().getString(this.getPath()));
		} else {
			message = ColorUtil.replace(defaultValue);
		}

		return message;
	}

	public static void sendMessage(CommandSender sender, String replacedString) {
		if (replacedString.equalsIgnoreCase(""))
			return;

		String[] message = replacedString.split("\\\\n");

		for (String msg : message) {
			sender.sendMessage(msg.replace("\\n", ""));
		}
	}
}