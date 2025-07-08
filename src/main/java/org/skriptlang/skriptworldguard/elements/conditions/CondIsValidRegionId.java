package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Valid Region ID")
@Description({
	"A condition to test whether a string is a valid region ID.",
	"Valid region IDs only contain letters, numbers, underscores, commas, single quotation marks, dashes, pluses, or forward slashes."
})
@Example("""
	command /createregion <text>:
		if the text-argument is not a valid region id:
			message "<red>'%text-argument%' is not a valid region ID")
		# here is where the rest of the command would go :)
	""")
@Since("1.0")
public class CondIsValidRegionId extends PropertyCondition<String> {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsValidRegionId.class, "[a] valid [worldguard] region id", "strings");
	}

	@Override
	public boolean check(String id) {
		return ProtectedRegion.isValidId(id);
	}

	@Override
	protected String getPropertyName() {
		return "valid region id";
	}

}
