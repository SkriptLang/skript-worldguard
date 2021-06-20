package io.github.apickledwalrus.skriptworldguard.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.sk89q.worldguard.session.MoveType;
import io.github.apickledwalrus.skriptworldguard.worldguard.RegionEnterLeaveEvent;
import io.github.apickledwalrus.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, WorldGuardRegion.class, new Getter<WorldGuardRegion, RegionEnterLeaveEvent>() {
			@Override
			public WorldGuardRegion get(RegionEnterLeaveEvent e) {
				return e.getRegion();
			}
		}, 0);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, Player.class, new Getter<Player, RegionEnterLeaveEvent>() {
			@Override
			public Player get(RegionEnterLeaveEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, MoveType.class, new Getter<MoveType, RegionEnterLeaveEvent>() {
			@Override
			public MoveType get(RegionEnterLeaveEvent e) {
				return e.getMoveType();
			}
		}, 0);
	}

	private Literal<WorldGuardRegion> regions;
	private boolean enter;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		regions = (Literal<WorldGuardRegion>) args[0];
		enter = matchedPattern <= 1;
		return true;
	}

	@Override
	public boolean check(Event e) {
		RegionEnterLeaveEvent event = (RegionEnterLeaveEvent) e;
		if (event.isEntering() != enter) { // This is a region enter event but we want a region leave event
			return false;
		} else if (regions == null) { // There are no regions to check so it is valid
			return true;
		}
		return regions.check(e, region -> region.equals(event.getRegion()));
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		return (enter ? "entering" : "leaving") + " of " + (regions == null ? "a region" : regions.toString(e, debug));
	}

}
