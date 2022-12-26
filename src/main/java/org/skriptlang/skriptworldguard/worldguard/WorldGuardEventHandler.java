package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class WorldGuardEventHandler extends Handler {

	public static class Factory extends Handler.Factory<WorldGuardEventHandler> {
		@Override
		public WorldGuardEventHandler create(Session session) {
			return new WorldGuardEventHandler(session);
		}
	}

	/**
	 * Create a new event handler.
	 * @param session The session
	 */
	public WorldGuardEventHandler(Session session) {
		super(session);
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
		Player player = Bukkit.getPlayer(localPlayer.getUniqueId());
		if (player == null) // this might be some sort of fake player (ex: Citizens), don't process anything
			return true;
		boolean cancellable = moveType.isCancellable();

		for (ProtectedRegion region : exited) { // Call leave events
			RegionEnterLeaveEvent event = new RegionEnterLeaveEvent(player, new WorldGuardRegion(player.getWorld(), region), moveType, false);
			Bukkit.getPluginManager().callEvent(event);
			if (cancellable && event.isCancelled()) {
				return false;
			}
		}

		for (ProtectedRegion region : entered) { // Call enter events
			RegionEnterLeaveEvent event = new RegionEnterLeaveEvent(player, new WorldGuardRegion(player.getWorld(), region), moveType, true);
			Bukkit.getPluginManager().callEvent(event);
			if (cancellable && event.isCancelled()) {
				return false;
			}
		}

		return true;
	}

}
