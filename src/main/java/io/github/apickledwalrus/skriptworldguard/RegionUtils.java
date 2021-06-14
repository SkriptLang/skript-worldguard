package io.github.apickledwalrus.skriptworldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class RegionUtils {

	/**
	 * A helper method to simplify getting a region in a world.
	 * @param world The world of the region.
	 * @param id The ID of the region. This method will perform ID validation.
	 * @return The region with the given id in the given world, or null if the region does not exist
	 * or the world's {@link RegionManager} could not be retrieved.
	 */
	@Nullable
	public static ProtectedRegion getRegion(World world, String id) {
		RegionManager rm;
		if (ProtectedRegion.isValidId(id) && (rm = getRegionManager(world)) != null) {
			return rm.getRegion(id);
		}
		return null;
	}

	/**
	 * A helper method to provide a standardized way to stringify a {@link ProtectedRegion}.
	 * @param world The world of a region.
	 * @param id The ID of a region.
	 * @return A stringified version of a region.
	 * @see SkriptRegion#toString()
	 */
	public static String toString(World world, String id) {
		return "region \"" + id + "\" in the world \"" + world.getName() + "\"";
	}

	/**
	 * A helper method to simplify getting the RegionManager for a world.
	 * @param world The world to get the RegionManager for.
	 * @return The RegionManager for the given world, or null if:
	 * - region data for the given world has not been loaded
	 * - region data for the given world has failed to load
	 * - support for regions has been disabled
	 */
	@Nullable
	public static RegionManager getRegionManager(World world) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
	}

}
