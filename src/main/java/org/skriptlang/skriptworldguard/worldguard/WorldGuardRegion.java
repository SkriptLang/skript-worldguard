package org.skriptlang.skriptworldguard.worldguard;

import ch.njol.skript.lang.util.common.AnyContains;
import ch.njol.skript.lang.util.common.AnyNamed;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * A utility record for associating a {@link ProtectedRegion} with a {@link World}.
 * @param world The world {@code region} is within.
 * @param region WorldGuard region.
 */
public record WorldGuardRegion(World world, ProtectedRegion region)
		implements Comparable<WorldGuardRegion>, AnyNamed, AnyContains<Object> {

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
		if (other instanceof WorldGuardRegion otherRegion) {
			return otherRegion.world() == world && otherRegion.region().getId().equals(region.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return toString(world, region.getId());
	}

	/*
	 * Comparable
	 */

	@Override
	public int compareTo(@NotNull WorldGuardRegion other) {
		return Integer.compare(this.region.getPriority(), other.region.getPriority());
	}

	/*
	 * AnyNamed
	 */

	@Override
	public String name() {
		return region.getId();
	}

	/*
	 * AnyContains
	 */

	@Override
	public boolean contains(Object object) {
		Location location = Converters.convert(object, Location.class);
		return location != null && region.contains(BukkitAdapter.asBlockVector(location));
	}

	@Override
	public boolean isSafeToCheck(Object value) {
		return value != null;
	}

}
