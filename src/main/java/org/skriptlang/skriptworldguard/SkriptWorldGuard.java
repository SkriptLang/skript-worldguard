package org.skriptlang.skriptworldguard;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardEventHandler.Factory;

import java.io.IOException;

public class SkriptWorldGuard extends JavaPlugin {

	public static final String PREFIX = "[skript-worldguard] ";

	@SuppressWarnings("NotNullFieldNotInitialized")
	private static SkriptWorldGuard instance;

	@Override
	public void onEnable() {

		// Dependency Searching

		Plugin skript = getServer().getPluginManager().getPlugin("Skript");
		if (skript == null || !skript.isEnabled()) {
			getLogger().severe(PREFIX + "Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (!Skript.getVersion().isLargerThan(new Version(2, 6, 3))) { // Skript is not any version after 2.5.3 (aka 2.6)
			getLogger().severe(PREFIX + "You are running an unsupported version of Skript. Please update to at least Skript 2.6. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuard == null || !worldGuard.isEnabled()) {
			getLogger().severe(PREFIX + "Could not find WorldGuard! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (new Version(WorldGuard.getVersion()).isSmallerThan(new Version(7))) {
			getLogger().severe(PREFIX + "You are running an unsupported version of WorldGuard. Please update to at least WorldGuard 7. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Initialize Event Handler (shouldn't ever return false since 'after' is null)
		WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Factory(), null);

		// Start Initialization

		instance = this;

		// Register with Skript

		SkriptAddon addon = Skript.registerAddon(this);
		try {
			addon.loadClasses("io.github.apickledwalrus.skriptworldguard.elements");
			addon.setLanguageFileDirectory("lang"); // Register ClassInfo lang definitions with Skript
			new RegionClasses(); // Register ClassInfos with Skript
		} catch (IOException e) {
			getLogger().severe("An error occurred while trying to register and load the addon with Skript. Disabling...");
			getLogger().severe("Printing StackTrace:");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}

	}

	public static SkriptWorldGuard getInstance() {
		return instance;
	}

}
