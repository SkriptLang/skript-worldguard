package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.protection.regions.RegionType;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

import java.util.Locale;

@Name("Is Region Type")
@Description("A condition to test what type/shape a region is.")
@Example("""
	on region enter:
		if the region is polygonal:
			message "Welcome to my wacky region!"
	""")
@Since("1.0")
public class CondIsRegionType extends PropertyCondition<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsRegionType.class, PropertyType.BE,
				"[a] (:global|:cuboid[s]|:polygon[s|al]) [region[s]]", "worldguardregions")
						.supplier(CondIsRegionType::new)
						.build());
	}

	private RegionType regionType;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		regionType = RegionType.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(WorldGuardRegion region) {
		return region.region().getType() == regionType;
	}

	@Override
	protected String getPropertyName() {
		return switch (regionType) {
			case CUBOID -> "cuboid";
			case POLYGON -> "polygonal";
			case GLOBAL -> "global";
		};
	}

}
