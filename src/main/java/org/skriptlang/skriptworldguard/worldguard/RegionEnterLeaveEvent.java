package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldguard.session.MoveType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionEnterLeaveEvent extends Event implements Cancellable {

	private final static HandlerList handlers = new HandlerList();
	private boolean cancelled = false;

	private final Player player;
	private final WorldGuardRegion region;
	private final MoveType moveType;
	private final boolean enter;

	public RegionEnterLeaveEvent(Player player, WorldGuardRegion region, MoveType moveType, boolean enter) {
		this.player = player;
		this.region = region;
		this.moveType = moveType;
		this.enter = enter;
	}

	public Player getPlayer() {
		return player;
	}

	public WorldGuardRegion getRegion() {
		return region;
	}

	/**
	 * @return Whether the player in this region event is entering or leaving the region
	 */
	public boolean isEntering() {
		return enter;
	}

	public MoveType getMoveType() {
		return moveType;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
