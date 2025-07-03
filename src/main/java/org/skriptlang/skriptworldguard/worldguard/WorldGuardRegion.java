package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

/**
 * A utility record for associating a {@link ProtectedRegion} with a {@link World}.
 * @param world The world {@code region} is within.
 * @param region WorldGuard region.
 */
public record WorldGuardRegion(World world, ProtectedRegion region) {

	@Override
	public boolean equals(Object other) {
		if (other instanceof WorldGuardRegion otherRegion) {
			return otherRegion.world() == world && otherRegion.region().getId().equals(region.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return RegionUtils.toString(world, region.getId());
	}

}
