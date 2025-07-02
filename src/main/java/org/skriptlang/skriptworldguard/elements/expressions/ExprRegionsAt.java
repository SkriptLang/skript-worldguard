package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
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
@Description("An expression that returns the regions at the given locations")
@Examples({
	"on click on a sign:",
	"\tline 1 of the clicked block is \"[region info]\"",
	"\tset {_regions::*} to regions at the clicked block",
	"\tif {_regions::*} is empty:",
	"\t\tmessage \"No regions exist at this sign.\"",
	"\telse:",
	"\t\tmessage \"Regions at this sign: <gold>%{_regions::*}%<reset>.\""
})
@RequiredPlugins("WorldGuard 7")
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
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		locations = Direction.combine(
				(Expression<? extends Direction>) exprs[0],
				(Expression<? extends Location>) exprs[1]
		);
		return true;
	}

	@Override
	protected WorldGuardRegion @NotNull [] get(@NotNull Event event) {
		Location[] locations = this.locations.getArray(event);
		if (locations.length == 0) {
			return new WorldGuardRegion[0];
		}
		List<WorldGuardRegion> regions = new ArrayList<>();
		for (Location location : locations) {
			regions.addAll(RegionUtils.getRegionsAt(location));
		}
		return regions.toArray(new WorldGuardRegion[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public @NotNull Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return "the regions " + locations.toString(event, debug);
	}

}
