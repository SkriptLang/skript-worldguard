package io.github.apickledwalrus.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import io.github.apickledwalrus.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("WorldGuard Region")
@Description("The WorldGuard region in any WorldGuard region related event.")
@Examples({
	"on region enter:",
	"\tsend \"You entered %region%\""
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprRegion extends EventValueExpression<WorldGuardRegion> {

	static {
		Skript.registerExpression(ExprRegion.class, WorldGuardRegion.class, ExpressionType.SIMPLE, "[the] [worldguard] region");
	}

	public ExprRegion() {
		super(WorldGuardRegion.class);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the worldguard region";
	}

}
