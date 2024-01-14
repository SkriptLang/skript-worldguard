package org.skriptlang.skriptworldguard.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;

public class ExprRegionPriority extends SimplePropertyExpression<WorldGuardRegion, Number> {

    static {
        register(ExprRegionPriority.class, Number.class, "priority", "worldguardregions");
    }

    @Override
    public Number convert(WorldGuardRegion region) {
        return region.getRegion().getPriority();
    }

    @Override
    public Class<?>[] acceptChange(final ChangeMode mode){
        switch(mode){
            case SET:
            case ADD:
            case REMOVE:
                return CollectionUtils.array(Number.class);
            case RESET:
            case DELETE:
                return CollectionUtils.array();
            default:
                return null;
        }
    }

    public void change(Event event, Object[] delta, ChangeMode mode) {
        WorldGuardRegion[] regions = getExpr().getArray(event);
        assert regions != null;
        for (WorldGuardRegion region : regions) {
            if (region != null) {
                ProtectedRegion protectedRegion = region.getRegion();
                if (delta != null) {
                    switch (mode) {
                        case SET:
                            protectedRegion.setPriority(((Number) delta[0]).intValue());
                            break;
                        case ADD:
                            protectedRegion.setPriority(protectedRegion.getPriority() + ((Number) delta[0]).intValue());
                            break;
                        case REMOVE:
                            protectedRegion.setPriority(protectedRegion.getPriority() - ((Number) delta[0]).intValue());
                            break;
                    }
                } else {
                    switch (mode) {
                        case RESET:
                        case DELETE:
                            protectedRegion.setPriority(0);
                    }
                }
            }
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
