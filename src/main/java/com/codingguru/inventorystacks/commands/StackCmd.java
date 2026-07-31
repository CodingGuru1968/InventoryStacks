package com.codingguru.inventorystacks.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.LangDefaults;
import com.codingguru.inventorystacks.util.MessageBuilder;
import com.codingguru.inventorystacks.util.VersionUtil;

public class StackCmd implements CommandExecutor {

	private final InventoryStacks plugin;

	public StackCmd(InventoryStacks plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!plugin.getConfig().getBoolean("stack-command.enabled")) {
			new MessageBuilder.Builder("command-disabled", LangDefaults.COMMAND_DISABLED).send(sender);
			return false;
		}

		if (sender instanceof ConsoleCommandSender) {
			new MessageBuilder.Builder("in-game-only", LangDefaults.IN_GAME_ONLY).send(sender);
			return false;
		}

		if (args.length == 0) {
			if (!sender.hasPermission("STACKS.*") && !sender.hasPermission("STACKS.COMMAND")) {
				new MessageBuilder.Builder("no-permission", LangDefaults.NO_PERMISSION).send(sender);
				return false;
			}

			String defaultStackType = plugin.getConfig().isSet("stack-command.defualt-stack-type")
					? plugin.getConfig().getString("stack-command.defualt-stack-type")
					: plugin.getConfig().getString("stack-command.default-stack-type");

			StackType stackType = StackType.getStackTypeFromName(defaultStackType);

			if (stackType == null) {
				new MessageBuilder.Builder("invalid-stack-type", LangDefaults.INVALID_STACK_TYPE)
						.set("%type%", defaultStackType).send(sender);
				return false;
			}

			stack((Player) sender, stackType);
		} else if (args.length == 1) {
			StackType stackType = StackType.getStackTypeFromName(args[0]);

			if (stackType == null) {
				new MessageBuilder.Builder("invalid-stack-type", LangDefaults.INVALID_STACK_TYPE).set("%type%", args[0])
						.send(sender);
				return false;
			}

			stack((Player) sender, stackType);
		} else {
			new MessageBuilder.Builder("incorrect-usage", LangDefaults.INCORRECT_USAGE).set("%command%", "/stack")
					.send(sender);
		}
		return false;
	}

	private void stack(Player player, StackType stackType) {
		switch (stackType) {
		case HAND:
			stackHand(player);
			new MessageBuilder.Builder("hand-items-stacked", LangDefaults.HAND_ITEMS_STACKED).send(player);
			break;
		case ALL:
			new MessageBuilder.Builder("all-items-stacked", LangDefaults.ALL_ITEMS_STACKED).send(player);
			stackAllItems(player);
			break;
		}
	}

	@SuppressWarnings("deprecation")
	public ItemStack getItemInHand(Player player) {
		if (!VersionUtil.v1_9_R1.isServerVersionHigher()) {
			return player.getInventory().getItemInHand();
		}
		return player.getInventory().getItemInMainHand();
	}

	private void stackHand(Player player) {
		ItemStack item = getItemInHand(player).clone();
		ItemStack[] items = player.getInventory().getContents();
		int amount = item.getAmount();
		int maxAmount = getMaxStack(item);

		for (int slot = 0; slot < items.length; slot++) {
			ItemStack foundItem = items[slot];

			if (foundItem == null || foundItem.getType() == Material.AIR || foundItem.getAmount() <= 0)
				continue;

			if (foundItem.isSimilar(item) && slot != player.getInventory().getHeldItemSlot()) {
				amount = amount + foundItem.getAmount();
				player.getInventory().clear(slot);
			}
		}

		if (amount <= maxAmount) {
			getItemInHand(player).setAmount(amount);
		} else {
			item.setAmount(maxAmount);
			getItemInHand(player).setAmount(maxAmount);

			for (int i = amount - maxAmount; i >= maxAmount; i -= maxAmount) {
				if (player.getInventory().firstEmpty() != -1) {
					player.getInventory().setItem(player.getInventory().firstEmpty(), item);
				} else {
					player.getWorld().dropItem(player.getLocation(), item);
				}
			}

			if (amount % maxAmount > 0) {
				item.setAmount(amount % maxAmount);
				if (player.getInventory().firstEmpty() != -1) {
					player.getInventory().setItem(player.getInventory().firstEmpty(), item);
				} else {
					player.getWorld().dropItem(player.getLocation(), item);
				}
			}
		}
	}

	private void stackAllItems(Player player) {
		ItemStack[] items = player.getInventory().getContents();
		int inventorySize = items.length;
		int affectedItems = 0;

		for (int i = 0; i < inventorySize; i++) {
			ItemStack item = items[i];

			if (item == null || item.getAmount() <= 0)
				continue;

			int max = getMaxStack(item);

			if (item.getAmount() < max) {
				int neededUntilMax = max - item.getAmount();
				for (int j = i + 1; j < inventorySize; j++) {
					ItemStack item2 = items[j];

					if (item2 == null || item2.getAmount() <= 0)
						continue;

					if (item2.isSimilar(item)) {
						if (item2.getAmount() > neededUntilMax) {
							item.setAmount(max);
							item2.setAmount(item2.getAmount() - neededUntilMax);
							break;
						} else {
							items[j] = null;
							item.setAmount(item.getAmount() + item2.getAmount());
							neededUntilMax = max - item.getAmount();
						}

						affectedItems++;
					}
				}
			}
		}

		if (affectedItems > 0) {
			player.getInventory().setContents(items);
		}
	}

	private int getMaxStack(ItemStack stack) {
		if (!ItemHandler.getInstance().isUsingModernAPI())
			return stack.getType().getMaxStackSize();

		ItemMeta currentMeta = stack.getItemMeta();

		if (currentMeta == null || !currentMeta.hasMaxStackSize())
			return stack.getMaxStackSize();

		return currentMeta.getMaxStackSize();
	}

	private enum StackType {
		HAND, ALL;

		private static StackType getStackTypeFromName(String name) {
			for (StackType type : values()) {
				if (type.toString().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return null;
		}
	}
}