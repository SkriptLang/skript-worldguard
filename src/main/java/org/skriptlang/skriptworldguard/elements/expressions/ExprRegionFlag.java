package org.skriptlang.skriptworldguard.elements.expressions;



import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
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


public class ExprRegionFlag extends PropertyExpression<WorldGuardRegion, String> {

    static {

        register(ExprRegionFlag.class, String.class, "[worldguard] flag %string%", "worldguardregions");

    }

    private Expression<String> flag;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expression, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        flag = (Expression<String>) expression[0];
        setExpr((Expression<? extends WorldGuardRegion>) expression[1]);
        return true;

    }

    @Override
    protected String[] get(Event e, WorldGuardRegion[] region) {
        Flag<?> fl = null;


        WorldGuard wg = WorldGuard.getInstance();
        fl = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), flag.getSingle(e));

        List<String> finalv = new ArrayList<>();

        for (WorldGuardRegion wrg : region){
            ProtectedRegion rg = wrg.getRegion();
            if (rg != null){
                finalv.add(rg.getFlag(fl).toString());
            }
        }



        return (String[]) finalv.toArray();
    }

    @Override
    public String toString(Event e, boolean debug) {

        return "flag \"" + flag.getSingle(e) + "\" of region" ;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }



    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) { return CollectionUtils.array(String.class);}
        return null;
    }

    public void change(Event e, Object[] delta, Changer.ChangeMode mode){

        Flag<?> fl = null;

        WorldGuard wg = WorldGuard.getInstance();
        WorldGuardRegion[] regions = getExpr().getArray(e);

        fl = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), flag.getSingle(e));
        for (WorldGuardRegion region : regions){
            ProtectedRegion rg = region.getRegion();
            if (rg != null && fl != null){
                if (mode == Changer.ChangeMode.SET && delta != null){
                    if (delta[0] instanceof Boolean) {
                        if ((Boolean) delta[0]) {
                            rg.setFlag((StateFlag) fl, StateFlag.State.ALLOW);
                        } else {
                            rg.setFlag((StateFlag) fl, StateFlag.State.DENY);
                        }
                    }
                    else if (delta[0] instanceof String) {
                        rg.setFlag((StringFlag) fl, (String) delta[0]);
                    } else if (delta[0] instanceof Integer) {
                        rg.setFlag((IntegerFlag) fl, (int) delta[0]);
                    } else if (delta[0] instanceof Double) {
                        rg.setFlag((DoubleFlag) fl, (double) delta[0]);
                    } else {
                        SkriptWorldGuard.getInstance().getLogger().warning("Region flag " + "\"" + fl.getName() + "\"" + " cannot be set to: " + delta[0]);
                    }
                }
                else if(mode == Changer.ChangeMode.DELETE){
                    rg.setFlag(fl, null);
                }
                else{
                    SkriptWorldGuard.getInstance().getLogger().warning("A flag can only be set or cleared.");
                }
            }else{
                if (rg == null) {
                    SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + "\"" + rg.getId()  +"\".");
                }
                if (flag == null){
                    SkriptWorldGuard.getInstance().getLogger().warning("Could not find flag " + "\"" + flag.getSingle(e) +"\".");
                }
            }
        }
    }
}
