package org.skriptlang.skriptworldguard.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.RegionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Name("Create Region")
@Description({
	"An effect to create a WorldGuard region.",
	"A temporary region is a region that will not be saved, meaning it is lost when the server restarts.",
	"A global region is a region that has no boundaries, meaning it covers the entire world.",
	"A cuboid region is the traditional WorldGuard region. It has two points to determine the boundaries.",
	"A polygonal region comprises many points. These points are used to draw a two-dimensional shape." +
		" Then, with the provided heights, the shape is extended vertically to form the region." +
		" At least three points must be provided to create a polygonal region.",
	"Note that if you do not specify the world for a region, you must be sure that the locations provided all have the same world.",
	"Note that Region IDs are only valid if they contain letters, numbers, underscores, commas, single quotation marks, dashes, pluses, or forward slashes.",
	"Note that if you attempt to create a region in a world where a region with the same ID already exists, that region will be replaced."
})
@Example("create a temporary global region named \"temporary_global_region\" in the player's world")
@Example("create region \"cuboid_region\" in player's world between the location (0, 60, 0) and the location (10, 70, 10)")
@Example("create a polygonal region named \"polygonal_region\" with a minimum height of 10 and a maximum height of 20 with points {points::*}")
@Since("1.0")
public class EffCreateRegion extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffCreateRegion.class)
				.supplier(EffCreateRegion::new)
				.addPatterns("create [a] [:temporary] global [worldguard] region [named] %string% [in %world%]",
						"create [a] [:temporary] [cuboid|rectangular] [worldguard] region [named] %string% [in %-world%] (between|from) %location% (to|and) %location%",
						"create [a] [:temporary] polygonal [worldguard] region [named] %string% [in %-world%] with [a] min[imum] height of %number% and [a] max[imum] height of %number% with [the] points %locations%")
				.build());
	}

	// Shared Values
	private boolean temporary;
	private Expression<String> id;
	private @Nullable Expression<World> world;
	// Cuboid Region Values
	private @Nullable Expression<Location> firstCorner;
	private @Nullable Expression<Location> secondCorner;
	// Polygonal Region Values
	private @Nullable Expression<Number> minY;
	private @Nullable Expression<Number> maxY;
	private @Nullable Expression<Location> points;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		temporary = parseResult.hasTag("temporary");
		id = (Expression<String>) exprs[0];
		world = (Expression<World>) exprs[1];
		if (matchedPattern == 1) { // Cuboid Region
			firstCorner = (Expression<Location>) exprs[2];
			secondCorner = (Expression<Location>) exprs[3];
		} else if (matchedPattern == 2) { // Polygonal Region
			minY = (Expression<Number>) exprs[2];
			maxY = (Expression<Number>) exprs[3];
			points = (Expression<Location>) exprs[4];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		String id = this.id.getSingle(event);
		if (id == null || !ProtectedRegion.isValidId(id)) {
			return;
		}

		World world = this.world != null ? this.world.getSingle(event) : null; // May be null, but that is not necessarily a bad thing

		ProtectedRegion region; // The new region we are going to create

		if (firstCorner != null) { // Cuboid Region
			Location firstCorner = this.firstCorner.getSingle(event);
			assert secondCorner != null;
			Location secondCorner = this.secondCorner.getSingle(event);
			if (firstCorner == null || secondCorner == null) {
				return;
			}

			if (world == null) { // Okay... we can try to get one from the locations
				World firstCornerWorld = firstCorner.getWorld();
				World secondCornerWorld = secondCorner.getWorld();
				// We want the locations to have matching worlds
				if (firstCornerWorld == null || firstCornerWorld != secondCornerWorld) {
					return;
				}
				world = firstCornerWorld;
			}

			region = new ProtectedCuboidRegion(id, temporary,
					BukkitAdapter.asBlockVector(firstCorner),BukkitAdapter.asBlockVector(secondCorner));
		} else if (minY != null) { // Polygonal Region
			assert this.maxY != null && this.points != null;
			Number minY = this.minY.getSingle(event);
			Number maxY = this.maxY.getSingle(event);
			Location[] points = this.points.getArray(event);
			if (minY == null || maxY == null || points.length < 3) {
				return;
			}

			if (world == null) { // Okay... we can try to get one from the locations
				world = points[0].getWorld();
				for (Location point : points) { // We want the locations to have matching worlds
					World pointWorld = point.getWorld();
					if (pointWorld == null || pointWorld != world) {
						return;
					}
				}
			}

			List<BlockVector2> pointVectors = new ArrayList<>();
			for (Location point : points) {
				pointVectors.add(BlockVector2.at(point.getBlockX(), point.getBlockZ()));
			}

			region = new ProtectedPolygonalRegion(id, temporary, pointVectors, minY.intValue(), maxY.intValue());
		} else { // Global Region
			if (world == null) { // This is a global region, so there are no locations to use as a backup
				return;
			}
			region = new GlobalProtectedRegion(id, temporary);
		}

		RegionManager regionManager = RegionUtils.getRegionManager(world);
		if (regionManager != null) {
			regionManager.addRegion(region);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("create a new");
		if (temporary) {
			builder.append("temporary");
		}
		if (firstCorner != null) { // Cuboid region
			assert secondCorner != null;
			builder.append("cuboid region named", id);
			if (world != null) {
				builder.append("in", world);
			}
			builder.append("between", firstCorner, "and", secondCorner);
		} else if (minY != null) { // Polygonal region
			assert maxY != null && points != null;
			builder.append("polygonal region named", id);
			if (world != null) {
				builder.append("in", world);
			}
			builder.append("with a minimum height of", minY, "and a maximum height of", maxY, "with the points", points);
		} else { // Global region
			assert world != null;
			builder.append("global region named", id, "in", world);
		}
		return builder.toString();
	}

}
