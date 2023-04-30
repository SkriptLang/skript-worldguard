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

public class ExprRegionFlag extends SimpleExpression<String> {

    static {

        Skript.registerExpression(ExprRegionFlag.class, String.class, ExpressionType.COMBINED, "[the] flag %string% of region %string% in [world] %world%");

    }

    private Expression<String> flag;
    private Expression<String> region;
    private Expression<World> world;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expression, int arg1, Kleenean arg2, SkriptParser.ParseResult arg3) {
        flag = (Expression<String>) expression[0];
        region = (Expression<String>) expression[1];
        world = (Expression<World>) expression[2];
        return true;

    }

    @Override
    protected String[] get(Event event) {
        Flag<?> fl = null;

        WorldGuard wg = WorldGuard.getInstance();
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world.getSingle(event)));
        ProtectedRegion rg = regions.getRegion(region.getSingle(event));

        fl = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), flag.getSingle(event));

        Object value = rg.getFlag(fl);

        return new String[]{value.toString()};
    }

    @Override
    public String toString(Event arg0, boolean arg1) {
        return "WorldGuard region flag";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?>[] acceptChange(final Changer.ChangeMode mode){
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) { return CollectionUtils.array(String.class);}
        return null;
    }

    public void change(Event e, Object[] delta, Changer.ChangeMode mode){

        Flag<?> fl = null;

        WorldGuard wg = WorldGuard.getInstance();
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world.getSingle(e)));

        fl = Flags.fuzzyMatchFlag(wg.getFlagRegistry(), flag.getSingle(e));
        ProtectedRegion rg = regions.getRegion(region.getSingle(e));

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
