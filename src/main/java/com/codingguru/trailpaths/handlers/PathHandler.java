package com.codingguru.trailpaths.handlers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.codingguru.trailpaths.TrailPaths;
import com.codingguru.trailpaths.utils.ConsoleUtil;
import com.codingguru.trailpaths.utils.WeightedRandomUtil;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PathHandler {

	private static final PathHandler INSTANCE = new PathHandler();
	private Map<Material, WeightedRandomUtil<Material>> pathMaterials;
	private Set<UUID> disabledPaths;

	private PathHandler() {
		pathMaterials = Maps.newHashMap();
		disabledPaths = Sets.newHashSet();
		resetMaterials();
	}

	@SuppressWarnings("deprecation")
	public void resetMaterials() {
		pathMaterials.clear();

		for (String materialName : TrailPaths.getInstance().getConfig().getConfigurationSection("paths")
				.getKeys(false)) {

			Optional<XMaterial> materialToChange = XMaterial.matchXMaterial(materialName);

			if (!materialToChange.isPresent()) {
				ConsoleUtil.warning("[TrailPaths] Could not add material: " + materialName + " as it does not exist.");
				continue;
			}

			WeightedRandomUtil<Material> materialsToChangeTo = new WeightedRandomUtil<>();

			ConfigurationSection section = TrailPaths.getInstance().getConfig()
					.getConfigurationSection("paths." + materialName);

			if (section == null) {
				ConsoleUtil.warning("[TrailPaths] Using old config format. Please update to new format.");
				continue;
			}

			for (String changeToName : section.getKeys(false)) {
				Optional<XMaterial> changeMaterialToType = XMaterial.matchXMaterial(changeToName);

				if (!changeMaterialToType.isPresent()) {
					ConsoleUtil
							.warning("[TrailPaths] Could not add material: " + changeToName + " as it does not exist.");
					continue;
				}

				int percentage = TrailPaths.getInstance().getConfig()
						.getInt("paths." + materialName + "." + changeToName);

				materialsToChangeTo.addEntry(changeMaterialToType.get().parseMaterial(), percentage);
			}

			pathMaterials.put(materialToChange.get().parseMaterial(), materialsToChangeTo);
		}
	}

	public boolean isPathDisabled(UUID uuid) {
		return disabledPaths.contains(uuid);
	}

	public void enablePath(UUID uuid) {
		disabledPaths.remove(uuid);
	}

	public void disablePath(UUID uuid) {
		disabledPaths.add(uuid);
	}

	public boolean contains(Material material) {
		return pathMaterials.containsKey(material);
	}

	public Material getChangedMaterial(Material material) {
		return pathMaterials.get(material).getRandom();
	}

	public static PathHandler getInstance() {
		return INSTANCE;
	}
}