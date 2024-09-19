package com.codingguru.inventorystacks.listeners;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.RandomUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import com.google.common.collect.Lists;

public class Commands implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (!e.getMessage().startsWith("/give"))
			return;

		String[] command = e.getMessage().split(" ");

		if (command.length < 4)
			return;

		String itemName = command[2].replaceAll("minecraft:", "");

		if (command[2] == null || command[3] == null)
			return;

		int amount = Integer.parseInt(command[3]);

		Optional<XMaterialUtil> item = XMaterialUtil.matchXMaterial(itemName);

		if (item == null || !item.isPresent() || item.get() == null)
			return;

		XMaterialUtil material = item.get();

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(material))
			return;

		if (isDamageable(material.parseMaterial())) {
			e.setCancelled(true);

			List<Player> targetedPlayers = getTargetedPlayers(e.getPlayer(), command[1]);

			if (targetedPlayers.isEmpty()) {
				MessagesUtil.sendMessage(e.getPlayer(),
						MessagesUtil.PLAYER_NOT_FOUND.toString().replaceAll("%id%", command[1]));
				return;
			}

			itemName = itemName.toLowerCase().replaceAll("_", " ");
			String niceName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
			ItemStack itemStack = material.parseItem();
			itemStack.setAmount(amount);
			targetedPlayers.stream().forEach(player -> {
				ItemUtil.addItem(player, itemStack);
				MessagesUtil.sendMessage(e.getPlayer(),
						MessagesUtil.GIVEN_ITEM.toString().replaceAll("%amount%", amount + "")
								.replaceAll("%item%", niceName).replaceAll("%id%", player.getName()));
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommand(ServerCommandEvent e) {
		if (!e.getCommand().startsWith("give"))
			return;

		String[] command = e.getCommand().split(" ");

		if (command.length < 4)
			return;

		String itemName = command[2].replaceAll("minecraft:", "");

		if (command[2] == null || command[3] == null)
			return;

		int amount = Integer.parseInt(command[3]);

		Optional<XMaterialUtil> item = XMaterialUtil.matchXMaterial(itemName);

		if (item == null || !item.isPresent() || item.get() == null)
			return;

		XMaterialUtil material = item.get();

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(material))
			return;

		if (isDamageable(material.parseMaterial())) {
			e.setCancelled(true);

			List<Player> targetedPlayers = getTargetedPlayers(null, command[1]);

			if (targetedPlayers.isEmpty()) {
				MessagesUtil.sendMessage(e.getSender(),
						MessagesUtil.PLAYER_NOT_FOUND.toString().replaceAll("%id%", command[1]));
				return;
			}

			itemName = itemName.toLowerCase().replaceAll("_", " ");
			String niceName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
			ItemStack itemStack = material.parseItem();
			itemStack.setAmount(amount);
			targetedPlayers.stream().forEach(player -> {
				ItemUtil.addItem(player, itemStack);
				MessagesUtil.sendMessage(e.getSender(),
						MessagesUtil.GIVEN_ITEM.toString().replaceAll("%amount%", amount + "")
								.replaceAll("%item%", niceName).replaceAll("%id%", player.getName()));
			});
		}
	}

	public List<Player> getTargetedPlayers(Player player, String value) {
		switch (value) {
		case "@a":
		case "@e":
			return Lists.newArrayList(Bukkit.getOnlinePlayers());
		case "@p":
		case "@n":
			if (player == null)
				return Lists.newArrayList();
			Player nearestPlayer = player.getWorld().getPlayers().stream().filter(p -> !p.equals(player))
					.min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(player.getLocation())))
					.orElse(null);
			if (nearestPlayer == null)
				return Lists.newArrayList();
			return Lists.newArrayList(nearestPlayer);
		case "@s":
			if (player == null)
				return Lists.newArrayList();
			return Lists.newArrayList(player);
		case "@r":
			List<Player> players = Lists.newArrayList(Bukkit.getOnlinePlayers());
			Player selectedPlayer = (Player) RandomUtil.getRandomItem(players);
			return Lists.newArrayList(selectedPlayer);
		default:
			Player target = Bukkit.getPlayer(value);
			if (target == null)
				return Lists.newArrayList();
			return Lists.newArrayList(target);
		}
	}

	boolean isDamageable(Material m) {
		switch (m) {
		case DIAMOND_SWORD:
		case STONE_SWORD:
		case GOLDEN_SWORD:
		case NETHERITE_SWORD:
		case IRON_SWORD:
		case WOODEN_SWORD:
		case DIAMOND_PICKAXE:
		case GOLDEN_PICKAXE:
		case IRON_PICKAXE:
		case NETHERITE_PICKAXE:
		case WOODEN_PICKAXE:
		case STONE_PICKAXE:
		case STONE_HOE:
		case WOODEN_HOE:
		case GOLDEN_HOE:
		case DIAMOND_HOE:
		case NETHERITE_HOE:
		case IRON_HOE:
		case DIAMOND_AXE:
		case GOLDEN_AXE:
		case NETHERITE_AXE:
		case WOODEN_AXE:
		case STONE_AXE:
		case IRON_AXE:
		case STONE_SHOVEL:
		case DIAMOND_SHOVEL:
		case IRON_SHOVEL:
		case GOLDEN_SHOVEL:
		case NETHERITE_SHOVEL:
		case WOODEN_SHOVEL:
		case CHAINMAIL_HELMET:
		case DIAMOND_HELMET:
		case GOLDEN_HELMET:
		case LEATHER_HELMET:
		case NETHERITE_HELMET:
		case IRON_HELMET:
		case TURTLE_HELMET:
		case CHAINMAIL_CHESTPLATE:
		case DIAMOND_CHESTPLATE:
		case GOLDEN_CHESTPLATE:
		case LEATHER_CHESTPLATE:
		case NETHERITE_CHESTPLATE:
		case IRON_CHESTPLATE:
		case LEATHER_LEGGINGS:
		case CHAINMAIL_LEGGINGS:
		case DIAMOND_LEGGINGS:
		case GOLDEN_LEGGINGS:
		case IRON_LEGGINGS:
		case NETHERITE_LEGGINGS:
		case CHAINMAIL_BOOTS:
		case DIAMOND_BOOTS:
		case LEATHER_BOOTS:
		case GOLDEN_BOOTS:
		case IRON_BOOTS:
		case NETHERITE_BOOTS:
		case TRIDENT:
		case ELYTRA:
		case SHEARS:
		case BOW:
		case CROSSBOW:
		case FISHING_ROD:
		case WARPED_FUNGUS_ON_A_STICK:
		case CARROT_ON_A_STICK:
		case SHIELD:
		case FLINT_AND_STEEL:
			return true;
		default:
			return false;
		}
	}
}