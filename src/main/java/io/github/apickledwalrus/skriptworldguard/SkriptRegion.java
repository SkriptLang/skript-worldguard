package io.github.apickledwalrus.skriptworldguard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public class SkriptRegion {

	private final World world;
	private final ProtectedRegion region;

	public SkriptRegion(World world, ProtectedRegion region) {
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
	public String toString() {
		return RegionUtils.toString(world, region.getId());
	}

}
