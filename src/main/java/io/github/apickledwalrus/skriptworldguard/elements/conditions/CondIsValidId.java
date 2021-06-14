package io.github.apickledwalrus.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

@Name("Is Valid ID")
@Description("A condition that tests whether the given string(s) is/are a valid region id.")
@Examples("send \"True\" if \"global_region\" is a valid region id")
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
	protected String getPropertyName() {
		return "valid region id";
	}

}
