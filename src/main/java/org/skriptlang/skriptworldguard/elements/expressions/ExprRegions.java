package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
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
	"An expression to obtain all regions of a specific world or the region with a specific name in a world.",
	"Please note that region names (IDs) are case insensitive."
})
@Example("the region \"region\" in world(\"world\"")
@Example("all of the regions in the player's world")
@Since("1.0")
public class ExprRegions extends SimpleExpression<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRegions.class, WorldGuardRegion.class)
				.supplier(ExprRegions::new)
				.addPatterns("[the] [worldguard] region[s] [named] %strings% [(in|of) %world%]",
						"[the] [worldguard] region[s] with [the] (name[s]|id[s]) %strings% [(in|of) %world%]",
						"[all [[of] the]|the] [worldguard] regions [(in|of) %worlds%]")
				.build());
	}

	private @Nullable Expression<String> ids;
	private Expression<World> worlds;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs.length == 2) {
			//noinspection unchecked
			ids = (Expression<String>) exprs[0];
		}
		//noinspection unchecked
		worlds = (Expression<World>) exprs[exprs.length - 1];
		return true;
	}

	@Override
	protected WorldGuardRegion [] get(Event event) {
		List<WorldGuardRegion> regions = new ArrayList<>();
		if (ids == null) {
			for (World world : this.worlds.getArray(event)) {
				regions.addAll(RegionUtils.getRegions(world));
			}
		} else {
			assert worlds != null;
			World world = this.worlds.getSingle(event); // single for this pattern
			if (world == null) {
				return new WorldGuardRegion[0];
			}
			for (String id : ids.getArray(event)) {
				WorldGuardRegion region = RegionUtils.getRegion(world, id);
				if (region != null) {
					regions.add(region);
				}
			}
		}
		return regions.toArray(new WorldGuardRegion[0]);
	}

	@Override
	public boolean isSingle() {
		return ids != null && ids.isSingle();
	}

	@Override
	public Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		boolean isSingle = isSingle();
		builder.append("the");
		if (isSingle) {
			builder.append("region");
		} else {
			builder.append("regions");
		}
		if (ids != null) {
			builder.append("with the");
			if (isSingle) {
				builder.append("id");
			} else {
				builder.append("ids");
			}
			builder.append(ids);
		}
		builder.append("in", worlds);
		return builder.toString();
	}

}
