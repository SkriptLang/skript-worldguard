package org.skriptlang.skriptworldguard.elements.events;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionEnterLeaveEvent;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class EvtRegionEnterLeave extends SkriptEvent implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		String regionPattern = "[the] [worldguard] region[s] [with [the] (name[s]|id[s])|named] %*strings% (in|of) [[the] world] %*string%";
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtRegionEnterLeave.class, "Region Enter/Leave")
				.supplier(EvtRegionEnterLeave::new)
				.addEvent(RegionEnterLeaveEvent.class)
				.addPatterns("region enter[ing]",
						"enter[ing] of " + regionPattern,
						"region (exit[ing]|leav(e|ing))",
						"exit[ing] of " + regionPattern)
				.addDescription("Called when a player enters or leaves a region.")
				.addExample("""
						on region enter:
							send "You entered %region%"
						""")
				.addSince("1.0")
				.build());
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, WorldGuardRegion.class, RegionEnterLeaveEvent::getRegion);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, Player.class, RegionEnterLeaveEvent::getPlayer);
		EventValues.registerEventValue(RegionEnterLeaveEvent.class, MoveType.class, RegionEnterLeaveEvent::getMoveType);
	}

	private Node node;

	private @Nullable Literal<String> regionIds;
	private @Nullable Literal<String> world;
	private boolean isEntering;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		node = getParser().getNode();
		if (args.length != 0) {
			//noinspection unchecked
			regionIds = (Literal<String>) args[0];
			//noinspection unchecked
			world = (Literal<String>) args[1];
		}
		isEntering = matchedPattern <= 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof RegionEnterLeaveEvent enterLeaveEvent) || enterLeaveEvent.isEntering() != isEntering) {
			return false;
		}
		if (regionIds == null) {
			return true;
		}
		assert world != null;

		// validate world
		World world = Bukkit.getWorld(this.world.getSingle());
		if (world == null) {
			error("The world '" + this.world.getSingle() + "' does not exist");
			return false;
		}

		// map regions
		String[] regionIds = this.regionIds.getAll();
		WorldGuardRegion[] regions = new WorldGuardRegion[regionIds.length];
		for (int i = 0; i < regionIds.length; i++) {
			regions[i] = RegionUtils.getRegion(world, regionIds[i]);
			if (regions[i] == null) {
				error("The region '" + regionIds[i] + "' does not exist in the world '" + world.getName() + "'");
				return false;
			}
		}

		return SimpleExpression.check(regions, region -> region.equals(enterLeaveEvent.getRegion()), false, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (regionIds == null) {
			builder.append("region");
		}
		if (isEntering) {
			builder.append("enter");
		} else {
			builder.append("exit");
		}
		if (regionIds != null) {
			assert world != null;
			builder.append("of");
			if (regionIds.isSingle()) {
				builder.append("region");
			} else {
				builder.append("regions");
			}
			builder.append("named", regionIds, "in the world", world);
		}
		return builder.toString();
	}

	@Override
	public Node getNode() {
		return node;
	}

}
