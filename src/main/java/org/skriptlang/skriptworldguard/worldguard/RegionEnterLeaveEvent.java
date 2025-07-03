package org.skriptlang.skriptworldguard.worldguard;

import com.sk89q.worldguard.session.MoveType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RegionEnterLeaveEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final WorldGuardRegion region;
	private final MoveType moveType;
	private final boolean enter;
	private boolean cancelled;

	public RegionEnterLeaveEvent(Player player, WorldGuardRegion region, MoveType moveType, boolean enter) {
		super(player);
		this.region = region;
		this.moveType = moveType;
		this.enter = enter;
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
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

}
