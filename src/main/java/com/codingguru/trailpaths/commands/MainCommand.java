package com.codingguru.trailpaths.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.codingguru.trailpaths.TrailPaths;
import com.codingguru.trailpaths.handlers.PathHandler;
import com.codingguru.trailpaths.utils.MessagesUtil;

public class MainCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("trails")) {
			if (args.length == 0) {
				if (sender instanceof ConsoleCommandSender) {
					MessagesUtil.sendMessage(sender, MessagesUtil.IN_GAME_ONLY.toString());
					return false;
				}

				Player player = (Player) sender;

				if (PathHandler.getInstance().isPathDisabled(player.getUniqueId())) {
					togglePath((Player) sender, true);
				} else {
					togglePath((Player) sender, false);
				}

				return false;
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission("TRAILS.*") && !sender.hasPermission("TRAILS.HELP")) {
						MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
						return false;
					}

					MessagesUtil.sendMessage(sender, MessagesUtil.HELP_TITLE.toString());
					MessagesUtil.sendMessage(sender,
							MessagesUtil.HELP_COMMAND.toString().replaceAll("%command%", "/trails help")
									.replaceAll("%description%", "View plugin commands."));
					MessagesUtil.sendMessage(sender,
							MessagesUtil.HELP_COMMAND.toString().replaceAll("%command%", "/trails reload")
									.replaceAll("%description%", "Reload the trails plugin."));
					MessagesUtil.sendMessage(sender,
							MessagesUtil.HELP_COMMAND.toString().replaceAll("%command%", "/trails [on | off]")
									.replaceAll("%description%", "Toggle your trail on or off."));
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("TRAILS.*") && !sender.hasPermission("TRAILS.RELOAD")) {
						MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
						return false;
					}

					TrailPaths.getInstance().getSettingsManager().setup(TrailPaths.getInstance());
					TrailPaths.getInstance().reloadConfig();
					PathHandler.getInstance().resetMaterials();
					MessagesUtil.sendMessage(sender, MessagesUtil.RELOAD.toString());
				} else if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enabled")
						|| args[0].equalsIgnoreCase("enable")) {
					if (sender instanceof ConsoleCommandSender) {
						MessagesUtil.sendMessage(sender, MessagesUtil.IN_GAME_ONLY.toString());
						return false;
					}

					if (!sender.hasPermission("TRAILS.*") && !sender.hasPermission("TRAILS.TOGGLE")) {
						MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
						return false;
					}

					togglePath((Player) sender, true);
				} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disabled")
						|| args[0].equalsIgnoreCase("disable")) {
					if (sender instanceof ConsoleCommandSender) {
						MessagesUtil.sendMessage(sender, MessagesUtil.IN_GAME_ONLY.toString());
						return false;
					}

					if (!sender.hasPermission("TRAILS.*") && !sender.hasPermission("TRAILS.TOGGLE")) {
						MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
						return false;
					}

					togglePath((Player) sender, false);
				} else {
					MessagesUtil.sendMessage(sender,
							MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/trails"));
				}
				return false;
			} else {
				MessagesUtil.sendMessage(sender,
						MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/trails"));
				return false;
			}
		}
		return false;
	}

	private void togglePath(Player player, boolean enabled) {
		if (enabled) {
			MessagesUtil.sendMessage(player, MessagesUtil.TOGGLE_PATH_ON.toString());
			PathHandler.getInstance().enablePath(player.getUniqueId());
		} else {
			MessagesUtil.sendMessage(player, MessagesUtil.TOGGLE_PATH_OFF.toString());
			PathHandler.getInstance().disablePath(player.getUniqueId());
		}
	}
}