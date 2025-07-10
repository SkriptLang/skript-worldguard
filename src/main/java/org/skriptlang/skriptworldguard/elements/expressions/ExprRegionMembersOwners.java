package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.domains.DefaultDomain;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Region Members/Owners")
@Description({
	"An expression to obtain the members/owners of the given regions.",
	"The members/owners of a region are made up of players and groups (strings).",
	"By default, this expression returns both. However, keyword specifiers for each type (player/group) are available."
})
@Example("""
	on region enter:
		message "You have entered %region%. It is owned by %owners of region%."
	""")
@Example("""
	command /promote <text> <player>:
		trigger:
			set {_region} to the region text-argument in the player's world
			if player-argument is an owner of {_region}:
				message "<red>%player-argument% is already an owner of %{_region}%"
			else if player-argument is a member of {_region}:
				add player to the owners of {_region}
				message "<green>%player-argument% has been promoted to an owner of %{_region}%"
			else:
				add player to the members of {_region}
				message "<green>%player-argument% has been promoted to a member of %{_region}%"
	""")
@Since("1.0")
public class ExprRegionMembersOwners extends PropertyExpression<WorldGuardRegion, Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprRegionMembersOwners.class, Object.class, "", "", false)
				.supplier(ExprRegionMembersOwners::new)
				.clearPatterns() // overwrite them
				.addPatterns(getPatterns(
						"[:player|:group] (members|:owners)",
						"worldguardregions"))
				.addPatterns(getPatterns(
						"(member|:owner) (player:players|group:groups)",
						"worldguardregions"))
				.build());
	}

	private enum Type {
		PLAYER,
		GROUP,
		BOTH
	}

	private boolean isOwners;
	private Type type = Type.BOTH;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isOwners = parseResult.hasTag("owners");
		if (parseResult.hasTag("player")) {
			type = Type.PLAYER;
		} else if (parseResult.hasTag("group")) {
			type = Type.GROUP;
		}
		//noinspection unchecked
		setExpr((Expression<? extends WorldGuardRegion>) exprs[0]);
		return true;
	}

	@Override
	protected Object[] get(Event event, WorldGuardRegion[] regions) {
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

		List<Object> values = new ArrayList<>();
		if (type != Type.GROUP) {
			for (DefaultDomain domain : domains) {
				for (UUID uuid : domain.getUniqueIds()) {
					values.add(Bukkit.getOfflinePlayer(uuid));
				}
			}
		}
		if (type != Type.PLAYER) {
			for (DefaultDomain domain : domains) {
				values.addAll(domain.getGroups());
			}
		}
		return values.toArray((Object[]) Array.newInstance(getReturnType(), values.size()));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET -> {
				Class<?>[] types = possibleReturnTypes();
				for (int i = 0; i < types.length; i++) { // allow multiple values
					types[i] = types[i].arrayType();
				}
				yield types;
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
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

		switch (mode) {
			case SET:
				for (DefaultDomain domain : domains) {
					domain.clear();
				}
				//$FALL-THROUGH$
			case ADD:
				assert delta != null;
				for (DefaultDomain domain : domains) {
					for (Object object : delta) {
						if (object instanceof OfflinePlayer player) {
							domain.addPlayer(player.getUniqueId());
						} else {
							domain.addGroup((String) object);
						}
					}
				}
				break;
			case REMOVE:
				assert delta != null;
				for (DefaultDomain domain : domains) {
					for (Object object : delta) {
						if (object instanceof OfflinePlayer player) {
							domain.removePlayer(player.getUniqueId());
						} else {
							domain.removeGroup((String) object);
						}
					}
				}
				break;
			case DELETE:
			case RESET:
				switch (type) {
					case PLAYER -> {
						for (DefaultDomain domain : domains) {
							domain.getPlayerDomain().clear();
						}
					}
					case GROUP -> {
						for (DefaultDomain domain : domains) {
							domain.getGroupDomain().clear();
						}
					}
					case BOTH -> {
						for (DefaultDomain domain : domains) {
							domain.clear();
						}
					}
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		// one region may have many players/groups
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return switch (type) {
			case PLAYER -> OfflinePlayer.class;
			case GROUP -> String.class;
			case BOTH -> Object.class;
		};
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (type == Type.BOTH) {
			return new Class[]{OfflinePlayer.class, String.class};
		}
		return super.possibleReturnTypes();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (type == Type.PLAYER) {
			builder.append("player");
		} else if (type == Type.GROUP) {
			builder.append("group");
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
