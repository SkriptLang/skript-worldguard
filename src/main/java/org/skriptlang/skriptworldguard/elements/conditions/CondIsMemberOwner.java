package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

@Name("Is Member/Owner of Region")
@Description("A condition that tests whether the given players or groups are a member or owner of the given regions.")
@Examples({
	"on region enter:",
		"\tplayer is the owner of the region",
		"\tmessage \"Welcome back to %region%!\"",
		"\tsend \"%player% just entered %region%!\" to the members of the region"
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondIsMemberOwner extends Condition {

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
	private boolean owner;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		users = (Expression<Object>) exprs[0];
		regions = (Expression<WorldGuardRegion>) exprs[1];
		owner = parseResult.hasTag("owner");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(@NotNull Event event) {
		WorldGuardRegion[] regions = this.regions.getAll(event);
		return users.check(event, user -> {
			if (user instanceof OfflinePlayer) {
				LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer((OfflinePlayer) user);
				return SimpleExpression.check(
						regions,
						region -> owner ? region.getRegion().isOwner(localPlayer) : region.getRegion().isMember(localPlayer),
						false,
						this.regions.getAnd()
				);
			} else { // It's a String (group)
				String group = (String) user;
				return SimpleExpression.check(
						regions,
						region -> (owner ? region.getRegion().getOwners() : region.getRegion().getMembers()).getGroups().contains(group),
						false,
						this.regions.getAnd()
				);
			}
		}, isNegated());
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		boolean isSingle = users.isSingle();
		return users.toString(event, debug) + " " + (isSingle ? "is" : "are")
				+ (isNegated() ? " not" : "")
				+ " the " + (owner ? "owner" : "member") + (isSingle ? "" : "s")
				+ " of " + regions.toString(event, debug);
	}

}
