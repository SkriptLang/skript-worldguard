package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Region Parent")
@Description({
	"An expression to obtain and change the parent of a region.",
	"When a region has a parent, it inherits the parents members, owners, and flags (that aren't defined on the child)."
})
@Example("""
	command /setparent <text>:
		trigger:
			set the parents of the regions at the player to the region text-argument
	""")
@Since("1.0")
public class ExprRegionParent extends SimplePropertyExpression<WorldGuardRegion, WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprRegionParent.class, WorldGuardRegion.class,
				"[region] parent[s]", "worldguardregions", false)
						.supplier(ExprRegionParent::new)
						.build());
	}

	@Override
	public WorldGuardRegion convert(WorldGuardRegion region) {
		return new WorldGuardRegion(region.world(), region.region().getParent());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
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
	protected String getPropertyName() {
		return "parent";
	}

	@Override
	public Class<? extends WorldGuardRegion> getReturnType() {
		return WorldGuardRegion.class;
	}

}
