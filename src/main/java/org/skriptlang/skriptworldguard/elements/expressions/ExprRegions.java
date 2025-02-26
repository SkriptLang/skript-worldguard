package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Name("All Regions")
@Description("Get all regions from all worlds or of a specific world.")
@Examples({
		"send all regions",
		"set {_regions::*} to the regions in world \"world\""
})
@Since("INSERT VERSION")
@RequiredPlugins("WorldGuard 7")
public class ExprRegions extends SimpleExpression<WorldGuardRegion> {

	static {
		Skript.registerExpression(ExprRegions.class, WorldGuardRegion.class, ExpressionType.SIMPLE,
				"(all [[of] the]|the) regions [(in|of|from) %-worlds%]");
	}

	private @Nullable Expression<World> worlds;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0] != null)
			//noinspection unchecked
			worlds = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	protected WorldGuardRegion @Nullable [] get(Event event) {
		if (worlds == null) {
			return RegionUtils.getRegions();
		} else {
			World[] worlds = this.worlds.getArray(event);
			if (worlds.length == 0)
				return null;
			List<WorldGuardRegion> regions = new ArrayList<>();
			for (World world : worlds) {
				WorldGuardRegion[] worldRegions = RegionUtils.getRegions(world);
				if (worldRegions == null || worldRegions.length == 0)
					continue;
				regions.addAll(Arrays.stream(worldRegions).collect(Collectors.toList()));
			}
			return regions.toArray(new WorldGuardRegion[0]);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the regions" + (worlds == null ? "" : " " + worlds.toString(event, debug));
	}

}
