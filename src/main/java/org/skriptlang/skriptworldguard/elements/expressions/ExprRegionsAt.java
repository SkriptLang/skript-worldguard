package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Name("Regions At")
@Description({
	"An expression to obtain the regions at a specific location.",
	"Note that the regions will be returned in ascending order by priority."
})
@Example("""
	on right click:
		the clicked block is tagged with minecraft tag "all_signs"
		line 1 of the clicked block is "[Region Info]"
		set {_regions::*} to the regions at the clicked block
		if {_regions::*} is not set:
			message "No regions exist at this sign."
		else:
			message "You are in: %{_regions::*}%."
	""")
@Since("1.0")
public class ExprRegionsAt extends SimpleExpression<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRegionsAt.class, WorldGuardRegion.class)
				.supplier(ExprRegionsAt::new)
				.addPattern("[the] regions %direction% %locations%")
				.build());
	}

	private Expression<Location> locations;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		locations = Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]);
		return true;
	}

	@Override
	protected WorldGuardRegion[] get(Event event) {
		Location[] locations = this.locations.getArray(event);
		if (locations.length == 0) {
			return new WorldGuardRegion[0];
		}
		List<WorldGuardRegion> regions = new ArrayList<>();
		for (Location location : locations) {
			regions.addAll(RegionUtils.getRegionsAt(location));
		}
		regions.sort(WorldGuardRegion::compareTo);
		return regions.toArray(new WorldGuardRegion[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	public boolean isLoopOf(String input) {
		return input.equalsIgnoreCase("region");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the regions " + locations.toString(event, debug);
	}

}
