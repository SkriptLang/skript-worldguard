package org.skriptlang.skriptworldguard.worldguard;

import ch.njol.skript.util.AABB;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

		RegionManager rm = getRegionManager(world);
		if (rm == null) {
			return null;
		}

		ProtectedRegion region = rm.getRegion(id);
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

		RegionManager rm = getRegionManager(world);
		if (rm == null) {
			return regions;
		}

		for (ProtectedRegion region : rm.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
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
	public static boolean canBuild(Player player, @NotNull Location location) {
		World world = location.getWorld();
		if (world == null) {
			return false;
		} else if (player.hasPermission("worldguard.region.bypass." + world.getName())) {
			// Build access is always granted with this permission
			// See https://worldguard.enginehub.org/en/latest/permissions/
			return true;
		}
		return getRegionContainer().createQuery().testBuild(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player));
	}

	/**
	 * Tests if a player can build in all the given regions.
	 * @param player The player to test with
	 * @param regions The regions to test against
	 * @return Whether the given player can build in all the regions.
	 */
	public static boolean canBuild(Player player, WorldGuardRegion... regions) {
		// create queryable set of regions
		ApplicableRegionSet regionSet = new RegionResultSet(
                (List<ProtectedRegion>) Arrays.stream(regions)
                        .map(WorldGuardRegion::region)
                        .collect(Collectors.toCollection(ArrayList::new)), null);
		return regionSet.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
	}

	// TODO method returning iterator for efficiency
	public static List<Block> getBlocksInRegion(WorldGuardRegion region) {
		ProtectedRegion protectedRegion = region.region();
		List<Block> blocks = new ArrayList<>();
		if (protectedRegion instanceof ProtectedPolygonalRegion) { // Not as simple as a cube...
			ProtectedPolygonalRegion polygonalRegion = (ProtectedPolygonalRegion) protectedRegion;
			World world = region.world();
			int min = polygonalRegion.getMinimumPoint().getBlockY();
			int max = polygonalRegion.getMaximumPoint().getBlockY();
			Polygonal2DRegion worldEditRegion = new Polygonal2DRegion(BukkitAdapter.adapt(world), polygonalRegion.getPoints(), min, max);
			for (BlockVector3 block : worldEditRegion) {
				blocks.add(world.getBlockAt(block.getBlockX(), block.getBlockY(), block.getBlockZ()));
			}
		} else if (protectedRegion instanceof ProtectedCuboidRegion) {
			BlockVector3 min = protectedRegion.getMinimumPoint();
			BlockVector3 max = protectedRegion.getMaximumPoint();
			AABB aabb = new AABB(
					region.world(),
					new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
					new Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ())
			);
			for (Block block : aabb) {
				blocks.add(block);
			}
		}
		return blocks;
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
