package org.skriptlang.skriptworldguard.worldguard;

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods for working with {@link WorldGuardRegion}s.
 */
public final class RegionUtils {

	private RegionUtils() { }

	/**
	 * A utility method for obtaining the {@link RegionContainer} from the running {@link WorldGuardPlatform}.
	 * @return The region container of the active platform.
	 */
	public static RegionContainer getRegionContainer() {
		return WorldGuard.getInstance().getPlatform().getRegionContainer();
	}

	/**
	 * A utility method for obtaining a world's {@link RegionManager}.
	 * @param world The world to get the RegionManager for.
	 * @return The RegionManager for the given world, or null if:
	 * <ul>
	 *     <li>Region data for the given world has not been loaded</li>
	 *     <li>Region data for the given world has failed to load</li>
	 *     <li>Support for regions has been disabled</li>
	 * </ul>
	 */
	public static @Nullable RegionManager getRegionManager(World world) {
		return getRegionContainer().get(BukkitAdapter.adapt(world));
	}

	/**
	 * A utility method for obtaining all of the regions in a world.
	 * @param world The world to obtain regions of.
	 * @return The regions of {@code world}.
	 *  If {@link #getRegionManager(World)} is unavailable, this method returns an empty list.
	 */
	public static @Unmodifiable List<WorldGuardRegion> getRegions(World world) {
		RegionManager regionManager = getRegionManager(world);
		if (regionManager == null) {
			return List.of();
		}

		return regionManager.getRegions().values().stream()
				.map(region -> new WorldGuardRegion(world, region))
				.toList();
	}

	/**
	 * A utility method for obtaining a named region in a world.
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

	/**
	 * A utility method for obtaining the regions at a specific location.
	 * @param location The location to search for regions at.
	 * @return A list of all regions found at {@code location}.
	 */
	public static Collection<WorldGuardRegion> getRegionsAt(Location location) {
		World world = location.getWorld();
		if (world == null) {
			return List.of();
		}

		RegionManager regionManager = getRegionManager(world);
		if (regionManager == null) {
			return List.of();
		}

		List<WorldGuardRegion> regions = new ArrayList<>();
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
		World world = location.getWorld();
		if (world == null) {
			return false;
		}
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(world)) ||
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
		// check bypass
		for (WorldGuardRegion region : regions) {
			if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(region.world()))) {
				return true;
			}
		}
		// create queryable set of regions
		ApplicableRegionSet regionSet = new RegionResultSet((List<ProtectedRegion>) Arrays.stream(regions)
				.map(WorldGuardRegion::region)
				.collect(Collectors.toCollection(ArrayList::new)), null);
		return regionSet.testState(localPlayer, Flags.BUILD);
	}

	/**
	 * A utility method for obtaining an iterator over the blocks of multiple regions.
	 * @param regionIterator An iterator over the regions whose blocks should be iterated over.
	 * @return An iterator over the blocks of the regions of {@code regionsIterator}.
	 */
	public static Iterator<Block> getRegionBlockIterator(Iterator<WorldGuardRegion> regionIterator) {
		return new Iterator<>() {
			Iterator<Block> currentBlockIterator = nextIterator();

			private Iterator<Block> nextIterator() {
				if (!regionIterator.hasNext()) { // no new blocks, reuse empty iterator
					return currentBlockIterator;
				}
				WorldGuardRegion region = regionIterator.next();
				World world = region.world();
				return Iterators.transform(region.asWorldEditRegion().iterator(),
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

}
