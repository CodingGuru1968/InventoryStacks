package com.codingguru.inventorystacks.listeners.general;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.DamageableUtil;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.RandomUtil;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Lists;

public class Commands implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (!e.getMessage().startsWith("/give"))
			return;

		String[] command = e.getMessage().split(" ");

		if (command.length < 4)
			return;

		String itemName = command[2].replaceAll("minecraft:", "");

		if (command[2] == null || command[3] == null)
			return;

		int amount;
		
		try {
			amount = Integer.parseInt(command[3]);
		} catch (NumberFormatException exp) {
			MessagesUtil.sendMessage(e.getPlayer(), MessagesUtil.NUMBER_EXCEPTION.toString());
			return;
		}
		
		Optional<XMaterial> item = XMaterial.matchXMaterial(itemName);

		if (item == null || !item.isPresent() || item.get() == null)
			return;

		XMaterial material = item.get();

		if (!ItemHandler.getInstance().hasEditedStackSize(material))
			return;

		if (DamageableUtil.isDamageable(material.get())) {
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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommand(ServerCommandEvent e) {
		if (!e.getCommand().startsWith("give"))
			return;

		String[] command = e.getCommand().split(" ");

		if (command.length < 4)
			return;

		String itemName = command[2].replaceAll("minecraft:", "");

		if (command[2] == null || command[3] == null)
			return;

		int amount;

		try {
			amount = Integer.parseInt(command[3]);
		} catch (NumberFormatException exp) {
			MessagesUtil.sendMessage(e.getSender(), MessagesUtil.NUMBER_EXCEPTION.toString());
			return;
		}

		Optional<XMaterial> item = XMaterial.matchXMaterial(itemName);

		if (item == null || !item.isPresent() || item.get() == null)
			return;

		XMaterial material = item.get();

		if (!ItemHandler.getInstance().hasEditedStackSize(material))
			return;

		if (DamageableUtil.isDamageable(material.get())) {
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
}