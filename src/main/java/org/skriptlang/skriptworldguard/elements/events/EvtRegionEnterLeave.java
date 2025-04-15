package org.skriptlang.skriptworldguard.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skriptworldguard.worldguard.RegionEnterLeaveEvent;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class EvtRegionEnterLeave extends SkriptEvent {

	static {
		Skript.registerEvent("WorldGuard Region Enter", EvtRegionEnterLeave.class, RegionEnterLeaveEvent.class,
				"enter[ing] of ([a] region|%-worldguardregions%)",
				"(region|%-worldguardregions%) enter[ing]",
				"(leav(e|ing)|exit[ing]) of ([a] region|%-worldguardregions%)",
				"(region|%-worldguardregions%) (leav(e|ing)|exit[ing])")
				.description("Called when a player enters or leaves a region or the given region(s)")
				.examples("on region enter:",
						"\tsend \"You entered %region%\"")
				.requiredPlugins("WorldGuard 7")
				.since("1.0");
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, WorldGuardRegion.class,
					RegionEnterLeaveEvent::getRegion, EventValues.TIME_NOW);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, Player.class,
				RegionEnterLeaveEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, MoveType.class,
				RegionEnterLeaveEvent::getMoveType, EventValues.TIME_NOW);
	}

	private @Nullable Literal<WorldGuardRegion> regions;
	private boolean enter;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
		regions = (Literal<WorldGuardRegion>) args[0];
		enter = matchedPattern <= 1;
		return true;
	}

	@Override
	public boolean check(@NotNull Event event) {
		if (!(event instanceof RegionEnterLeaveEvent enterLeaveEvent))
			return false;
		if (enterLeaveEvent.isEntering() != enter) { // This is a region enter event, but we want a region leave event
			return false;
		} else if (regions == null) { // There are no regions to check so it is valid
			return true;
		}
		return regions.check(enterLeaveEvent, region -> region.equals(enterLeaveEvent.getRegion()));
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return (enter ? "entering" : "leaving") + " of "
				+ (regions == null ? "a region" : regions.toString(event, debug));
	}

}
