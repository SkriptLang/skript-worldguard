package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.jetbrains.annotations.NotNull;

@Name("Is Valid Region ID")
@Description({
	"A condition that tests whether the given string(s) is/are a valid region id.",
	"Region IDs are only valid if they contain letters, numbers, underscores, commas, single quotation marks, dashes, pluses, or forward slashes."
})
@Examples("send \"I am a valid region ID!\" if \"global_region\" is a valid region id")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondIsValidId extends PropertyCondition<String> {

	static {
		register(CondIsValidId.class, "[a] valid [worldguard] region id", "strings");
	}

	@Override
	public boolean check(String id) {
		return ProtectedRegion.isValidId(id);
	}

	@Override
	protected @NotNull String getPropertyName() {
		return "valid region id";
	}

}
