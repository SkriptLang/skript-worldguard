package io.github.apickledwalrus.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apickledwalrus.skriptworldguard.RegionUtils;
import io.github.apickledwalrus.skriptworldguard.SkriptRegion;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Regions")
@Description("An expression that returns a region from a region id and world. Please note that region ids are case insensitive.")
@Examples("the region \"region\" in world(\"world\"")
@Since("1.0")
public class ExprRegionNamed extends SimpleExpression<SkriptRegion> {

	static {
		Skript.registerExpression(ExprRegionNamed.class, SkriptRegion.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] [worldguard] region[s] [(with (name[s]|id[s])|named)] %strings% (in|of) [[the] world] %world%"
		);
	}

	private Expression<String> ids;
	private Expression<World> world;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ids = (Expression<String>) exprs[0];
		world = (Expression<World>) exprs[1];
		return true;
	}

	@Override
	protected SkriptRegion[] get(Event e) {
		String[] ids = this.ids.getArray(e);
		World world = this.world.getSingle(e);
		if (ids.length == 0 || world == null) {
			return new SkriptRegion[0];
		}
		SkriptRegion[] regions = new SkriptRegion[ids.length];
		for (int i = 0; i < ids.length; i++) {
			ProtectedRegion region = RegionUtils.getRegion(world, ids[i]); // Will validate IDs
			if (region != null) {
				regions[i] = new SkriptRegion(world, region);
			}
		}
		return regions;
	}

	@Override
	public boolean isSingle() {
		return ids.isSingle();
	}

	@Override
	@NotNull
	public Class<? extends SkriptRegion> getReturnType() {
		return SkriptRegion.class;
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		boolean isSingle = ids.isSingle();
		return "the " + (isSingle ? "region" : "regions")
				+ " with the " + (isSingle ? "id" : "ids") + " " + ids.toString(e, debug)
				+ " in the world " + world.toString(e, debug);
	}

}
