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
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Maps;

@SuppressWarnings("deprecation")
public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();
	private final static InventoryStacks PLUGIN = InventoryStacks.getInstance();

	private final Map<XMaterial, Integer> cachedDefaultStackSizes = Maps.newHashMap();
	private final Map<XMaterial, Integer> cachedUpdatedStackSizes = Maps.newHashMap();

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

	public void applyItem(boolean isStartUp, ItemStack stack) {
		XMaterial xMat = XMaterial.matchXMaterial(stack);

		if (xMat == null)
			return;

		if (!cachedUpdatedStackSizes.containsKey(xMat))
			return;

		int amount = cachedUpdatedStackSizes.get(xMat);
		applier.applyItem(isStartUp, stack, amount);
	}

	public boolean hasEditedStackSize(Material material) {
		if (material == Material.AIR)
			return false;

		XMaterial xMaterial = XMaterial.matchXMaterial(material);

		if (xMaterial == null)
			return false;

		return hasEditedStackSize(xMaterial);
	}

	public boolean hasEditedStackSize(XMaterial xMaterial) {
		return cachedUpdatedStackSizes.containsKey(xMaterial);
	}

	public void cacheMaterialStackSize(XMaterial xMaterial, int newStackSize, int oldStackSize) {
		cachedUpdatedStackSizes.putIfAbsent(xMaterial, newStackSize);

		if (applier.isModernApi()) // no need to cache default stack size
			return;

		cachedDefaultStackSizes.putIfAbsent(xMaterial, oldStackSize);
	}

	public void reloadInventoryStacks() {
		resetMaterialsToDefaultValues();
		cachedDefaultStackSizes.clear();
		cachedUpdatedStackSizes.clear();
		applyConfiguredStacks();
	}

	private void applyConfiguredStacks() {
		Map<String, Integer> resolvedStacks = new HashMap<String, Integer>();

		if (PLUGIN.getConfig().isSet("items")) {
			resolvedStacks = resolveConfiguredItems(PLUGIN.getConfig().getConfigurationSection("items").getKeys(true));

			for (Map.Entry<String, Integer> e : resolvedStacks.entrySet()) {
				String matName = e.getKey();
				int size = e.getValue();

				XMaterial xMat = XMaterial.matchXMaterial(matName).orElse(null);

				if (xMat == null) {
					ConsoleUtil.warning(ChatColor.RED + "The Item: " + matName
							+ " does not exist. Check MATERIAL_LIST.txt for all up to date item names.");
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

	private Map<String, Integer> resolveConfiguredItems(Collection<String> keys) {
		Map<String, Integer> resolved = new HashMap<>();
		XMaterial[] allMaterials = XMaterial.VALUES;

		for (String key : keys) {
			if (key == null)
				continue;

			if (key.isEmpty())
				continue;

			if (!PLUGIN.getConfig().isInt("items." + key))
				continue;

			int stackSize = validateStackSize(PLUGIN.getConfig().getInt("items." + key), key);

			final Pattern pattern;

			try {
				pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException ex) {
				ConsoleUtil.warning(ChatColor.RED + "Invalid regex in items: '" + key + "': " + ex.getDescription());
				continue;
			}

			for (XMaterial material : allMaterials) {
				String name = material.name();

				if (pattern.matcher(name).matches()) {
					resolved.putIfAbsent(name.toUpperCase(), stackSize);
				}
			}
		}

		return resolved;
	}

	private void updateAllItems(Set<String> exemptMaterials, int stackSize) {
		Set<Material> processed = new HashSet<>();

		for (XMaterial xMat : XMaterial.VALUES) {
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
	}
	
	public boolean isItem(Material mat) {
	    if (mat == null) return false;

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

		for (Map.Entry<XMaterial, Integer> entry : cachedDefaultStackSizes.entrySet()) {
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
