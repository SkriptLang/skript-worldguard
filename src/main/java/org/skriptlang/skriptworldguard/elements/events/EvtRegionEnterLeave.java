package org.skriptlang.skriptworldguard.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionEnterLeaveEvent;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class EvtRegionEnterLeave extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtRegionEnterLeave.class, "WorldGuard Region Enter")
				.supplier(EvtRegionEnterLeave::new)
				.addEvent(RegionEnterLeaveEvent.class)
				.addPatterns("enter[ing] of ([a] region|%-worldguardregions%)",
						"(region|%-worldguardregions%) enter[ing]",
						"(leav(e|ing)|exit[ing]) of ([a] region|%-worldguardregions%)",
						"(region|%-worldguardregions%) (leav(e|ing)|exit[ing])")
				.addDescription("Called when a player enters or leaves a region (or the specified region(s))")
				.addExample("""
						on region enter:
							send "You entered %region%"
						""")
				.addRequiredPlugin("WorldGuard 7")
				.addSince("1.0")
				.build());
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, WorldGuardRegion.class, RegionEnterLeaveEvent::getRegion);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, Player.class, RegionEnterLeaveEvent::getPlayer);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, MoveType.class, RegionEnterLeaveEvent::getMoveType);
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
