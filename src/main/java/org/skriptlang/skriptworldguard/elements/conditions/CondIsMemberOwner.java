package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.domains.DefaultDomain;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

@Name("Is Member/Owner of Region")
@Description("A condition to test whether a player/group is a member/owner of a region.")
@Example("""
	on region enter:
		player is the owner of the region
		message "Welcome back to %region%"
		message "%player's name% just entered %region%" to the members of the region
	""")
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondIsMemberOwner extends Condition {

	// TODO 'direct' flag for whether or not to consider parent regions?
	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondIsMemberOwner.class)
				.supplier(CondIsMemberOwner::new)
				.addPatterns(PropertyCondition.getPatterns(PropertyType.BE,
						"([a] member|owner:[the|an] owner) of %worldguardregions%",
						"offlineplayers/strings"))
				.build());
	}

	private Expression<Object> users;
	private Expression<WorldGuardRegion> regions;
	private boolean isOwner;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		users = (Expression<Object>) exprs[0];
		//noinspection unchecked
		regions = (Expression<WorldGuardRegion>) exprs[1];
		isOwner = parseResult.hasTag("owner");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		WorldGuardRegion[] regions = this.regions.getAll(event);
		return users.check(event, user -> SimpleExpression.check(regions,
				region -> check(region, isOwner, user), false, this.regions.getAnd()), isNegated());
	}

	private static boolean check(WorldGuardRegion region, boolean owner, Object object) {
		DefaultDomain domain;
		if (owner) {
			domain = region.region().getOwners();
		} else {
			domain = region.region().getMembers();
		}
		if (object instanceof OfflinePlayer player) {
			return domain.contains(player.getUniqueId());
		} else if (object instanceof String group) {
			return domain.getGroups().contains(group);
		} else {
			throw new IllegalArgumentException("object must be OfflinePlayer or String");
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		boolean isSingle = users.isSingle();
		builder.append(users);
		if (isSingle) {
			builder.append("is");
		} else {
			builder.append("are");
		}
		if (isNegated()) {
			builder.append("not");
		}
		builder.append("the");
		if (isOwner) {
			if (isSingle) {
				builder.append("owner");
			} else {
				builder.append("owners");
			}
		} else {
			if (isSingle) {
				builder.append("member");
			} else {
				builder.append("members");
			}
		}
		builder.append("of", regions);
		return builder.toString();
	}

}
