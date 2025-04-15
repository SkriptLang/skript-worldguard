package org.skriptlang.skriptworldguard;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.hooks.regions.GriefPreventionHook;
import ch.njol.skript.hooks.regions.PreciousStonesHook;
import ch.njol.skript.hooks.regions.ResidenceHook;
import ch.njol.skript.hooks.regions.WorldGuardHook;
import ch.njol.skript.util.Version;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardEventHandler.Factory;

import java.io.IOException;

public class SkriptWorldGuard extends JavaPlugin {

	private static SkriptWorldGuard instance;

	@Override
	public void onEnable() {

		// Dependency Searching

		Plugin skript = getServer().getPluginManager().getPlugin("Skript");
		if (skript == null || !skript.isEnabled()) {
			getLogger().severe("Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (Skript.getVersion().isSmallerThan(new Version(2, 10, 0))) {
			getLogger().severe("You are running an unsupported version of Skript. Please update to at least Skript 2.10.0. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuard == null || !worldGuard.isEnabled()) {
			getLogger().severe("Could not find WorldGuard! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (new Version(WorldGuard.getVersion()).isSmallerThan(new Version(7))) {
			getLogger().severe("You are running an unsupported version of WorldGuard. Please update to at least WorldGuard 7. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Initialize WorldGuard Event Handler
		WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Factory(), null);

		// Start Initialization

		instance = this;

		// Register with Skript

		SkriptAddon addon = Skript.registerAddon(this);
		try {
			addon.setLanguageFileDirectory("lang"); // Register ClassInfo lang definitions with Skript
			new RegionClasses(); // Register ClassInfos with Skript
			addon.loadClasses("org.skriptlang.skriptworldguard.elements");
		} catch (IOException e) {
			getLogger().severe("An error occurred while trying to register and load the addon with Skript. Disabling...");
			getLogger().severe("Printing StackTrace:");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}

		// Skript hooks are disabled in a delayed startup task so this should be okay to do now
		// Disable all regions hooks so that Skript doesn't load any of its region syntax
		Skript.disableHookRegistration(
				GriefPreventionHook.class, PreciousStonesHook.class, ResidenceHook.class, WorldGuardHook.class
		);

	}

	public static SkriptWorldGuard getInstance() {
		return instance;
	}

}
