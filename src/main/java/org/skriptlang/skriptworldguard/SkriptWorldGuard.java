package org.skriptlang.skriptworldguard;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.hooks.regions.GriefPreventionHook;
import ch.njol.skript.hooks.regions.PreciousStonesHook;
import ch.njol.skript.hooks.regions.ResidenceHook;
import ch.njol.skript.hooks.regions.WorldGuardHook;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Version;
import ch.njol.yggdrasil.Fields;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.ClassLoader;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardEventHandler.Factory;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;

public class SkriptWorldGuard extends JavaPlugin implements AddonModule {

	private static SkriptWorldGuard instance;

	public static SkriptWorldGuard getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		// Disable all regions hooks so that Skript doesn't load any of its region syntax
		Skript.disableHookRegistration(GriefPreventionHook.class, PreciousStonesHook.class,
				ResidenceHook.class, WorldGuardHook.class);
	}

	@Override
	public void onEnable() {
		// Dependency Searching
		Plugin skript = getServer().getPluginManager().getPlugin("Skript");
		if (skript == null || !skript.isEnabled()) {
			getLogger().severe("Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else if (Skript.getVersion().isSmallerThan(new Version("2.12.0"))) {
			getLogger().severe("You are running an unsupported version of Skript. Please update to at least Skript 2.12.0. Disabling...");
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

		// Start Initialization
		instance = this;

		// Initialize WorldGuard Event Handler
		WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Factory(), null);

		// Register with Skript
		SkriptAddon addon = Skript.instance().registerAddon(SkriptWorldGuard.class, "skript-worldguard");
		addon.localizer().setSourceDirectories("lang", null);
		addon.loadModules(this);
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(WorldGuardRegion.class, "worldguardregion")
				.user("worldguard ?regions?")
				.name("Region")
				.description("A WorldGuard region.")
				.examples("region \"region\" in world(\"world\"")
				.requiredPlugins("WorldGuard 7")
				.since("1.0")
				.parser(new Parser<>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(WorldGuardRegion region, int flags) {
						return region.toString();
					}

					@Override
					public String toVariableNameString(WorldGuardRegion region) {
						return "worldguardregion:" + region;
					}
				})
				.serializer(new Serializer<>() {
					@Override
					public Fields serialize(WorldGuardRegion region) {
						Fields fields = new Fields();
						fields.putObject("world", region.world());
						fields.putObject("id", region.region().getId());
						return fields;
					}

					@Override
					public void deserialize(WorldGuardRegion region, Fields fields) {
						assert false;
					}

					@Override
					protected WorldGuardRegion deserialize(Fields fields) throws StreamCorruptedException {
						World world = fields.getObject("world", World.class);
						String id = fields.getObject("id", String.class);
						if (world == null || id == null) {
							throw new StreamCorruptedException();
						}
						WorldGuardRegion region = RegionUtils.getRegion(world, id);
						if (region == null) {
							throw new StreamCorruptedException("The " + WorldGuardRegion.toString(world, id) + " from WorldGuard could not be found. Does it still exist?");
						}
						return region;
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false;
					}
				})
				.changer(new Changer<>() {
					@Override
					public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
						if (mode == ChangeMode.DELETE) {
							return new Class[0];
						}
						return null;
					}

					@Override
					public void change(WorldGuardRegion[] regions, Object @Nullable [] delta, ChangeMode mode) {
						for (WorldGuardRegion region : regions) {
							RegionManager regionManager = RegionUtils.getRegionManager(region.world());
							if (regionManager != null) {
								regionManager.removeRegion(region.region().getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
							}
						}
					}
				}));

		Classes.registerClass(new EnumClassInfo<>(MoveType.class, "worldguardmovetype", "worldguard move types")
				.user("worldguard ?move(ment)? ?types?")
				.name("WorldGuard Move Type")
				.description("The move type in a WorldGuard enter/leave event.")
				.requiredPlugins("WorldGuard 7")
				.examples("on region enter:",
						"\tsend \"The move type is %the move type%\"")
				.since("1.0"));
	}

	@Override
	public void load(SkriptAddon addon) {
		ClassLoader.builder()
				.basePackage("org.skriptlang.skriptworldguard.elements")
				.deep(true)
				.initialize(true)
				.forEachClass(clazz -> {
					if (SyntaxElement.class.isAssignableFrom(clazz)) {
						try {
							clazz.getMethod("register", SyntaxRegistry.class).invoke(null, addon.syntaxRegistry());
						} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
							getLogger().severe("Failed to load syntax class: " + e);
						}
					}
				})
				.build()
				.loadClasses(SkriptWorldGuard.class, getFile());
	}

}
