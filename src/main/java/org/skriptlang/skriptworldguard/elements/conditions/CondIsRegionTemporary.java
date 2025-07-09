package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

@Name("Is Region Temporary")
@Description({
		"A condition to test whether a region is temporary.",
		"Temporary regions are those that are removed when the server restarts."
})
@Example("""
	on region enter:
		if the region is temporary:
			message "Be ready! This protected region will expire when the server restarts."
	""")
@Since("1.0")
public class CondIsRegionTemporary extends PropertyCondition<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsRegionTemporary.class, PropertyType.BE,
				"(temporary|transient)", "worldguardregions")
						.supplier(CondIsRegionTemporary::new)
						.build());
	}

	@Override
	public boolean check(WorldGuardRegion region) {
		return region.region().isTransient();
	}

	@Override
	protected String getPropertyName() {
		return "temporary";
	}

}
