package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.sk89q.worldguard.domains.DefaultDomain;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
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
@Example("""
	on region enter:
		message "You have entered %region%. It is owned by %owners of region%."
	""")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class ExprRegionMembersOwners extends PropertyExpression<WorldGuardRegion, Object> {

	public static void register(SyntaxRegistry registry) {
		// TODO consider alternative pattern: player members/owners, member/owner groups
		register(registry, ExprRegionMembersOwners.class, Object.class,
				"[player|:group] (members|:owners)", "worldguardregions");
	}

	private boolean isGroups;
	private boolean isOwners;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isGroups = parseResult.hasTag("group");
		isOwners = parseResult.hasTag("owners");
		//noinspection unchecked
		setExpr((Expression<? extends WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Object [] get(Event event, WorldGuardRegion [] regions) {
		List<DefaultDomain> domains = new ArrayList<>();
		if (isOwners) {
			for (WorldGuardRegion region : regions) {
				domains.add(region.region().getOwners());
			}
		} else {
			for (WorldGuardRegion region : regions) {
				domains.add(region.region().getMembers());
			}
		}

		if (isGroups) {
			List<String> groups = new ArrayList<>();
			for (DefaultDomain domain : domains) {
				groups.addAll(domain.getGroups());
			}
			return groups.toArray(new String[0]);
		} else {
			List<OfflinePlayer> players = new ArrayList<>();
			for (DefaultDomain domain : domains) {
				for (UUID uuid : domain.getUniqueIds()) {
					players.add(Bukkit.getOfflinePlayer(uuid));
				}
			}
			return players.toArray(new OfflinePlayer[0]);
		}
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET ->
					isGroups ? CollectionUtils.array(String[].class) : CollectionUtils.array(OfflinePlayer[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if ((delta == null && mode != ChangeMode.DELETE && mode != ChangeMode.RESET)) {
			return;
		}

		WorldGuardRegion[] regions = getExpr().getArray(event);
		if (regions.length == 0) {
			return;
		}

		List<DefaultDomain> domains = new ArrayList<>();
		if (isOwners) {
			for (WorldGuardRegion region : regions) {
				domains.add(region.region().getOwners());
			}
		} else {
			for (WorldGuardRegion region : regions) {
				domains.add(region.region().getMembers());
			}
		}

		if (isGroups) {
			switch (mode) {
				case SET:
				case DELETE:
				case RESET:
					for (DefaultDomain domain : domains) {
						domain.getGroupDomain().clear();
					}
					if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) { // Don't fall through
						break;
					}
				case ADD:
					for (DefaultDomain domain : domains) {
						for (Object group : delta) {
							domain.addGroup((String) group);
						}
					}
					break;
				case REMOVE:
					for (DefaultDomain domain : domains) {
						for (Object group : delta) {
							domain.removeGroup((String) group);
						}
					}
					break;
				default:
					assert false;
			}
		} else { // Players
			switch (mode) {
				case SET:
				case DELETE:
				case RESET:
					for (DefaultDomain domain : domains) {
						domain.getPlayerDomain().clear();
					}
					if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) { // Don't fall through
						break;
					}
				case ADD:
					for (DefaultDomain domain : domains) {
						for (Object player : delta) {
							domain.addPlayer(((OfflinePlayer) player).getUniqueId());
						}
					}
					break;
				case REMOVE:
					for (DefaultDomain domain : domains) {
						for (Object player : delta) {
							domain.removePlayer(((OfflinePlayer) player).getUniqueId());
						}
					}
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		// one region may have many players/groups
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return isGroups ? String.class : OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (isGroups) {
			builder.append("group");
		} else {
			builder.append("player");
		}
		if (isOwners) {
			builder.append("owners");
		} else {
			builder.append("members");
		}
		builder.append("of", getExpr());
		return builder.toString();
	}

}
