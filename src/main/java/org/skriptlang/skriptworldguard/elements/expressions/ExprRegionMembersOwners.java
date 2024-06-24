package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Members/Owners of Region")
@Description({
	"An expression that returns the members of owners of the given regions.",
	"The members or owners of a region are not limited to players, so a keyword to get the group members or owners exists.",
	"By default though, the player members or owners of a group will be returned."
})
@Examples({
	"on region enter:",
		"\tsend \"You're entering %region% whose owners are %owners of region%\"."
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprRegionMembersOwners extends PropertyExpression<WorldGuardRegion, Object> {

	static {
		register(ExprRegionMembersOwners.class, Object.class, "[player|:group] (members|:owners)", "worldguardregions");
	}

	private boolean groups;
	private boolean owners;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		groups = parseResult.hasTag("group");
		owners = parseResult.hasTag("owners");
		setExpr((Expression<? extends WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Object @NotNull [] get(@NotNull Event event, WorldGuardRegion @NotNull [] regions) {
		if (groups) {
			List<String> groups = new ArrayList<>();
			if (owners) { // Group Owners
				for (WorldGuardRegion region : regions) {
					groups.addAll(region.getRegion().getOwners().getGroups());
				}
			} else { // Group Members
				for (WorldGuardRegion region : regions) {
					groups.addAll(region.getRegion().getMembers().getGroups());
				}
			}
			return groups.toArray(new String[0]);
		} else {
			List<OfflinePlayer> players = new ArrayList<>();
			if (owners) { // Player Owners
				for (WorldGuardRegion region : regions) {
					for (UUID uuid : region.getRegion().getOwners().getUniqueIds()) {
						players.add(Bukkit.getOfflinePlayer(uuid));
					}
				}
			} else { // Player Members
				for (WorldGuardRegion region : regions) {
					for (UUID uuid : region.getRegion().getMembers().getUniqueIds()) {
						players.add(Bukkit.getOfflinePlayer(uuid));
					}
				}
			}
			return players.toArray(new OfflinePlayer[0]);
		}
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case REMOVE:
			case DELETE:
			case RESET:
				return groups ? CollectionUtils.array(String[].class) : CollectionUtils.array(OfflinePlayer[].class);
			case REMOVE_ALL:
			default:
				return null;
		}
	}

	@Override
	public void change(@NotNull Event event, Object @Nullable [] delta, @NotNull ChangeMode mode) {
		if ((delta == null && mode != ChangeMode.DELETE && mode != ChangeMode.RESET)) {
			return;
		}

		WorldGuardRegion[] regions = getExpr().getArray(event);
		if (regions.length == 0) {
			return;
		}

		if (groups) {
			String[] groups = (String[]) delta;
			switch (mode) {
				case SET:
				case DELETE:
				case RESET:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							region.getRegion().getOwners().getGroupDomain().clear();
						} else { // Members
							region.getRegion().getMembers().getGroupDomain().clear();
						}
					}
					if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) { // Don't fall through
						break;
					}
				case ADD:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							for (String group : groups) {
								region.getRegion().getOwners().addGroup(group);
							}
						} else { // Members
							for (String group : groups) {
								region.getRegion().getMembers().addGroup(group);
							}
						}
					}
					break;
				case REMOVE:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							for (String group : groups) {
								region.getRegion().getOwners().removeGroup(group);
							}
						} else { // Members
							for (String group : groups) {
								region.getRegion().getMembers().removeGroup(group);
							}
						}
					}
					break;
				default:
					assert false;
			}
		} else { // Players
			OfflinePlayer[] players = (OfflinePlayer[]) delta;
			switch (mode) {
				case SET:
				case DELETE:
				case RESET:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							region.getRegion().getOwners().getPlayerDomain().clear();
						} else { // Members
							region.getRegion().getMembers().getPlayerDomain().clear();
						}
					}
					if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) { // Don't fall through
						break;
					}
				case ADD:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							for (OfflinePlayer player : players) {
								region.getRegion().getOwners().addPlayer(player.getUniqueId());
							}
						} else { // Members
							for (OfflinePlayer player : players) {
								region.getRegion().getMembers().addPlayer(player.getUniqueId());
							}
						}
					}
					break;
				case REMOVE:
					for (WorldGuardRegion region : regions) {
						if (owners) {
							for (OfflinePlayer player : players) {
								region.getRegion().getOwners().removePlayer(player.getUniqueId());
							}
						} else { // Members
							for (OfflinePlayer player : players) {
								region.getRegion().getMembers().removePlayer(player.getUniqueId());
							}
						}
					}
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	public @NotNull Class<?> getReturnType() {
		return groups ? String.class : OfflinePlayer.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return "the " + (groups ? "group" : "player") + " "
				+ (owners ? "owners" : "members")
				+ " of " + getExpr().toString(event, debug);
	}

}
