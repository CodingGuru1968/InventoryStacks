package com.codingguru.inventorystacks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.LangDefaults;
import com.codingguru.inventorystacks.util.MessageBuilder;

public class ReloadCmd implements CommandExecutor {

	private final InventoryStacks plugin;

	public ReloadCmd(InventoryStacks plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			new MessageBuilder.Builder("incorrect-usage", LangDefaults.INCORRECT_USAGE)
					.set("%command%", "/stacks reload").send(sender);
			return false;
		}

		if (!args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("rl")) {
			new MessageBuilder.Builder("incorrect-usage", LangDefaults.INCORRECT_USAGE)
					.set("%command%", "/stacks reload").send(sender);
			return false;
		}

		if (!sender.hasPermission("STACKS.*") && !sender.hasPermission("STACKS.RELOAD")) {
			new MessageBuilder.Builder("no-permission", LangDefaults.NO_PERMISSION).send(sender);
			return false;
		}

		plugin.reload();
		new MessageBuilder.Builder("reload", LangDefaults.RELOAD).send(sender);
		return false;
	}
}