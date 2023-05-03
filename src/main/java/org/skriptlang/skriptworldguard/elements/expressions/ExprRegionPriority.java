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

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expression, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        region = (Expression<WorldGuardRegion>) expression[0];
        return true;
    }

    @Override
    public Number convert(WorldGuardRegion rg) {
        return rg.getRegion().getPriority();
    }

    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        switch(mode){
            case SET:
            case ADD:
            case REMOVE:
                return CollectionUtils.array(Number.class);
            case RESET:
            case DELETE:
                return CollectionUtils.array();
        }
        return null;
    }

    public void change(Event event, Object[] delta, Changer.ChangeMode mode){
        ProtectedRegion rg = region.getSingle(event).getRegion();
        if (rg != null){
            if (delta != null) {
                switch(mode){
                    case SET:
                        rg.setPriority(((Number) delta[0]).intValue());
                    case ADD:
                        rg.setPriority(rg.getPriority() + ((Number) delta[0]).intValue());
                    case REMOVE:
                        rg.setPriority(rg.getPriority() - ((Number) delta[0]).intValue());
                }
            } else{
                switch(mode){
                    case RESET:
                    case DELETE:
                        rg.setPriority(0);
                }
            }
        }else{
            SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + "\"" + region.toString() +"\".");
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "priority";
    }
}
