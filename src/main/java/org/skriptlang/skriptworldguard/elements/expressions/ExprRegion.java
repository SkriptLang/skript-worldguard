package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.event.Event;

@Name("Region")
@Description("The region in any region related event.")
@Example("""
	on region enter:
		send "You have entered %region%"
	""")
@Events("Region Enter/Leave")
@Since("1.0")
public class ExprRegion extends EventValueExpression<WorldGuardRegion> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprRegion.class, WorldGuardRegion.class, "[worldguard] region");
	}

	public ExprRegion() {
		super(WorldGuardRegion.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the worldguard region";
	}

}
