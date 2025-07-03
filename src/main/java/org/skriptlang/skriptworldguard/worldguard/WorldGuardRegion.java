package org.skriptlang.skriptworldguard.worldguard;

import ch.njol.skript.lang.util.common.AnyContains;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * A utility record for associating a {@link ProtectedRegion} with a {@link World}.
 * @param world The world {@code region} is within.
 * @param region WorldGuard region.
 */
public record WorldGuardRegion(World world, ProtectedRegion region) implements AnyContains<Object> {

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

	/*
	 * AnyContains
	 */

	@Override
	public boolean contains(Object object) {
		Location location = Converters.convert(object, Location.class);
		return location != null && region().contains(BukkitAdapter.asBlockVector(location));
	}

	@Override
	public boolean isSafeToCheck(Object value) {
		return value != null;
	}

}
