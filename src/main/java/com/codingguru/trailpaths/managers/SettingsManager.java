package com.codingguru.trailpaths.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.codingguru.trailpaths.utils.MessagesUtil;

public class SettingsManager {

	public void setup(Plugin p) {
		setupLang(p);
		setValues();
	}

	private FileConfiguration lang;
	private File langFile;

	private void setupLang(Plugin p) {
		langFile = new File(p.getDataFolder(), "lang.yml");

		if (!langFile.exists()) {
			try {
				langFile.createNewFile();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		lang = YamlConfiguration.loadConfiguration(langFile);
	}

	public FileConfiguration getLang() {
		return lang;
	}

	private void saveLang() {
		try {
			getLang().save(langFile);
		} catch (IOException ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not save config to " + langFile, ex);
		}
	}

	public void setValues() {
		for (MessagesUtil lang : MessagesUtil.values()) {
			if (!getLang().isSet(lang.getPath())) {
				getLang().set(lang.getPath(), lang.getDefault());
			}
		}

		saveLang();
	}
}
