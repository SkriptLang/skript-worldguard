package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Name("Region Points")
@Description({
	"An expression to obtain the points of a region.",
	"Note that when obtaining the points of a region, the region itself is projected onto an X-Z plane." +
		" Thus, the points do not have a y-component." +
		" However, for simplicity, the y-component of the minimum point is used."
})
@Example("""
	command /highlight-points <text>:
		trigger:
			set the blocks at the points of the region text-argument to red wool
	""")
@Since("1.0")
public class ExprRegionPoints extends PropertyExpression<WorldGuardRegion, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprRegionPoints.class, Location.class, "", "", false)
				.supplier(ExprRegionPoints::new)
				.clearPatterns() // overwrite them
				.addPatterns(getPatterns("(min|:max)[imum] point[s]", "worldguardregions"))
				.addPatterns(getPatterns("points", "worldguardregions"))
				.build());
	}

	private boolean isMax;
	private boolean isAll;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		isAll = matchedPattern > 1;
		//noinspection unchecked
		setExpr((Expression<? extends WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Location[] get(Event event, WorldGuardRegion[] regions) {
		List<Location> locations = new ArrayList<>();
		if (isAll) {
			for (WorldGuardRegion region : regions) {
				int minY = region.region().getMinimumPoint().getY();
				region.region().getPoints().stream()
						.map(point -> BukkitAdapter.adapt(region.world(), point.toBlockVector3(minY)))
						.forEach(locations::add);
			}
		} else if (isMax) {
			for (WorldGuardRegion region : regions) {
				locations.add(BukkitAdapter.adapt(region.world(), region.region().getMaximumPoint()));
			}
		} else {
			for (WorldGuardRegion region : regions) {
				locations.add(BukkitAdapter.adapt(region.world(), region.region().getMinimumPoint()));
			}
		}
		return locations.toArray(new Location[0]);
	}

	@Override
	public boolean isSingle() {
		return !isAll && getExpr().isSingle();
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (isAll) {
			builder.append("points");
		} else {
			if (isMax) {
				builder.append("maximum");
			} else {
				builder.append("minimum");
			}
			if (getExpr().isSingle()) {
				builder.append("point");
			} else {
				builder.append("points");
			}
		}
		builder.append("of", getExpr());
		return builder.toString();
	}

}
