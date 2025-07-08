package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.Collections;
import java.util.Iterator;

@Name("Blocks of Region")
@Description("An expression that returns all of the blocks in the given regions.")
@Example("set the blocks of {arena} to air")
@Since("1.0")
public class ExprBlocksInRegion extends PropertyExpression<WorldGuardRegion, Block> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprBlocksInRegion.class, Block.class, "blocks", "worldguardregions");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Block[] get(Event event, WorldGuardRegion [] regions) {
		return Iterators.toArray(iterator(event), Block.class);
	}

	@Override
	public @NotNull Iterator<? extends Block> iterator(Event event) {
		//noinspection unchecked
		Iterator<WorldGuardRegion> regionIterator = (Iterator<WorldGuardRegion>) getExpr().iterator(event);
		if (regionIterator == null) {
			return Collections.emptyIterator();
		}
		return RegionUtils.getRegionBlockIterator(regionIterator);
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the blocks of " + getExpr().toString(event, debug);
	}

}
