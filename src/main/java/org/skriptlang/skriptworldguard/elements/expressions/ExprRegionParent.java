package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.util.ArrayList;
import java.util.List;

@Name("Region Parent")
@Description({
	"An expression to obtain and change the direct parent of a region.",
	"It is also possible to obtain all parents of a region (e.g., including a parent region's parent region).",
	"When a region has a parent, it inherits the parents members, owners, and flags (that aren't defined on the child)."
})
@Example("""
	command /setparent <text> <text>:
		trigger:
			set the parent of the region text-argument-1 to the region text-argument-2
	""")
@Example("if any of all of the parent regions of {_region} are global")
@Since("1.0")
public class ExprRegionParent extends SimplePropertyExpression<WorldGuardRegion, WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprRegionParent.class, WorldGuardRegion.class,
				"parent region[s]", "worldguardregions", false)
						.supplier(ExprRegionParent::new)
						.addPattern("all [[of] the] parent regions of %worldguardregions%")
						.build());
	}

	private boolean isAll;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isAll = matchedPattern == 2;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	protected WorldGuardRegion[] get(Event event, WorldGuardRegion[] regions) {
		if (isAll) {
			List<WorldGuardRegion> parents = new ArrayList<>();
			for (WorldGuardRegion region : regions) {
				ProtectedRegion parent = region.region().getParent();
				while (parent != null) {
					parents.add(new WorldGuardRegion(region.world(), parent));
					parent = parent.getParent();
				}
			}
			return parents.toArray(new WorldGuardRegion[0]);
		}
		return super.get(event, regions);
	}

	@Override
	public WorldGuardRegion convert(WorldGuardRegion region) {
		return new WorldGuardRegion(region.world(), region.region().getParent());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (isAll) {
			Skript.error("It is not possible to change all the parents of a region");
			return null;
		}
		return switch (mode) {
			case SET, DELETE, RESET -> new Class[]{WorldGuardRegion.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ProtectedRegion newRegion = delta != null ? ((WorldGuardRegion) delta[0]).region() : null;
		for (WorldGuardRegion region : getExpr().getArray(event)) {
			try {
				region.region().setParent(newRegion);
			} catch (CircularInheritanceException e) {
				assert delta != null;
				error("Circular region inheritance detected. '" + delta[0] + "' has '" +
						region + "' as an existing parent.");
			}
		}
	}

	@Override
	public Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

	@Override
	protected String getPropertyName() {
		return "parent";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isAll) {
			return "all the parent regions of " + getExpr().toString(event, debug);
		}
		return super.toString(event, debug);
	}

}
