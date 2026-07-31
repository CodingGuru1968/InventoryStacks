package com.codingguru.inventorystacks.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.api.PluginManager;
import com.codingguru.inventorystacks.util.ConsoleUtil;

public final class LanguageManager implements PluginManager {

	private final InventoryStacks plugin;
	private FileConfiguration langConfig;
	private File langFile;

	public LanguageManager(InventoryStacks plugin) {
		this.plugin = plugin;
	}

	@Override
	public void start() {
		initializeLanguageFile();
	}

	@Override
	public void stop() {
		langFile = null;
		langConfig = null;
	}

	private void initializeLanguageFile() {
		File langDirectory = new File(plugin.getDataFolder(), "lang");

		if (!langDirectory.exists() && !langDirectory.mkdirs()) {
			ConsoleUtil.warning("Failed to create the 'lang' directory.");
		}

		String languageName = plugin.getConfig().getString("language", "en");
		String resourcePath = "lang/" + languageName + ".yml";

		langFile = new File(plugin.getDataFolder(), resourcePath);

		if (!langFile.exists()) {
			try {
				plugin.saveResource(resourcePath, false);
			} catch (IllegalArgumentException ex) {
				ConsoleUtil.info(
						"Language file " + languageName + ".yml not found in plugin jar. Creating a blank file...");
				try {
					langFile.createNewFile();
				} catch (IOException ioException) {
					ConsoleUtil.warning(
							"Could not create language file: " + langFile.getName() + ioException.getMessage());
				}
			}
		}

		migrateAndCleanOldFiles(langFile);

		langConfig = YamlConfiguration.loadConfiguration(langFile);
	}

	public FileConfiguration getLang() {
		return langConfig;
	}

	public void saveLang() {
		if (langConfig == null || langFile == null) {
			return;
		}

		try {
			langConfig.save(langFile);
		} catch (IOException ex) {
			ConsoleUtil.warning("Could not save language file to " + langFile + " : " + ex.getMessage());
		}
	}

	private void migrateAndCleanOldFiles(File activeLangFile) {
		File oldLegacyFile = new File(plugin.getDataFolder(), "lang.yml");

		if (oldLegacyFile.exists()) {
			ConsoleUtil.info("Found legacy InventoryStacks language file. Migrating values before deletion...");

			FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldLegacyFile);
			FileConfiguration newConfig = YamlConfiguration.loadConfiguration(activeLangFile);

			Map<String, String> migrationMap = new HashMap<>();
			migrationMap.put("GIVEN_ITEM", "given-item");
			migrationMap.put("HAND_ITEMS_STACKED", "hand-items-stacked");
			migrationMap.put("ALL_ITEMS_STACKED", "all-items-stacked");
			migrationMap.put("INVALID_STACK_TYPE", "invalid-stack-type");
			migrationMap.put("DISALLOW_ANVIL_STACK", "disallow-anvil-stack");
			migrationMap.put("PREVENT_SHIFT_COMBINING_DAMAGEABLE_ITEMS", "prevent-shift-combining-damageable-items");
			migrationMap.put("COMMAND_DISABLED", "command-disabled");
			migrationMap.put("BUNDLE_FIX", "bundle-fix");
			migrationMap.put("RELOAD", "reload");
			migrationMap.put("INCORRECT_USAGE", "incorrect-usage");
			migrationMap.put("IN_GAME", "in-game-only");
			migrationMap.put("NUMBER_EXCEPTION", "number-exception");
			migrationMap.put("PLAYER_NOT_FOUND", "player-not-found");
			migrationMap.put("NO_PERMISSION", "no-permission");

			boolean migrated = false;

			for (Map.Entry<String, String> entry : migrationMap.entrySet()) {
				if (oldConfig.contains(entry.getKey())) {
					newConfig.set(entry.getValue(), oldConfig.get(entry.getKey()));
					migrated = true;
				}
			}

			if (migrated) {
				try {
					newConfig.save(activeLangFile);
					ConsoleUtil.info("Successfully migrated old InventoryStacks values into the new format.");
				} catch (IOException e) {
					ConsoleUtil.warning("Failed to save the new InventoryStacks language file during migration!");
					e.printStackTrace();
				}
			}

			if (oldLegacyFile.delete()) {
				ConsoleUtil.info("Successfully deleted obsolete InventoryStacks legacy file.");
			} else {
				ConsoleUtil.warning("Could not delete old InventoryStacks legacy file.");
			}
		}
	}
}