package com.codingguru.inventorystacks.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.items.StackSizeApplier;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.ReflectionLegacyUtil;
import com.codingguru.inventorystacks.util.ServerTypeUtil;
import com.codingguru.inventorystacks.util.StackSizeApplierUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import com.google.common.collect.Maps;

@SuppressWarnings("deprecation")
public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();
	private final static InventoryStacks PLUGIN = InventoryStacks.getInstance();

	private final Map<XMaterialUtil, Integer> cachedDefaultStackSizes = Maps.newHashMap();
	private final Map<XMaterialUtil, Integer> cachedUpdatedStackSizes = Maps.newHashMap();
	private final Map<Material, Integer> cachedUpdatedDirectMaterialSizes = Maps.newHashMap();
	private final Set<String> loggedUnsupportedMaterials = new HashSet<>();

	private VersionUtil serverVersion;
	private ServerTypeUtil serverType;
	private StackSizeApplier applier;

	private ItemHandler() {
	}

	public static ItemHandler getInstance() {
		return INSTANCE;
	}

	public void setup() {
		if (!setupServerVersion()) {
			String pkg = Bukkit.getServer().getClass().getPackage().getName();
			String versionFound = pkg.substring(pkg.lastIndexOf('.') + 1);
			ConsoleUtil.warning("THE VERSION: " + versionFound + " IS CURRENTLY UNSUPPORTED. DISABLING PLUGIN...");
			Bukkit.getPluginManager().disablePlugin(InventoryStacks.getInstance());
			return;
		}

		setupServerType();

		this.applier = StackSizeApplierUtil.create();
		this.applier.setup();

		ConsoleUtil.message(ChatColor.GREEN + "Loaded Version: " + ChatColor.YELLOW
				+ ItemHandler.getInstance().getServerVersion().toString());
		ConsoleUtil.message(ChatColor.GREEN + "Server Type: " + ChatColor.YELLOW
				+ ItemHandler.getInstance().getServerType().toString());
		ConsoleUtil.message(ChatColor.GREEN + "Stack Sizing Mode: " + ChatColor.YELLOW
				+ (applier.isModernApi() ? "ItemMeta API (1.20.5+)" : "Legacy NMS"));
		ConsoleUtil.message("");

		applyConfiguredStacks();

	}

	private boolean setupServerVersion() {
		String pkg = Bukkit.getServer().getClass().getPackage().getName();
		String versionFound = pkg.substring(pkg.lastIndexOf('.') + 1);

		String cleanVersion = Bukkit.getBukkitVersion().split("-")[0];
		String[] parts = cleanVersion.split("\\.");
		if (parts.length < 2)
			return false;

		String majorMinor = parts[0] + "." + parts[1];

		if (versionFound.equalsIgnoreCase("craftbukkit")) {
			if (majorMinor.equals("26.1") || majorMinor.equals("1.26")) {
				serverVersion = VersionUtil.v1_26;
				return true;
			}
			if (majorMinor.equals("1.21")) {
				serverVersion = VersionUtil.v1_21;
				return true;
			}
			if (majorMinor.equals("1.20")) {
				serverVersion = VersionUtil.v1_20;
				return true;
			}
			return false;
		}

		for (VersionUtil version : VersionUtil.values()) {
			if (versionFound.contains(version.name())) {
				serverVersion = version;
				return true;
			}
		}

		return false;
	}

	private void setupServerType() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			serverType = ServerTypeUtil.FOLIA;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		try {
			Class.forName("io.papermc.paper.ServerBuildInfo");
			serverType = ServerTypeUtil.PAPER;
			return;
		} catch (ClassNotFoundException ignored) {
		}

		serverType = ServerTypeUtil.SPIGOT;
	}

	public boolean isUsingModernAPI() {
		return applier.isModernApi();
	}

	public boolean hasUpdatedStack(ItemStack stack) {
		if (stack == null || stack.getType() == Material.AIR)
			return false;

		XMaterialUtil xMat = matchXMaterialSafely(stack);

		if (xMat != null)
			return cachedUpdatedStackSizes.containsKey(xMat);

		return cachedUpdatedDirectMaterialSizes.containsKey(stack.getType());
	}

	public void applyItem(boolean isStartUp, ItemStack stack) {
		if (stack == null || stack.getType() == Material.AIR)
			return;

		XMaterialUtil xMat = matchXMaterialSafely(stack);

		if (xMat != null) {
			Integer amount = cachedUpdatedStackSizes.get(xMat);
			if (amount == null)
				return;

			applier.applyItem(isStartUp, stack, amount);
			return;
		}

		Integer amount = cachedUpdatedDirectMaterialSizes.get(stack.getType());
		if (amount == null)
			return;

		applier.applyItem(isStartUp, stack, amount);
	}

	private XMaterialUtil matchXMaterialSafely(ItemStack stack) {
		try {
			return XMaterialUtil.matchXMaterial(stack);
		} catch (IllegalArgumentException ex) {
			debugUnsupportedMaterial(stack.getType(), ex);
			return null;
		}
	}

	private XMaterialUtil matchXMaterialSafely(Material material) {
		try {
			return XMaterialUtil.matchXMaterial(material);
		} catch (IllegalArgumentException ex) {
			debugUnsupportedMaterial(material, ex);
			return null;
		}
	}

	private void debugUnsupportedMaterial(Material material, Exception ex) {
		if (material == null)
			return;

		String key = material.name().toUpperCase();

		if (!loggedUnsupportedMaterials.add(key))
			return;

		ConsoleUtil.debug("Unsupported XMaterial mapping detected for '" + material.name()
				+ "' on server version " + getServerVersion() + ". Falling back to direct Material handling.");
		ConsoleUtil.debug("Root cause: " + ex.getMessage());
	}

	public boolean hasEditedStackSize(Material material) {
		if (material == Material.AIR)
			return false;

		XMaterialUtil xMaterial = matchXMaterialSafely(material);

		if (xMaterial != null)
			return hasEditedStackSize(xMaterial);

		return cachedUpdatedDirectMaterialSizes.containsKey(material);
	}

	public boolean hasEditedStackSize(XMaterialUtil xMaterial) {
		if (xMaterial == null)
			return false;

		return cachedUpdatedStackSizes.containsKey(xMaterial);
	}

	public void cacheMaterialStackSize(XMaterialUtil xMaterial, int newStackSize, int oldStackSize) {
		cachedUpdatedStackSizes.putIfAbsent(xMaterial, newStackSize);

		if (applier.isModernApi()) // no need to cache default stack size
			return;

		cachedDefaultStackSizes.putIfAbsent(xMaterial, oldStackSize);
	}

	public void cacheMaterialStackSize(Material material, int newStackSize) {
		if (material == null || material == Material.AIR)
			return;

		cachedUpdatedDirectMaterialSizes.putIfAbsent(material, newStackSize);
	}

	public void reloadInventoryStacks() {
		resetMaterialsToDefaultValues();
		cachedDefaultStackSizes.clear();
		cachedUpdatedStackSizes.clear();
		cachedUpdatedDirectMaterialSizes.clear();
		loggedUnsupportedMaterials.clear();
		applyConfiguredStacks();
	}

	private void applyConfiguredStacks() {
		Map<String, Integer> resolvedStacks = new HashMap<String, Integer>();

		if (PLUGIN.getConfig().isSet("items")) {
			resolvedStacks = resolveConfiguredItems(PLUGIN.getConfig().getConfigurationSection("items").getKeys(true));

			for (Map.Entry<String, Integer> e : resolvedStacks.entrySet()) {
				String matName = e.getKey();
				int size = e.getValue();

				XMaterialUtil xMat = XMaterialUtil.matchXMaterial(matName).orElse(null);

				if (xMat == null) {
					Material directMaterial = resolveMaterialByName(matName);

					if (directMaterial == null || !isItem(directMaterial)) {
						ConsoleUtil.warning(ChatColor.RED + "The Item: " + matName
								+ " does not exist. Check MATERIAL_LIST.txt for all up to date item names.");
						continue;
					}

					cacheMaterialStackSize(directMaterial, size);
					applier.applyItem(true, new ItemStack(directMaterial), size);
					ConsoleUtil.info(ChatColor.YELLOW + "Successfully set " + directMaterial.name() + " stack size to: " + size
							+ " (direct Material fallback)");
					continue;
				}

				Material mat = xMat.get();

				if (mat == null || !isItem(mat)) {
					continue;
				}

				cacheMaterialStackSize(xMat, size, mat.getMaxStackSize());
				applier.applyItem(true, xMat.parseItem(), size);
				ConsoleUtil.info(ChatColor.YELLOW + "Successfully set " + matName + " stack size to: " + size);
			}
		}

		if (PLUGIN.getConfig().getBoolean("max-stack-for-all-items.enabled")) {
			int stackSize = validateStackSize(PLUGIN.getConfig().getInt("max-stack-for-all-items.amount"), "ALL");

			List<String> configExemptList = PLUGIN.getConfig().getStringList("max-stack-for-all-items.whitelist");

			Set<String> exemptMaterials = configExemptList.stream().filter(Objects::nonNull).map(s -> s.toUpperCase())
					.collect(Collectors.toSet());

			resolvedStacks.keySet().forEach(name -> exemptMaterials.add(name.toUpperCase()));

			updateAllItems(exemptMaterials, stackSize);
		}
	}

	private Material resolveMaterialByName(String materialName) {
		if (materialName == null || materialName.trim().isEmpty())
			return null;

		String normalized = materialName.trim().toUpperCase();

		Material matched = Material.getMaterial(normalized);
		if (matched != null)
			return matched;

		matched = Material.matchMaterial(materialName);
		if (matched != null)
			return matched;

		if (!normalized.startsWith("MINECRAFT:"))
			return null;

		return Material.matchMaterial(normalized.substring("MINECRAFT:".length()));
	}

	private Map<String, Integer> resolveConfiguredItems(Collection<String> keys) {
		Map<String, Integer> resolved = new HashMap<>();
		XMaterialUtil[] allMaterials = XMaterialUtil.VALUES;

		for (String key : keys) {
			if (key == null)
				continue;

			if (key.isEmpty())
				continue;

			if (!PLUGIN.getConfig().isInt("items." + key))
				continue;

			int stackSize = validateStackSize(PLUGIN.getConfig().getInt("items." + key), key);
			String patternInput = resolveItemGroupPattern(key);

			final Pattern pattern;

			try {
				pattern = Pattern.compile(patternInput, Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException ex) {
				ConsoleUtil.warning(ChatColor.RED + "Invalid regex in items: '" + key + "': " + ex.getDescription());
				continue;
			}

			for (XMaterialUtil material : allMaterials) {
				String name = material.name();

				if (pattern.matcher(name).matches()) {
					resolved.putIfAbsent(name.toUpperCase(), stackSize);
				}
			}
		}

		return resolved;
	}

	private String resolveItemGroupPattern(String key) {
		String normalized = key.trim().toUpperCase();

		switch (normalized) {
		case "BEDS":
		case "BED":
			return "^.*_BED$";
		case "POTIONS":
		case "POTION":
			return "^(POTION|SPLASH_POTION|LINGERING_POTION)$";
		case "STEWS":
		case "STEW":
			return "^(MUSHROOM_STEW|RABBIT_STEW|SUSPICIOUS_STEW|BEETROOT_SOUP)$";
		case "BUCKETS":
		case "BUCKET":
			return "^(BUCKET|.*_BUCKET)$";
		case "BOATS":
		case "BOAT":
			return "^(.*_BOAT|.*_CHEST_BOAT|.*_RAFT|.*_CHEST_RAFT)$";
		default:
			return key;
		}
	}

	private void updateAllItems(Set<String> exemptMaterials, int stackSize) {
		Set<Material> processed = new HashSet<>();

		for (XMaterialUtil xMat : XMaterialUtil.VALUES) {
			if (!xMat.isSupported())
				continue;

			Material mat = xMat.get();

			if (mat == null || !isItem(mat))
				continue;

			if (exemptMaterials.contains(mat.name().toUpperCase())
					|| exemptMaterials.contains(xMat.name().toUpperCase()))
				continue;

			if (!processed.add(mat))
				continue;

			cacheMaterialStackSize(xMat, stackSize, mat.getMaxStackSize());
			applier.applyItem(true, xMat.parseItem(), stackSize);
			ConsoleUtil.info(ChatColor.YELLOW + "Successfully set " + mat.name() + " stack size to: " + stackSize);
		}

		for (Material mat : Material.values()) {
			if (mat == null || !isItem(mat))
				continue;

			if (exemptMaterials.contains(mat.name().toUpperCase()))
				continue;

			if (!processed.add(mat))
				continue;

			cacheMaterialStackSize(mat, stackSize);
			applier.applyItem(true, new ItemStack(mat), stackSize);
			ConsoleUtil.info(ChatColor.YELLOW + "Successfully set " + mat.name() + " stack size to: " + stackSize
					+ " (direct Material fallback)");
		}
	}

	public boolean isItem(Material mat) {
		if (mat == null)
			return false;

		try {
			return mat.isItem(); // 1.13+
		} catch (NoSuchMethodError ignored) {
			return ReflectionLegacyUtil.hasItemForm(mat) != null; // 1.8–1.12
		}
	}

	private int validateStackSize(int stackSize, String itemName) {
		int absoluteMaxStackSize = serverVersion.getAbsoluteMaxStackSize();

		if (stackSize > absoluteMaxStackSize) {
			ConsoleUtil.warning("Stack size: " + stackSize + " can not be set for " + itemName + " item(s) over "
					+ absoluteMaxStackSize + ". Defaulting to max value...");
			return absoluteMaxStackSize;
		}

		if (stackSize < 1) {
			ConsoleUtil.warning(ChatColor.RED + "Unable to set stack size to: " + stackSize + " for " + itemName
					+ " item(s). Defaulting to 1...");
			stackSize = 1;
		}

		return stackSize;
	}

	private void resetMaterialsToDefaultValues() {
		if (applier.isModernApi())
			return;

		for (Map.Entry<XMaterialUtil, Integer> entry : cachedDefaultStackSizes.entrySet()) {
			int defaultSize = entry.getValue();
			applier.applyItem(true, entry.getKey().parseItem(), defaultSize);
		}
	}

	public VersionUtil getServerVersion() {
		return serverVersion;
	}

	public ServerTypeUtil getServerType() {
		return serverType;
	}
}
