package io.github.apickledwalrus.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptworldguard.SkriptRegion;
import io.github.apickledwalrus.skriptworldguard.SkriptWorldGuard;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Is In Region")
@Description("A condition that tests whether the given entities are in the given regions.")
@Examples("send \"True\" if the player is in the region \"region\" in player's world")
@Since("1.0")
public class CondIsInRegion extends Condition {

	static {
		PropertyCondition.register(CondIsInRegion.class,
				"in [[the] [worldguard] region[s]] %regions%", "entities"
		);
	}

	private Expression<Entity> entities;
	private Expression<SkriptRegion> regions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		regions = (Expression<SkriptRegion>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return entities.check(e,
				entity -> regions.check(e,
						region -> {
							Location loc = entity.getLocation();
							SkriptWorldGuard.getInstance().getLogger().warning(entity.getName());
							return region.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
						}
				), isNegated());
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, entities,
				"in the " + (regions.isSingle() ? "region " : "regions ") + regions.toString(e, debug));
	}

}
