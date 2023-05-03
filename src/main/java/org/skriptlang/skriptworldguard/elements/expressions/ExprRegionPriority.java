package org.skriptlang.skriptworldguard.elements.expressions;



import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;


import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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
    public boolean init(Expression<?>[] expression, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
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
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }



    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        if (mode == Changer.ChangeMode.SET ||
            mode == Changer.ChangeMode.ADD ||
            mode == Changer.ChangeMode.REMOVE) {
                return CollectionUtils.array(Number.class);
            }
        else if (mode == Changer.ChangeMode.RESET ||
                mode == Changer.ChangeMode.DELETE) {
                return CollectionUtils.array();
        }
        return null;
    }

    public void change(Event e, Object[] delta, Changer.ChangeMode mode){


        ProtectedRegion rg = region.getSingle(e).getRegion();

        if (rg != null){
            if (delta != null) {
                Integer d = ((Number) delta[0]).intValue();
                if (mode == Changer.ChangeMode.SET) {
                    rg.setPriority(((Number) delta[0]).intValue());
                } else if (mode == Changer.ChangeMode.ADD) {
                    rg.setPriority(rg.getPriority() + d);
                } else if (mode == Changer.ChangeMode.REMOVE) {
                    rg.setPriority(rg.getPriority() - d);
                }
            } else{
                if (mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE){
                    rg.setPriority(0);
                }
            }
        }else{
            SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + "\"" + region.toString() +"\".");
        }
    }



}
