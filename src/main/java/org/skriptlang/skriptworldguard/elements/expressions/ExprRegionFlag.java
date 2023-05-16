package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.skriptlang.skriptworldguard.SkriptWorldGuard;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import java.util.ArrayList;
import java.util.List;

public class ExprRegionFlag extends PropertyExpression<WorldGuardRegion, Flag> {

    private static final WorldGuard WORLD_GUARD = WorldGuard.getInstance();

    static {
        Skript.registerExpression(ExprRegionFlag.class, Flag.class, ExpressionType.COMBINED,"[worldguard] flag[s] %strings%", "worldguardregions");
    }

    private Expression<String> exprFlag;
    private Expression<WorldGuardRegion[]> exprRegions;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expression, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        exprFlag = (Expression<String>) expression[0];
        exprRegions = (Expression<WorldGuardRegion[]>) expression[1];
        return true;
    }

    @Override
    protected Flag[] get(Event event, WorldGuardRegion[] regions) {
        String flag = exprFlag.getSingle(event);
        if(flag != null) {
            Flag<?> flagMatch = Flags.fuzzyMatchFlag(WORLD_GUARD.getFlagRegistry(), flag);
            List<Flag> result = new ArrayList<>();
            for (WorldGuardRegion region : regions) {
                ProtectedRegion rg = region.getRegion();
                result.add((Flag) rg.getFlag(flagMatch));
            }
            return result.toArray(new Flag[0]);
        }
        return null;
    }

    @Override
    public Class<?>[] acceptChange(ChangeMode mode){
        switch (mode){
            case SET:
            case DELETE:
            case RESET:
                return CollectionUtils.array(String.class, Double.class, Boolean.class);
            default:
                return null;
        }
    }

    public void change(Event event, Object[] delta, Changer.ChangeMode mode){
        Flag<?> flagMatch;
        String flag = exprFlag.getSingle(event);
        WorldGuardRegion[] regions = exprRegions.getSingle(event);
        if(flag == null || regions == null){
            return;
        }
        WorldGuard wg = WorldGuard.getInstance();
        flagMatch = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), flag);
        if(flagMatch != null) {
            for (WorldGuardRegion region : regions) {
                ProtectedRegion rg = region.getRegion();
                switch (mode) {
                    case SET:
                        if (delta != null) {
                            if (delta[0] instanceof Boolean) {
                                if ((Boolean) delta[0]) {
                                    rg.setFlag((StateFlag) flagMatch, StateFlag.State.ALLOW);
                                } else {
                                    rg.setFlag((StateFlag) flagMatch, StateFlag.State.DENY);
                                }
                            } else if (delta[0] instanceof String) {
                                rg.setFlag((StringFlag) flagMatch, (String) delta[0]);
                            } else if (delta[0] instanceof Integer) {
                                rg.setFlag((IntegerFlag) flagMatch, (int) delta[0]);
                            } else if (delta[0] instanceof Double) {
                                rg.setFlag((DoubleFlag) flagMatch, (double) delta[0]);
                            } else {
                                SkriptWorldGuard.getInstance().getLogger().warning("Region flag " + flagMatch.getName() + " cannot be set to: " + delta[0]);
                            }
                        }
                        break;
                    case RESET:
                    case DELETE:
                        rg.setFlag(flagMatch, null);
                        break;
                    default:
                        assert false;
                }
            }
        }else{
            SkriptWorldGuard.getInstance().getLogger().warning("Could not find flag " +  flagMatch.getName());
        }
    }

    @Override
    public Class<? extends Flag> getReturnType() {
        return Flag.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "flag " + exprFlag.toString(event, debug) + " of region " + exprRegions.toString(event, debug) ;
    }
}
