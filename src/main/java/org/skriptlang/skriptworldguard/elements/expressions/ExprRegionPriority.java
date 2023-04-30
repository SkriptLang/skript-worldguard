package org.skriptlang.skriptworldguard.elements.expressions;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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

public class ExprRegionPriority extends SimpleExpression<Number> {

    static {

        Skript.registerExpression(ExprRegionPriority.class, Number.class, ExpressionType.COMBINED, "[the] priority of region %string% in [world] %world%");

    }

    private Expression<String> region;
    private Expression<World> world;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expression, int arg1, Kleenean arg2, SkriptParser.ParseResult arg3) {
        region = (Expression<String>) expression[0];
        world = (Expression<World>) expression[1];
        return true;

    }

    @Override
    protected Number[] get(Event event) {
        Flag<?> fl = null;

        WorldGuard wg = WorldGuard.getInstance();
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world.getSingle(event)));
        ProtectedRegion rg = regions.getRegion(region.getSingle(event));



        Number value = rg.getPriority();

        return new Number[]{value};
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
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        if (mode == Changer.ChangeMode.SET) { return CollectionUtils.array(Number.class);}
        return null;
    }

    public void change(Event e, Object[] delta, Changer.ChangeMode mode){


        WorldGuard wg = WorldGuard.getInstance();
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world.getSingle(e)));

        ProtectedRegion rg = regions.getRegion(region.getSingle(e));

        if (rg != null){
            if (mode == Changer.ChangeMode.SET && delta != null){
                rg.setPriority(((Number) delta[0]).intValue());
            }
            else{
                SkriptWorldGuard.getInstance().getLogger().warning("A region priority can only be set.");
            }
        }else{
            SkriptWorldGuard.getInstance().getLogger().warning("Could not find region " + "\"" + rg.getId()  +"\".");
        }
    }



}
