package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Can Build In Regions")
@Description("A condition to test whether a player can build in a region or at a specific location.")
@Example("""
	command /setblock <material>:
		description: Sets your targeted block to a different type.
		trigger:
			if the player cannot build at the targeted block:
				message "<red>You do not have permission to modify your targeted block!"
			else:
				set the targeted block to the first argument
	""")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondCanBuildInRegions extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondCanBuildInRegions.class)
				.supplier(CondCanBuildInRegions::new)
				.addPatterns(PropertyCondition.getPatterns(PropertyType.BE, "allowed to build (%-directions% %-locations%|[in] %-worldguardregions%)", "players"))
				.addPatterns(PropertyCondition.getPatterns(PropertyType.CAN, "build (%-directions% %-locations%|[in] %-worldguardregions%)", "players"))
				.build());
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private Expression<WorldGuardRegion> regions;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) exprs[0];
		if (exprs[1] != null) {
			//noinspection unchecked
			locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		} else {
			//noinspection unchecked
			regions = (Expression<WorldGuardRegion>) exprs[3];
		}
		setNegated(matchedPattern % 2 == 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (locations != null) {
			Location[] locations = this.locations.getAll(event); // get all to avoid double-eval + permit or lists.
			return players.check(event, player -> SimpleExpression.check(locations,
					location -> RegionUtils.canBuild(player, location), false, this.locations.getAnd()), isNegated());
		} else {
			assert regions != null;
			WorldGuardRegion[] regions = this.regions.getAll(event); // get all to avoid double-eval + permit or lists.
			return players.check(event, player -> SimpleExpression.check(regions,
					region -> RegionUtils.canBuild(player, region), false, this.regions.getAnd()), isNegated());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(players, "can build");
		if (locations != null) {
			builder.append(locations);
		} else {
			builder.append(regions);
		}
		return builder.toString();
	}

}
