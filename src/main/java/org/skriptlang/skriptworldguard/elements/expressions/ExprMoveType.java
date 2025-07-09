package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import com.sk89q.worldguard.session.MoveType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("WorldGuard Move Type")
@Description("The movement type in a 'region enter/leave' event. This represents how the player ended up in/out of a region.")
@Example("""
	on region enter:
		if the movement type is swimming:
			message "You have swum into %region%!"
	""")
@Events("Region Enter/Leave")
@Since("1.0")
public class ExprMoveType extends EventValueExpression<MoveType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprMoveType.class, MoveType.class,
				"[worldguard] move[ment][ ]type")
						.supplier(ExprMoveType::new)
						.build());
	}

	public ExprMoveType() {
		super(MoveType.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the worldguard move type";
	}

}
