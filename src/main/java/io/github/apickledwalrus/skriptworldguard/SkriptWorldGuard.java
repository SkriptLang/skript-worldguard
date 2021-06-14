package io.github.apickledwalrus.skriptworldguard;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SkriptWorldGuard extends JavaPlugin {

	public static final String prefix = "[skript-worldguard] ";

	private static SkriptWorldGuard instance;

	@Override
	public void onEnable() {

		// Dependency Searching

		Plugin skript = getServer().getPluginManager().getPlugin("Skript");
		if (skript == null || !skript.isEnabled()) {
			getLogger().severe(prefix + "Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (!Skript.getVersion().isLargerThan(new Version(2, 5, 3))) { // Skript is not any version after 2.5.3 (aka 2.6)
			getLogger().severe(prefix + "You are running an unsupported version of Skript. Please update to at least Skript 2.6. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuard == null || !worldGuard.isEnabled()) {
			getLogger().severe(prefix + "Could not find WorldGuard! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (new Version(WorldGuard.getVersion()).isSmallerThan(new Version(7))) {
			getLogger().severe(prefix + "You are running an unsupported version of WorldGuard. Please update to at least WorldGuard 7. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

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
