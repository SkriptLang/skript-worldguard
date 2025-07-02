package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Location;
import org.bukkit.event.Event;

@Name("Region Contains")
@Description("A condition that tests whether the given locations are inside of the given regions")
@Examples({
	"player is in the region {spawnregion}",
	"on region enter:",
	"\tworldguard region the region contains {teamflags::red}",
	"\tmessage \"The red flag is near!\""
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondRegionContains extends Condition {

	public static void register(SyntaxRegistry registry) {
		// TODO see what can be done about requiring 'region' as it looks quite dumb in region events with the region expression
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
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		boolean locationsFirst = matchedPattern % 2 != 0;
		regions = (Expression<WorldGuardRegion>) exprs[locationsFirst ? 1 : 0];
		locations = (Expression<Location>) exprs[locationsFirst ? 0 : 1];
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(@NotNull Event event) {
		Location[] locations = this.locations.getAll(event);
		return regions.check(event, region -> SimpleExpression.check(
				locations,
				location -> region.getRegion().contains(BukkitAdapter.asBlockVector(location)),
				false,
				this.locations.getAnd()
		), isNegated());
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return regions.toString(event, debug) + " contain" + (regions.isSingle() ? "s" : "") +
				" " + locations.toString(event, debug);
	}

}
