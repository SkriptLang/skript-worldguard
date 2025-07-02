package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Name("Blocks of Region")
@Description("An expression that returns all of the blocks in the given regions.")
@Examples("set the blocks of {arena} to air")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprBlocksInRegion extends PropertyExpression<WorldGuardRegion, Block> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprBlocksInRegion.class, Block.class, "blocks", "worldguardregions");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		setExpr((Expression<WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Block @NotNull [] get(@NotNull Event event, WorldGuardRegion @NotNull [] regions) {
		return getBlocks(regions).toArray(new Block[0]);
	}

	@Override
	public Iterator<? extends Block> iterator(@NotNull Event event) {
		return getBlocks(getExpr().getArray(event)).iterator();
	}

	private List<Block> getBlocks(WorldGuardRegion[] regions) {
		List<Block> blocks = new ArrayList<>();
		for (WorldGuardRegion region : regions) {
			blocks.addAll(RegionUtils.getBlocksInRegion(region));
		}
		return blocks;
	}

	@Override
	public @NotNull Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return "the blocks of " + getExpr().toString(event, debug);
	}

}
