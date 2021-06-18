package io.github.apickledwalrus.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptworldguard.worldguard.RegionUtils;
import io.github.apickledwalrus.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Name("Blocks of Region")
@Description("An expression that returns all of the blocks in the given regions.")
@Examples("set the blocks of {arenas::ffa} to air")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprBlocksInRegion extends PropertyExpression<WorldGuardRegion, Block> {

	static {
		register(ExprBlocksInRegion.class, Block.class, "blocks", "worldguardregions");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Block @NotNull [] get(Event event, WorldGuardRegion[] regions) {
		return getBlocks(regions).toArray(new Block[0]);
	}

	@Override
	public Iterator<? extends Block> iterator(Event e) {
		return getBlocks(getExpr().getArray(e)).iterator();
	}

	private List<Block> getBlocks(WorldGuardRegion[] regions) {
		List<Block> blocks = new ArrayList<>();
		if (regions.length == 0) {
			return blocks;
		}
		for (WorldGuardRegion region : regions) {
			blocks.addAll(RegionUtils.getBlocksInRegion(region));
		}
		return blocks;
	}

	@Override
	@NotNull
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	@NotNull
	public String toString(@Nullable Event e, boolean debug) {
		return "the blocks of " + getExpr().toString(e, debug);
	}

}
