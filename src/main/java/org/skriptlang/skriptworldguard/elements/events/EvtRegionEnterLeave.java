package org.skriptlang.skriptworldguard.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionEnterLeaveEvent;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class EvtRegionEnterLeave extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtRegionEnterLeave.class, "Region Enter/Leave")
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
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		regions = (Literal<WorldGuardRegion>) args[0];
		enter = matchedPattern <= 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof RegionEnterLeaveEvent enterLeaveEvent
				&& enterLeaveEvent.isEntering() == enter
				&& (regions == null || regions.check(enterLeaveEvent, region -> region.equals(enterLeaveEvent.getRegion())));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (enter) {
			builder.append("entering");
		} else {
			builder.append("leaving");
		}
		builder.append("of");
		if (regions == null) {
			builder.append("a region");
		} else {
			builder.append(regions);
		}
		return builder.toString();
	}

}
