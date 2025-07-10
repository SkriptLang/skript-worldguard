package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Is Parent Region")
@Description("A condition to test whether a region is a parent region of another.")
@Example("""
	command /printchildren <text>:
		trigger:
			set {_parent} to the region text-argument
			message "Children of %{_parent}%:"
			loop all regions:
				if {_parent} is a parent region of loop-region:
					message "- %loop-region%"
	""")
@Since("1.0")
public class CondIsParentRegion extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, PropertyCondition.infoBuilder(CondIsParentRegion.class, PropertyType.BE,
						"a parent region[s] of %worldguardregions%", "worldguardregions")
				.supplier(CondIsParentRegion::new)
				.build()
		);
	}

	private Expression<WorldGuardRegion> parents;
	private Expression<WorldGuardRegion> children;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		parents = (Expression<WorldGuardRegion>) expressions[0];
		//noinspection unchecked
		children = (Expression<WorldGuardRegion>) expressions[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		WorldGuardRegion[] children = this.children.getArray(event);
		return parents.check(event, parent -> SimpleExpression.check(children,
				child -> isParent(parent, child), false, this.children.getAnd()), isNegated());
	}

	private boolean isParent(WorldGuardRegion parent, WorldGuardRegion child) {
		ProtectedRegion childRegion = child.region();
		while (childRegion != null) {
			childRegion = childRegion.getParent();
			if (childRegion == parent.region()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String property = "a parent region" + (parents.isSingle() ? "" : "s") + " of ";
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, parents,
				property + children.toString(event, debug));
	}

}
