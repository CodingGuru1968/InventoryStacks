package com.codingguru.inventorystacks.scheduler;

import java.lang.ref.WeakReference;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.VersionUtil;

public class ChangeItemInHandTask extends Schedule {

	private final WeakReference<Player> player;
	private final ItemStack item;
	private final Material material;

	public ChangeItemInHandTask(Player player, ItemStack item, Material material) {
		this.player = new WeakReference<Player>(player);
		this.item = item;
		this.material = material;
	}

	@Override
	public void run() {
		Player player = this.player.get();
		updateItem(player, item, material);
	}

	@SuppressWarnings("deprecation")
	private void updateItem(Player player, ItemStack item, Material mat) {
		if (!VersionUtil.v1_9_R1.isServerVersionHigher()) {
			player.getInventory().setItemInHand(item);
			return;
		}

		if (player.getInventory().getItemInMainHand().getType() == mat) {
			player.getInventory().setItemInMainHand(item);
		} else if (player.getInventory().getItemInOffHand().getType() == mat) {
			player.getInventory().setItemInOffHand(item);
		}
	}
}