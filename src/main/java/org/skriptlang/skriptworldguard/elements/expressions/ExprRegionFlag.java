package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardFlag;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Name("Region Flag")
@Description({
		"An expression for working with the flag of a region.",
		"NOTE: Skript may not support all flags, specifically those representing values Skript cannot understand."
})
@Example("message flag \"greeting\" of player's region")
@Since("1.0")
public class ExprRegionFlag extends SimpleExpression<Object> {

	public static void register(SyntaxRegistry registry) {
		PropertyExpression.register(registry, ExprRegionFlag.class, Object.class,
				"[region] flag %string%", "worldguardregions");
	}

	private Expression<String> flagName;
	private Expression<WorldGuardRegion> regions;

	private WorldGuardFlag<?, ?> flag;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isFlagFirst = matchedPattern % 2 == 0;
		//noinspection unchecked
		flagName = (Expression<String>) expressions[isFlagFirst ? 0 : 1];
		//noinspection unchecked
		regions = (Expression<WorldGuardRegion>) expressions[isFlagFirst ? 1 : 0];

		if (flagName instanceof Literal<String> literal) {
			WorldGuardFlag.LookupResult result = WorldGuardFlag.fromName(literal.getSingle());
			flag = result.flag();
			if (flag == null) {
				Skript.error(result.error());
				return false;
			}
		}

		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		WorldGuardFlag<?, ?> flag = this.flag;
		if (flag == null) {
			String flagName = this.flagName.getSingle(event);
			if (flagName == null) {
				return new Object[0];
			}
			WorldGuardFlag.LookupResult result = WorldGuardFlag.fromName(flagName);
			flag = result.flag();
			if (flag == null) {
				error(result.error());
				return new Object[0];
			}
		}

		WorldGuardRegion[] regions = this.regions.getArray(event);

		List<Object> values = new ArrayList<>();
		for (WorldGuardRegion region : regions) {
			Object value = region.region().getFlag(flag.flag());
			if (value == null) {
				value = flag.flag().getDefault();
			}
			if (value == null) {
				continue;
			}
			//noinspection unchecked, rawtypes
			value = ((Function) flag.valueConverter().fromMapper()).apply(value);
			if (value instanceof Object[] valueArray) {
				if (regions.length == 1) { // early exit to avoid unnecessary array creation
					return valueArray;
				}
				values.addAll(Arrays.asList(valueArray));
			} else {
				values.add(value);
			}
		}

		return values.toArray((Object[]) Array.newInstance(getReturnType(), values.size()));
	}

	@Override
	public boolean isSingle() {
		if (flag == null) {
			return false;
		}
		return regions.isSingle() && !flag.valueConverter().fromType().isArray();
	}

	@Override
	public Class<?> getReturnType() {
		if (flag == null) {
			return Object.class;
		}
		Class<?> type = flag.valueConverter().fromType();
		if (type.isArray()) {
			type = type.getComponentType();
		}
		return type;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
				.append("the flag", flagName, "of", regions)
				.toString();
	}

}
