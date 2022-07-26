package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public class WorldGuardRegion {

	private final World world;
	private final ProtectedRegion region;

	public WorldGuardRegion(World world, ProtectedRegion region) {
		this.world = world;
		this.region = region;
	}

	public World getWorld() {
		return world;
	}

	public ProtectedRegion getRegion() {
		return region;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WorldGuardRegion) {
			WorldGuardRegion other = (WorldGuardRegion) o;
			return other.getRegion().getId().equals(region.getId()) && other.getWorld() == world;
		}
		return false;
	}

	@Override
	public String toString() {
		return RegionUtils.toString(world, region.getId());
	}

}
