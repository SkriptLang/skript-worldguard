package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A utility record for associating a {@link ProtectedRegion} with a {@link World}.
 * @param world The world {@code region} is within.
 * @param region WorldGuard region.
 */
public record WorldGuardRegion(World world, ProtectedRegion region) implements Comparable<WorldGuardRegion> {

	/**
	 * A helper method to provide a standardized way to stringify a {@link WorldGuardRegion} from its components.
	 * @param world The world of a region.
	 * @param id The ID of a region.
	 * @return A stringified version of a region.
	 * @see WorldGuardRegion#toString()
	 */
	public static String toString(World world, String id) {
		return "region \"" + id + "\" in the world \"" + world.getName() + "\"";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof WorldGuardRegion(World otherWorld, ProtectedRegion otherRegion)) {
			return world == otherWorld && region.getId().equals(otherRegion.getId());
		}
		return false;
	}

	@Override
	public @NotNull String toString() {
		return toString(world, region.getId());
	}

	/**
	 * @return The name (ID) of this region.
	 */
	public String name() {
		return region.getId();
	}

	/*
	 * Comparable
	 */

	@Override
	public int compareTo(@NotNull WorldGuardRegion other) {
		return Integer.compare(this.region.getPriority(), other.region.getPriority());
	}

	/*
	 * Utility
	 */

	/**
	 * Converts this region into a WorldEdit {@link Region}.
	 * @return A region representing the physical area of this region.
	 * @throws IllegalArgumentException If this region does not encompass a physical area ({@link ProtectedRegion#isPhysicalArea()}).
	 */
	public Region asWorldEditRegion() {
		Region worldEditRegion;
		if (region instanceof ProtectedPolygonalRegion polygonalRegion) { // Not as simple as a cube...
			worldEditRegion = new Polygonal2DRegion(BukkitAdapter.adapt(world), polygonalRegion.getPoints(),
					polygonalRegion.getMinimumPoint().getY(), polygonalRegion.getMaximumPoint().getY());
		} else if (region instanceof ProtectedCuboidRegion) {
			worldEditRegion = new CuboidRegion(BukkitAdapter.adapt(world),
					region.getMinimumPoint(), region.getMaximumPoint());
		} else {
			throw new IllegalArgumentException("Unexpected region type: " + region.getClass());
		}
		return worldEditRegion;
	}

	/**
	 * @param chunk The chunk to check for.
	 * @return Whether {@code chunk} is within this region.
	 */
	public boolean contains(Chunk chunk) {
		BlockVector2 chunkVector = BlockVector2.at(chunk.getX(), chunk.getZ());
		return asWorldEditRegion().getChunks().contains(chunkVector);
	}

	/**
	 * @param location The location to check for.
	 * @return Whether {@code location} is within this region.
	 */
	public boolean contains(Location location) {
		return region.contains(BukkitAdapter.asBlockVector(location));
	}

}
