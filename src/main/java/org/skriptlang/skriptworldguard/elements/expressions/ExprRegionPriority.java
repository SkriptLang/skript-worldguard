package org.skriptlang.skriptworldguard.elements.expressions;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.skriptlang.skriptworldguard.SkriptWorldGuard;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class ExprRegionPriority extends SimplePropertyExpression<WorldGuardRegion, Number> {

    static {

        register(ExprRegionPriority.class, Number.class, "priority", "worldguardregions");

    }


    private Expression<WorldGuardRegion> region;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expression, int arg1, Kleenean arg2, SkriptParser.ParseResult arg3) {
        region = (Expression<WorldGuardRegion>) expression[0];
        return true;

    }

    @Override
    public Number convert(WorldGuardRegion rg) {

        Number value = rg.getRegion().getPriority();

        return value;
    }

    @Override
    protected String getPropertyName() {
        return "priority";
    }


    @Override
    public String toString(Event arg0, boolean arg1) {
        return "WorldGuard region priority";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }



    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        if (mode == Changer.ChangeMode.SET) { return CollectionUtils.array(Number.class);}
        return null;
    }

    public void change(Event e, Object[] delta, Changer.ChangeMode mode){


        ProtectedRegion rg = region.getSingle(e).getRegion();

        if (rg != null){
            if (mode == Changer.ChangeMode.SET && delta != null){
                rg.setPriority(((Number) delta[0]).intValue());
            }
            else{
                SkriptWorldGuard.getInstance().getLogger().warning("A region priority can only be set.");
            }
        }else{
            SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + "\"" + region.toString() +"\".");
        }
    }



}
