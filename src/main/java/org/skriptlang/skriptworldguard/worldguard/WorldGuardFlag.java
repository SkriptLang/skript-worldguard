package org.skriptlang.skriptworldguard.worldguard;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.WeatherType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility record for working with {@link Flag}s.
 * @param <F> The Skript-equivalent type of value held by the flag.
 * @param <T> The raw (WorldGuard) type of value held by the flag.
 */
public record WorldGuardFlag<F, T>(Flag<T> flag, FlagValueConverter<F, T> valueConverter) {

	/**
	 * Holds information for converting {@link Flag} values from Skript types to WorldGuard types.
	 * @param fromType Class representing {@code F}.
	 * @param toType Class representing {@code T}.
	 * @param toMapper A function for mapping values of {@code fromType} to {@code toType}.
	 * @param fromMapper A function for mapping values of {@code toType} to {@code fromType}.
	 * @param <F> The Skript-equivalent type of value held by the flag.
	 * @param <T> The raw (WorldGuard) type of value held by the flag.
	 */
	public record FlagValueConverter<F, T>(Class<F> fromType, Class<T> toType,
			Function<F, T> toMapper, Function<T, F> fromMapper) {

		static <T> FlagValueConverter<T, T> simple(Class<T> type) {
			return new FlagValueConverter<>(type, type, from -> from, to -> to);
		}

	}

	// Map Flag Class -> WorldGuard Type
	private static final Map<Class<? extends Flag<?>>, Class<?>> FLAG_CLASS_MAPPINGS;
	// Map WorldGuard type -> Converter
	private static final Map<Class<?>, FlagValueConverter<?, ?>> FLAG_VALUE_CONVERTERS;

	static {
		Builder<Class<? extends Flag<?>>, Class<?>> mappings = ImmutableMap.builder();
		Builder<Class<?>, FlagValueConverter<?, ?>> converters = ImmutableMap.builder();

		// BooleanFlag
		mappings.put(BooleanFlag.class, Boolean.class);
		converters.put(Boolean.class, FlagValueConverter.simple(Boolean.class));
		// CommandStringFlag
		mappings.put(CommandStringFlag.class, String.class);
		converters.put(String.class, FlagValueConverter.simple(String.class));
		// DoubleFlag
		mappings.put(DoubleFlag.class, Double.class);
		converters.put(Double.class, FlagValueConverter.simple(Double.class));
		// skipped: EnumFlag (handled at runtime)
		// IntegerFlag
		mappings.put(IntegerFlag.class, Integer.class);
		converters.put(Integer.class, FlagValueConverter.simple(Integer.class));
		// LocationFlag
		mappings.put(LocationFlag.class, com.sk89q.worldedit.util.Location.class);
		converters.put(com.sk89q.worldedit.util.Location.class,
				new FlagValueConverter<>(Location.class, com.sk89q.worldedit.util.Location.class,
						BukkitAdapter::adapt, BukkitAdapter::adapt));
		// skipped: MapFlag (too complex), NumberFlag (is abstract)
		// RegionGroupFlag
		mappings.put(RegionGroupFlag.class, RegionGroup.class);
		// We use the same parsing for group flags as is used by the command
		// It is consistent across flags, just not static, so we use the BUILD flag instance
		converters.put(RegionGroup.class, new FlagValueConverter<>(String.class, RegionGroup.class,
				from -> Flags.BUILD.getRegionGroupFlag().detectValue(from),
				to -> to.name().replace("_", "").toLowerCase(Locale.ENGLISH)));
		// skipped: RegistryFlag (handled at runtime), SetFlag (handled at runtime)
		// StateFlag
		mappings.put(StateFlag.class, StateFlag.State.class);
		converters.put(StateFlag.State.class, new FlagValueConverter<>(Boolean.class, StateFlag.State.class,
				from -> from ? StateFlag.State.ALLOW : StateFlag.State.DENY,
				to -> to == StateFlag.State.ALLOW));
		// StringFlag
		mappings.put(StringFlag.class, String.class);
		// String converter is registered above under CommandStringFlag
		// TimestampFlag
		mappings.put(TimestampFlag.class, Instant.class);
		converters.put(Instant.class, new FlagValueConverter<>(Date.class, Instant.class,
				Date::toInstant, to -> Date.fromJavaDate(Date.from(to))));
		// UUIDFlag
		mappings.put(UUIDFlag.class, UUID.class);
		converters.put(UUID.class, FlagValueConverter.simple(UUID.class));
		// VectorFlag
		mappings.put(VectorFlag.class, Vector3.class);
		converters.put(Vector3.class, new FlagValueConverter<>(Vector.class, Vector3.class,
				from -> Vector3.at(from.getX(), from.getY(), from.getZ()),
				to -> new Vector(to.getX(), to.getY(), to.getZ())));

		// Additional mappings
		// no WorldGuard GameMode -> Bukkit GameMode adapter...
		converters.put(com.sk89q.worldedit.world.gamemode.GameMode.class,
				new FlagValueConverter<>(GameMode.class, com.sk89q.worldedit.world.gamemode.GameMode.class,
						BukkitAdapter::adapt, to -> GameMode.valueOf(to.getName().toUpperCase(Locale.ENGLISH))));
		converters.put(com.sk89q.worldedit.world.weather.WeatherType.class,
				new FlagValueConverter<>(WeatherType.class, com.sk89q.worldedit.world.weather.WeatherType.class,
						from -> switch (from) {
							case CLEAR -> WeatherTypes.CLEAR;
							case RAIN -> WeatherTypes.RAIN;
							case THUNDER -> WeatherTypes.THUNDER_STORM;
						}, to -> {
							if (to == WeatherTypes.CLEAR) {
								return WeatherType.CLEAR;
							} else if (to == WeatherTypes.RAIN) {
								return WeatherType.RAIN;
							} else if (to == WeatherTypes.THUNDER_STORM) {
								return WeatherType.THUNDER;
							} else {
								throw new IllegalArgumentException("Unknown WorldGuard weather type: " + to);
							}
						}));
		converters.put(com.sk89q.worldedit.world.entity.EntityType.class,
				new FlagValueConverter<>(EntityData.class, com.sk89q.worldedit.world.entity.EntityType.class,
						from -> BukkitAdapter.adapt(EntityUtils.toBukkitEntityType(from)),
						to -> EntityUtils.toSkriptEntityData(BukkitAdapter.adapt(to))));

		FLAG_CLASS_MAPPINGS = mappings.build();
		FLAG_VALUE_CONVERTERS = converters.build();
	}

