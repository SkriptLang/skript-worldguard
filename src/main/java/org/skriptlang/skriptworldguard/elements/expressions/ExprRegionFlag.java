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
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.skriptlang.skriptworldguard.SkriptWorldGuard;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import java.util.ArrayList;
import java.util.List;

public class ExprRegionFlag extends PropertyExpression<WorldGuardRegion, Flag> {

    static {
        Skript.registerExpression(ExprRegionFlag.class, Flag.class, ExpressionType.COMBINED,"[worldguard] flag[s] %strings%", "worldguardregions");
    }

    WorldGuard wg = WorldGuard.getInstance();
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
    protected Flag[] get(Event event, WorldGuardRegion[] region) {
        Flag<?> flag = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), exprFlag.getSingle(event));
        List<Flag> result = new ArrayList<>();
        for (WorldGuardRegion wrg : region){
            ProtectedRegion rg = wrg.getRegion();
            if (rg != null){
                result.add((Flag) rg.getFlag(flag));
            }
        }
        return (Flag[]) result.toArray();
    }

    @Override
    public Class<?>[] acceptChange(ChangeMode mode){
        switch (mode){
            case SET:
            case DELETE:
                return CollectionUtils.array(String.class);
        }
        return null;
    }

    public void change(Event event, Object[] delta, Changer.ChangeMode mode){
        Flag<?> flag;
        if(exprFlag.getSingle(event) == null || exprRegions.getSingle(event) == null){
            return;
        }
        WorldGuard wg = WorldGuard.getInstance();
        WorldGuardRegion[] regions = getExpr().getArray(event);
        flag = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), exprFlag.getSingle(event));
        for (WorldGuardRegion region : regions){
            ProtectedRegion rg = region.getRegion();
            if (rg != null && flag != null){
                switch(mode){
                    case SET:
                        if(delta != null){
                            if (delta[0] instanceof Boolean) {
                                if ((Boolean) delta[0]) {
                                    rg.setFlag((StateFlag) flag, StateFlag.State.ALLOW);
                                } else {
                                    rg.setFlag((StateFlag) flag, StateFlag.State.DENY);
                                }
                            }
                            else if (delta[0] instanceof String) {
                                rg.setFlag((StringFlag) flag, (String) delta[0]);
                            } else if (delta[0] instanceof Integer) {
                                rg.setFlag((IntegerFlag) flag, (int) delta[0]);
                            } else if (delta[0] instanceof Double) {
                                rg.setFlag((DoubleFlag) flag, (double) delta[0]);
                            } else {
                                SkriptWorldGuard.getInstance().getLogger().warning("Region flag " + flag.getName() + " cannot be set to: " + delta[0]);
                            }
                        }
                    case DELETE:
                        rg.setFlag(flag, null);
                    default:
                        Skript.error("A flag can only be set or cleared.");
                }
            }else{
                if (rg == null) {
                    SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + rg.getId());
                }
                if (flag == null){
                    SkriptWorldGuard.getInstance().getLogger().warning("Could not find flag " +  flag.getName());
                }
            }
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
