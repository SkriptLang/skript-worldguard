package io.github.apickledwalrus.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("WorldGuard Move Type")
@Description("The WorldGuard move type in a WorldGuard region enter/leave event.")
@Examples({
		"on region enter:",
		"\tsend \"The move type is '%the move type%'\""
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprMoveType extends EventValueExpression<MoveType> {

	static {
		Skript.registerExpression(ExprMoveType.class, MoveType.class, ExpressionType.SIMPLE, "[the] [worldguard] move[ ]type");
	}

	public ExprMoveType() {
		super(MoveType.class);
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		return "the worldguard move type";
	}

}