	/**
	 * Attempts to return a Converter for mapping a Skript type with the type of {@code flag}.
	 * @param flag The flag to obtain a value converter for.
	 * @return A converter if successful, otherwise null.
	 */
	private static @Nullable FlagValueConverter<?, ?> getFlagValueConverter(Flag<?> flag) {
		FlagValueConverter<?, ?> converter;
		if (flag instanceof EnumFlag<?> enumFlag) {
			converter = FLAG_VALUE_CONVERTERS.get(enumFlag.getEnumClass());
		} else if (flag instanceof RegistryFlag<?> registryFlag) {
			converter = FLAG_VALUE_CONVERTERS.get(registryFlag.getRegistry().iterator().next().getClass());
		} else if (flag instanceof SetFlag<?> setFlag) {
			// convert from F[] to Set<T> using Function<F, T>
			//noinspection rawtypes
			FlagValueConverter subTypeConverter = getFlagValueConverter(setFlag.getType());
			if (subTypeConverter != null) {
				//noinspection unchecked, rawtypes
				converter = new FlagValueConverter(subTypeConverter.fromType().arrayType(), Set.class,
						from -> Arrays.stream((Object[]) from)
								.map(subTypeConverter.toMapper())
								.collect(Collectors.toSet()),
						to -> ((Set<?>) to).stream()
								.map(subTypeConverter.fromMapper())
								.toArray(length -> Array.newInstance(subTypeConverter.fromType(), length)));
			} else {
				converter = null;
			}
		} else {
			Class<?> flagClass = flag.getClass();
			Class<?> flagValueClass = FLAG_CLASS_MAPPINGS.get(flagClass);
			if (flagValueClass == null) { // try broader lookup
				for (var entry : FLAG_CLASS_MAPPINGS.entrySet()) {
					if (entry.getKey().isAssignableFrom(flagClass)) {
						flagValueClass = entry.getValue();
						break;
					}
				}
			}
			converter = FLAG_VALUE_CONVERTERS.get(flagValueClass);
		}

		return converter;
	}

	/**
	 * The result of a flag lookup.
	 * @param flag The found flag. Null if unsuccessful.
	 * @param error An error message describing why the lookup failed. Null if successful.
	 * @see #fromName(String)
	 */
	public record LookupResult(@Nullable WorldGuardFlag<?, ?> flag, @Nullable String error) { }

	/**
	 * Searches for a flag by name and returns a lookup result.
	 * @param name The name of the flag to lookup.
	 * @return A result with {@link LookupResult#flag} set if the search was successful,
	 *  otherwise a message set in {@link LookupResult#error}.
	 */
	public static LookupResult fromName(String name) {
		// identify flag
		Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), name);
		if (flag == null) {
			return new LookupResult(null, "The flag '" + name + "' does not exist");
		}

		// identify converter
		FlagValueConverter<?, ?> converter = getFlagValueConverter(flag);
		if (converter == null) {
			return new LookupResult(null, "The flag '" + name + "' exists, but Skript is unable to handle its value.");
		}

		//noinspection unchecked, rawtypes
		return new LookupResult(new WorldGuardFlag(flag, converter), null);
	}

	/**
	 * Obtains the group flag of this flag, which controls who the flag is applied to.
	 * @return The group flag of this flag. Null if this flag is already a group flag.
	 * @see RegionGroupFlag
	 */
	public @Nullable WorldGuardFlag<String, RegionGroup> groupFlag() {
		RegionGroupFlag groupFlag = flag().getRegionGroupFlag();
		if (groupFlag == null) { // This must be a group flag
			return null;
		}
		//noinspection unchecked
		return new WorldGuardFlag<>(flag().getRegionGroupFlag(),
				(FlagValueConverter<String, RegionGroup>) getFlagValueConverter(groupFlag));
	}

}
