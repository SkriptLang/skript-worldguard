package io.github.apickledwalrus.skriptworldguard.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.math.BlockVector3;
import io.github.apickledwalrus.skriptworldguard.worldguard.WorldGuardRegion;
import io.github.apickledwalrus.skriptworldguard.worldguard.RegionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	static {
		Skript.registerCondition(CondCanBuildInRegions.class,
				"%players% (can|(is|are) allowed to) build (%-directions% %-locations%|[in] %-worldguardregions%)",
				"%players% (can('t|not)|(is|are)(n't| not) allowed to) build (%-directions% %-locations%|[in] %-worldguardregions%)"
		);
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private Expression<WorldGuardRegion> regions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		if (exprs[1] != null) { // We are using directions and locations
			locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		} else {
			regions = (Expression<WorldGuardRegion>) exprs[3];
		}
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return players.check(e,
				player -> {
					if (locations != null) {
						return locations.check(e,
								location -> RegionUtils.canBuild(player, location));
					} else {
						return regions.check(e, region -> { // Convert region to location essentially
							BlockVector3 minPoint = region.getRegion().getMinimumPoint();
							Location location = new Location(region.getWorld(), minPoint.getX(), minPoint.getY(), minPoint.getZ());
							return RegionUtils.canBuild(player, location);
						});
					}
				}, isNegated());
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		return players.toString(e, debug) + " can build " + (locations != null ? locations.toString(e, debug) : regions.toString(e, debug));
	}

}
