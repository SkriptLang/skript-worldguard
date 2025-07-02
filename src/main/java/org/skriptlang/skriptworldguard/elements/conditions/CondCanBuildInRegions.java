package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Can Build In Regions")
@Description("A condition that tests whether the given players can build in the given regions or the regions of the given locations.")
@Examples({
	"command /setblock <material>:",
		"\tdescription: set the block at your crosshair to a different type",
		"\ttrigger:",
			"\t\tif the player cannot build at the targeted block:",
				"\t\t\tmessage \"<red>You do not have permission to change blocks there!\"",
			"\t\telse:",
				"\t\t\tset the targeted block to argument"
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondCanBuildInRegions extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondCanBuildInRegions.class)
				.supplier(CondCanBuildInRegions::new)
				.addPatterns("%players% (can|(is|are) allowed to) build (%-directions% %-locations%|[in] %-worldguardregions%)",
						"%players% (can('t|not)|(is|are)(n't| not) allowed to) build (%-directions% %-locations%|[in] %-worldguardregions%)")
				.build());
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private Expression<WorldGuardRegion> regions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		if (exprs[1] != null) { // We are using directions and locations
			locations = Direction.combine(
					(Expression<? extends Direction>) exprs[1],
					(Expression<? extends Location>) exprs[2]
			);
		} else {
			regions = (Expression<WorldGuardRegion>) exprs[3];
		}
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(@NotNull Event event) {
		if (locations != null) {
			Location[] locations = this.locations.getAll(event); // get all to avoid double-eval + permit or lists.
			return players.check(event, player -> SimpleExpression.check(
					locations,
					location -> RegionUtils.canBuild(player, location),
					false,
					this.locations.getAnd()
			), isNegated());
		} else {
			assert regions != null;
			WorldGuardRegion[] regions = this.regions.getAll(event); // get all to avoid double-eval + permit or lists.
			return players.check(event, player -> SimpleExpression.check(
					regions,
					region -> RegionUtils.canBuild(player, region),
					false,
					this.regions.getAnd()
			), isNegated());
		}
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		assert (locations != null && regions == null) || (locations == null && regions != null);
		return players.toString(event, debug) + " can build " + ((locations != null ? locations : regions).toString(event, debug));
	}

}
