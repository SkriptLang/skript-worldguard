package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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

	static {
		Skript.registerCondition(CondIsMemberOwner.class,
				"%offlineplayers/strings% (is|are) ([a] member|owner:[(the|an)] owner) of %worldguardregions%",
				"%offlineplayers/strings% (is|are)(n't| not) ([a] member|owner:[(the|an)] owner) of %worldguardregions%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Object> users;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<WorldGuardRegion> regions;
	private boolean owner;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		users = (Expression<Object>) exprs[0];
		regions = (Expression<WorldGuardRegion>) exprs[1];
		owner = parseResult.hasTag("owner");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return users.check(e,
				user -> {
					if (user instanceof OfflinePlayer) {
						LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer((OfflinePlayer) user);
						return regions.check(e,
								region -> owner ? region.getRegion().isOwner(localPlayer) : region.getRegion().isMember(localPlayer));
					} else { // It's a String
						String group = (String) user;
						return regions.check(e,
								region -> owner ? region.getRegion().getOwners().getGroups().contains(group) : region.getRegion().getMembers().getGroups().contains(group));
					}
				}, isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		boolean isSingle = users.isSingle();
		return users.toString(e, debug) + " " + (isSingle ? "is" : "are")
				+ (isNegated() ? " not" : "")
				+ (isSingle ? " an" : " the") + " " + (owner ? "owner" : "member") + (isSingle ? "" : "s")
				+ " of " + regions.toString(e, debug);
	}

}
