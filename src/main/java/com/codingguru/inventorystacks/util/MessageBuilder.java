package com.codingguru.inventorystacks.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ManagerHandler;
import com.codingguru.inventorystacks.managers.LanguageManager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageBuilder {

	private final String path;
	private final String defaultText;
	private final Map<String, String> replacements;

	private MessageBuilder(Builder builder) {
		this.path = builder.path;
		this.defaultText = builder.defaultText;
		this.replacements = builder.replacements;
	}

	private static final InventoryStacks PLUGIN = InventoryStacks.getInstance();
	private static BukkitAudiences adventureAPI;

	public String build() {
		LanguageManager settingsManager = ManagerHandler.getInstance().get(LanguageManager.class);
		String message = defaultText;

		if (settingsManager != null && settingsManager.getLang() != null) {
			if (!settingsManager.getLang().isSet(path)) {
				settingsManager.getLang().set(path, defaultText);
				settingsManager.saveLang();
			}
			message = settingsManager.getLang().getString(path, defaultText);
		}

		if (message == null) {
			message = "";
		}

		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			message = message.replace(entry.getKey(), entry.getValue());
		}

		message = ColorUtil.replace(message);
		return message;
	}

	public void send(CommandSender sender) {
		String message = build();
		
		if (message.isEmpty()) {
			return;
		}

		if (PLUGIN.getConfig().getBoolean("use-mini-message", false)) {
			Audience audience = getAudience().sender(sender);
			Component component = MiniMessage.miniMessage().deserialize(message);
			audience.sendMessage(component);
			return;
		}

		String[] multiMessage = message.split("\\\\n");
		for (String msg : multiMessage) {
			sender.sendMessage(msg.replace("\\n", ""));
		}
	}

	public void broadcast() {
		Bukkit.getOnlinePlayers().forEach(this::send);
	}

	private static BukkitAudiences getAudience() {
		if (adventureAPI == null && PLUGIN.getConfig().getBoolean("use-mini-message", false)) {
			adventureAPI = BukkitAudiences.create(PLUGIN);
		}
		return adventureAPI;
	}

	public static class Builder {
		private final String path;
		private final String defaultText;
		private final Map<String, String> replacements = new HashMap<>();

		public Builder(String path, String defaultText) {
			this.path = path;
			this.defaultText = defaultText;
		}

		public Builder set(String placeholder, Object value) {
			this.replacements.put(placeholder, value != null ? value.toString() : "");
			return this;
		}

		public MessageBuilder build() {
			return new MessageBuilder(this);
		}

		public void send(CommandSender sender) {
			build().send(sender);
		}

		public void broadcast() {
			build().broadcast();
		}
	}
}