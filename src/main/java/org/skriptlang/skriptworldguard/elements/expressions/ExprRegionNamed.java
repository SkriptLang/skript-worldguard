package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.bukkit.World;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Name("Regions")
@Description({
	"An expression that obtains a region from an ID and world.",
	"Please note that region IDs are case insensitive."
})
@Example("the region \"region\" in world(\"world\"")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprRegionNamed extends SimpleExpression<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRegionNamed.class, WorldGuardRegion.class)
				.supplier(ExprRegionNamed::new)
				.addPatterns("[the] [worldguard] region[s] [named] %strings% [(in|of) %world%]",
						"[the] [worldguard] region[s] with [the] (name[s]|id[s]) %strings% [(in|of) %world%]")
				.build());
	}

	private Expression<String> ids;
	private Expression<World> world;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		ids = (Expression<String>) exprs[0];
		//noinspection unchecked
		world = (Expression<World>) exprs[1];
		return true;
	}

	@Override
	protected WorldGuardRegion [] get(Event event) {
		World world = this.world.getSingle(event);
		if (world == null) {
			return new WorldGuardRegion[0];
		}

		List<WorldGuardRegion> regions = new ArrayList<>();
		for (String id : ids.getArray(event)) {
			WorldGuardRegion region = RegionUtils.getRegion(world, id);
			if (region != null) {
				regions.add(region);
			}
		}

		return regions.toArray(new WorldGuardRegion[0]);
	}

	@Override
	public boolean isSingle() {
		return ids.isSingle();
	}

	@Override
	public Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		boolean isSingle = ids.isSingle();
		builder.append("the");
		if (isSingle) {
			builder.append("region");
		} else {
			builder.append("regions");
		}
		builder.append("with the");
		if (isSingle) {
			builder.append("id");
		} else {
			builder.append("ids");
		}
		builder.append(ids, "in", world);
		return builder.toString();
	}

}
