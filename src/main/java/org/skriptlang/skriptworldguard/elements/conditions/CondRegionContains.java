package org.skriptlang.skriptworldguard.elements.conditions;

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
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Location;
import org.bukkit.event.Event;

@Name("Region Contains")
@Description("A condition to test whether a location is inside of a region.")
@Example("the player is in the region {spawnregion}")
@Example("""
	on region enter:
		if the region the region contains {teamflags::red}
			message "The red flag is nearby!"
	""")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondRegionContains extends Condition {

	public static void register(SyntaxRegistry registry) {
		// TODO see what can be done about requiring 'region' as it looks quite dumb in region events with the region expression
		// TODO any contains (solves above)
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondRegionContains.class)
				.supplier(CondRegionContains::new)
				.addPatterns("[the] [worldguard] region %worldguardregions% contain[s] %locations%",
						"%locations% (is|are) ([contained] in|part of) [the] [worldguard] region %worldguardregions%",
						"[the] [worldguard] region %worldguardregions% (do|does)(n't| not) contain %locations%",
						"%locations% (is|are)(n't| not) (contained in|part of) [the] [worldguard] region %worldguardregions%")
				.build());
	}

	private Expression<WorldGuardRegion> regions;
	private Expression<Location> locations;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isRegionFirst = matchedPattern % 2 == 0;
		//noinspection unchecked
		regions = (Expression<WorldGuardRegion>) exprs[isRegionFirst ? 0 : 1];
		//noinspection unchecked
		locations = (Expression<Location>) exprs[isRegionFirst ? 1 : 0];
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location[] locations = this.locations.getAll(event);
		return regions.check(event, region -> SimpleExpression.check(locations,
				location -> region.region().contains(BukkitAdapter.asBlockVector(location)),
				false, this.locations.getAnd()), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(regions);
		if (regions.isSingle()) {
			builder.append("contain");
		} else {
			builder.append("contains");
		}
		builder.append(locations);
		return builder.toString();
	}

}
