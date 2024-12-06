package com.codingguru.inventorystacks.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import com.codingguru.inventorystacks.InventoryStacks;

public class UpdateUtil {

	private final int RESOURCE_ID = 116877;

	@SuppressWarnings("deprecation")
	public boolean hasNewUpdate() {
		if (!InventoryStacks.getInstance().getConfig().getBoolean("check-for-updates", true)) {
			return false;
		}

		String currentVersion = InventoryStacks.getInstance().getDescription().getVersion();

		try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID)
				.toURL().openStream(); Scanner scanner = new Scanner(inputStream)) {
			if (scanner.hasNext()) {
				String updatedVersion = scanner.next();
				if (!currentVersion.equalsIgnoreCase(updatedVersion)) {
					return true; // found update
				}
			}
		} catch (IOException | URISyntaxException e) {
		}

		return false;
	}
}