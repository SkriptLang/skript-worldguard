package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import com.google.common.collect.Sets;
import com.sk89q.worldguard.protection.flags.Flag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardFlag;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Name("Region Flag")
@Description({
	"An expression for working with the flag of a region.",
	"The region group flag of a flag supports controlling which groups of a region" +
		" (i.e., members, owners, etc.) a flag applies to.",
	"NOTE: Skript may not support all flags, specifically those representing values Skript cannot understand."
})
@Example("message flag \"greeting\" of player's region")
@Since("1.0")
public class ExprRegionFlag extends SimpleExpression<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, PropertyExpression.infoBuilder(ExprRegionFlag.class, Object.class,
				"[region] [group:group flag of [the]] flag %string%", "worldguardregions", false)
						.supplier(ExprRegionFlag::new)
						.build());
	}

	private Expression<String> flagName;
	private Expression<WorldGuardRegion> regions;
	private boolean isGroupFlag;

	private WorldGuardFlag<?, ?> flag;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isFlagFirst = matchedPattern % 2 == 0;
		//noinspection unchecked
		flagName = (Expression<String>) expressions[isFlagFirst ? 0 : 1];
		//noinspection unchecked
		regions = (Expression<WorldGuardRegion>) expressions[isFlagFirst ? 1 : 0];
		isGroupFlag = parseResult.hasTag("group");

		if (flagName instanceof Literal<String> literal) {
			WorldGuardFlag.LookupResult result = WorldGuardFlag.fromName(literal.getSingle());
			flag = result.flag();
			if (flag == null) {
				Skript.error(result.error());
				return false;
			}
			if (isGroupFlag) {
				flag = flag.groupFlag();
				if (flag == null) {
					Skript.error("The flag '" + literal.getSingle() + "' does not have a group flag");
					return false;
				}
			}
		}

		return true;
	}

	private @Nullable WorldGuardFlag<?, ?> getRuntimeFlag(Event event) {
		if (flag != null) {
			return flag;
		}
		String flagName = this.flagName.getSingle(event);
		if (flagName == null) {
			return null;
		}
		WorldGuardFlag.LookupResult result = WorldGuardFlag.fromName(flagName);
		WorldGuardFlag<?, ?> flag = result.flag();
		if (flag == null) {
			error(result.error());
			return null;
		}
		if (isGroupFlag) {
			flag = flag.groupFlag();
			if (flag == null) {
				error("The flag '" + flagName + "' does not have a group flag");
				return null;
			}
		}
		return flag;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		WorldGuardFlag<?, ?> flag = getRuntimeFlag(event);
		if (flag == null) {
			return new Object[0];
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
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET ->
					new Class[]{flag == null ? Object[].class : flag.valueConverter().fromType()};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		WorldGuardFlag<?, ?> flag = getRuntimeFlag(event);
		if (flag == null) {
			return;
		}
		WorldGuardRegion[] regions = this.regions.getArray(event);

		// verify types
		// this will have been done by EffChange if flag is set (due to accurate types)
		if (delta != null && this.flag == null) {
			Class<?> expectedType = flag.valueConverter().fromType();
			if (expectedType.isArray()) {
				expectedType = expectedType.getComponentType();
			}

			// special runtime type handling
			// need to manually convert to correct number type
			if (Number.class.isAssignableFrom(expectedType)) {
				Object[] delta2 = new Object[delta.length];
				Converter<Number, ?> converter = Converters.getConverter(Number.class, expectedType);
				assert converter != null;
				for (int i = 0; i < delta.length; i++) {
					if (delta[i] instanceof Number number) {
						delta2[i] = converter.convert(number);
					}
				}
				delta = delta2;
			}

			Class<?> foundType = Utils.getSuperType(Arrays.stream(delta)
					.map(Object::getClass)
					.toArray(Class[]::new));

			if (!expectedType.isAssignableFrom(foundType)) {
				String expr = toString(event, Skript.debug());
				String add = switch (mode) {
					case ADD -> "to add to '" + expr + "'";
					case SET -> "to set '" + expr + "' to";
					case REMOVE -> "to remove from '" + expr + "'";
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				};
				error("Expected the value " + add + " to be " +
						Utils.a(Classes.toString(Classes.getSuperClassInfo(expectedType))) +
						" but it was " +
						Utils.a(Classes.toString(Classes.getSuperClassInfo(foundType))));
				return;
			}
			if (delta.length > 1 && !flag.valueConverter().fromType().isArray()) {
				error(toString(event, Skript.debug()) + " can only have one value " +
						(mode == ChangeMode.ADD ? "added to" : "removed from") + " it, not more");
				return;
			}
		}

		if (delta != null) { // handle special cases
			for (int i = 0; i < delta.length; i++) {
				if (delta[i] instanceof EntityData<?> entityData) { // fix comparison issues, e.g. isPlural
					delta[i] = EntityData.fromClass(entityData.getType());
				}
			}
		}

		switch (mode) {
			case SET -> {
				assert delta != null;
				Object value;
				if (flag.valueConverter().fromType().isArray()) {
					value = delta;
				} else {
					value = delta[0];
				}
				//noinspection unchecked, rawtypes
				Object mappedValue = ((Function) flag.valueConverter().toMapper()).apply(value);
				if (mappedValue != null) {
					for (WorldGuardRegion region : regions) {
						//noinspection unchecked, rawtypes
						region.region().setFlag((Flag) flag.flag(), mappedValue);
					}
				}
			}
			case ADD, REMOVE -> {
				assert delta != null;
				boolean isRemove = mode == ChangeMode.REMOVE;
				if (Number.class.isAssignableFrom(flag.valueConverter().fromType())) {
					for (WorldGuardRegion region : regions) {
						Object current = region.region().getFlag(flag.flag());
						if (current == null) {
							current = flag.flag().getDefault();
						}
						if (current == null) {
							continue;
						}
						//noinspection unchecked, rawtypes
						Number mappedCurrent = (Number) ((Function) flag.valueConverter().fromMapper()).apply(current);

						// calculate result
						Number result = Arithmetics.calculate(isRemove ? Operator.SUBTRACTION : Operator.ADDITION,
								mappedCurrent, delta[0], Number.class);
						if (result instanceof Long longResult) { // convert to Integer
							result = (int) Math2.fit(Integer.MIN_VALUE, longResult, Integer.MAX_VALUE);
						}

						//noinspection unchecked, rawtypes
						Object mappedResult = ((Function) flag.valueConverter().toMapper()).apply(result);
						if (mappedResult != null) {
							//noinspection unchecked, rawtypes
							region.region().setFlag((Flag) flag.flag(), mappedResult);
						}
					}
				} else if (flag.valueConverter().fromType().isArray()) {
					Set<?> deltaSet = Set.of(delta);
					for (WorldGuardRegion region : regions) {
						Object current = region.region().getFlag(flag.flag());
						if (current == null) {
							current = flag.flag().getDefault();
						}
						Set<?> mappedSet;
						if (current == null) {
							if (isRemove) { // nothing to remove
								continue;
							}
							mappedSet = Set.of();
						} else {
							//noinspection unchecked, rawtypes
							Object[] mappedCurrent = (Object[]) ((Function) flag.valueConverter().fromMapper()).apply(current);
							mappedSet = Set.of(mappedCurrent);
						}

						// merge sets
						Set<?> valueSet;
						if (isRemove) {
							valueSet = Sets.difference(mappedSet, deltaSet);
						} else {
							valueSet = Sets.union(mappedSet, deltaSet);
						}

						//noinspection unchecked, rawtypes
						Object mappedValue = ((Function) flag.valueConverter().toMapper()).apply(valueSet.toArray());
						if (mappedValue != null) {
							//noinspection unchecked, rawtypes
							region.region().setFlag((Flag) flag.flag(), mappedValue);
						}
					}
				} else {
					error(toString(event, Skript.debug()) + " can't have values " +
							(mode == ChangeMode.ADD ? "added to" : "removed from") + " it");
				}

			}
			case DELETE, RESET -> {
				for (WorldGuardRegion region : regions) {
					// first, clear the flag's group flag if possible, then clear the flag itself
					WorldGuardFlag<?, ?> groupFlag = flag.groupFlag();
					if (groupFlag != null) {
						region.region().setFlag(groupFlag.flag(), null);
					}
					region.region().setFlag(flag.flag(), null);
				}
			}
			default -> {
				assert false;
			}
		}
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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (isGroupFlag) {
			builder.append("group flag of the");
		}
		builder.append("flag", flagName, "of", regions);
		return builder.toString();
	}

}
