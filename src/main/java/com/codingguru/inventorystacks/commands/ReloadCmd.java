package com.codingguru.inventorystacks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.MessagesUtil;

public class ReloadCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			MessagesUtil.sendMessage(sender,
					MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/stacks reload"));
			return false;
		}

		if (!args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("rl")) {
			MessagesUtil.sendMessage(sender,
					MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/stacks reload"));
			return false;
		}

		if (!sender.hasPermission("STACKS.*") && !sender.hasPermission("STACKS.RELOAD")) {
			MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
			return false;
		}

		InventoryStacks.getInstance().reloadConfig();
		ItemHandler.getInstance().reloadInventoryStacks();
		MessagesUtil.sendMessage(sender, MessagesUtil.RELOAD.toString());
		return false;
	}
}
