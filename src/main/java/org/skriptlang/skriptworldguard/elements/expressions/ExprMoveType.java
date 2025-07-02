package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("WorldGuard Move Type")
@Description("The WorldGuard move type in a WorldGuard region enter/leave event.")
@Examples({
	"on region enter:",
		"\tsend \"The move type is '%the move type%'\""
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprMoveType extends EventValueExpression<MoveType> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprMoveType.class, MoveType.class, "[worldguard] move[ ]type");
	}

	public ExprMoveType() {
		super(MoveType.class);
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return "the worldguard move type";
	}

}
