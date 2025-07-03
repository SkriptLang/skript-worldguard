package org.skriptlang.skriptworldguard.worldguard;

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RegionUtils {

	/**
	 * A helper method to simplify getting a region in a world.
	 * @param world The world of the region.
	 * @param id The ID of the region. This method will perform ID validation.
	 * @return The region with the given id in the given world, or null if the region does not exist
	 * or the world's {@link RegionManager} could not be retrieved.
	 */
	public static @Nullable WorldGuardRegion getRegion(World world, String id) {
		if (!ProtectedRegion.isValidId(id)) {
			return null;
		}

		RegionManager regionManager = getRegionManager(world);
		if (regionManager == null) {
			return null;
		}

		ProtectedRegion region = regionManager.getRegion(id);
		if (region == null) {
			return null;
		}

		return new WorldGuardRegion(world, region);
	}

	public static Collection<WorldGuardRegion> getRegionsAt(Location location) {
		List<WorldGuardRegion> regions = new ArrayList<>();

		World world = location.getWorld();
		if (world == null) {
			return regions;
		}

		RegionManager regionManager = getRegionManager(world);
		if (regionManager == null) {
			return regions;
		}

		for (ProtectedRegion region : regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
			regions.add(new WorldGuardRegion(world, region));
		}

		return regions;
	}

	/**
	 * Tests if a player can build at a given location.
	 * @param player The player to test with
	 * @param location The location to test at
	 * @return Whether the given player can build at the location.
	 */
	public static boolean canBuild(Player player, Location location) {
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld()) ||
				getRegionContainer().createQuery().testBuild(BukkitAdapter.adapt(location), localPlayer);
	}

	/**
	 * Tests if a player can build in all the given regions.
	 * @param player The player to test with
	 * @param regions The regions to test against
	 * @return Whether the given player can build in all the regions.
	 */
	public static boolean canBuild(Player player, WorldGuardRegion... regions) {
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
			return true;
		}
		// create queryable set of regions
		ApplicableRegionSet regionSet = new RegionResultSet((List<ProtectedRegion>) Arrays.stream(regions)
				.map(WorldGuardRegion::region)
				.collect(Collectors.toCollection(ArrayList::new)), null);
		return regionSet.testState(localPlayer, Flags.BUILD);
	}

	public static Iterator<Block> getRegionBlockIterator(Iterator<WorldGuardRegion> regionsIterator) {
		return new Iterator<>() {
			Iterator<Block> currentBlockIterator = nextIterator();

			private Iterator<Block> nextIterator() {
				if (!regionsIterator.hasNext()) { // no new blocks, reuse empty iterator
					return currentBlockIterator;
				}
				WorldGuardRegion region = regionsIterator.next();
				World world = region.world();
				ProtectedRegion protectedRegion = region.region();
				AbstractRegion adaptedRegion;
				if (protectedRegion instanceof ProtectedPolygonalRegion polygonalRegion) { // Not as simple as a cube...
					adaptedRegion = new Polygonal2DRegion(BukkitAdapter.adapt(world), polygonalRegion.getPoints(),
							polygonalRegion.getMinimumPoint().getY(), polygonalRegion.getMaximumPoint().getY());
				} else if (protectedRegion instanceof ProtectedCuboidRegion) {
					adaptedRegion = new CuboidRegion(BukkitAdapter.adapt(region.world()),
							protectedRegion.getMinimumPoint(), protectedRegion.getMaximumPoint());
				} else {
					throw new IllegalArgumentException("Unexpected region type: " + protectedRegion.getClass());
				}
				return Iterators.transform(adaptedRegion.iterator(),
						blockVector -> world.getBlockAt(blockVector.getX(), blockVector.getY(), blockVector.getZ()));
			}

			@Override
			public boolean hasNext() {
				if (!currentBlockIterator.hasNext()) {
					currentBlockIterator = nextIterator();
				}
				return currentBlockIterator.hasNext();
			}

			@Override
			public Block next() {
				if (!currentBlockIterator.hasNext()) {
					currentBlockIterator = nextIterator();
				}
				return currentBlockIterator.next();
			}
		};
	}

	/**
	 * A helper method to provide a standardized way to stringify a {@link ProtectedRegion}.
	 * @param world The world of a region.
	 * @param id The ID of a region.
	 * @return A stringified version of a region.
	 * @see WorldGuardRegion#toString()
	 */
	public static String toString(World world, String id) {
		return "region \"" + id + "\" in the world \"" + world.getName() + "\"";
	}

	/**
	 * A helper method to simplify getting the RegionContainer from WorldGuard.
	 * @return The RegionContainer from the {@link WorldGuardPlatform}.
	 */
	public static RegionContainer getRegionContainer() {
		return WorldGuard.getInstance().getPlatform().getRegionContainer();
	}

	/**
	 * A helper method to simplify getting the RegionManager for a world.
	 * @param world The world to get the RegionManager for.
	 * @return The RegionManager for the given world, or null if:
	 * <ul>
	 *     <li>Region data for the given world has not been loaded</li>
	 *     <li>Region data for the given world has failed to load</li>
	 *     <li>Support for regions has been disabled</li>
	 * </ul>
	 */
	@Nullable
	public static RegionManager getRegionManager(World world) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
	}

}
