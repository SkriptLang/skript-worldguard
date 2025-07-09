package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Region Priority")
@Description({
	"An expression to obtain and change the priority of a region.",
	"When regions overlap, the priority is used to determine properties such as" +
		" membership, ability to build, and flag values."
})
@Example("""
	function get_highest_priority_region(location: location) returns worldguard region:
		set {_regions::*} to the regions at {_location}
		sort {_regions::*} in descending order by priority of input
		return {_regions::1}
	""")
@Since("1.0")
public class ExprRegionPriority extends SimplePropertyExpression<WorldGuardRegion, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprRegionPriority.class, Integer.class,
				"[region] priority", "worldguardregions", false)
						.supplier(ExprRegionPriority::new)
						.build());
	}

	@Override
	public Integer convert(WorldGuardRegion region) {
		return region.region().getPriority();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET -> new Class[]{Integer.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int change = delta != null ? (Integer) delta[0] : 0;
		WorldGuardRegion[] regions = getExpr().getArray(event);
		switch (mode) {
			case SET, DELETE, RESET:
				for (WorldGuardRegion region : regions) {
					region.region().setPriority(change);
				}
				break;
			case REMOVE:
				change *= -1;
				//$FALL-THROUGH$
			case ADD:
				for (WorldGuardRegion region : regions) {
					ProtectedRegion protectedRegion = region.region();
					protectedRegion.setPriority(change + protectedRegion.getPriority());
				}
				break;
		}
	}

	@Override
	protected String getPropertyName() {
		return "priority";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

}
