package io.github.apickledwalrus.skriptworldguard;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

public class RegionClasses {

	public RegionClasses() {

		Classes.registerClass(new ClassInfo<>(SkriptRegion.class, "worldguardregion")
				.user("worldguard regions?")
				.name("Region")
				.description("A WorldGuard region.")
				.examples("region \"region\" in world(\"world\"")
				.since("1.0")
				.parser(new Parser<SkriptRegion>() {
					@Nullable
					@Override
					public SkriptRegion parse(String s, ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					@NotNull
					public String toString(SkriptRegion region, int flags) {
						return region.toString();
					}

					@Override
					@NotNull
					public String toVariableNameString(SkriptRegion region) {
						return "worldguardregion:" + region;
					}

					@Override
					@NotNull
					public String getVariableNamePattern() {
						return "worldguardregion:.+";
					}
				})
				.serializer(new Serializer<SkriptRegion>() {
					@Override
					@NotNull
					public Fields serialize(SkriptRegion region) {
						Fields f = new Fields();
						f.putObject("world", region.getWorld());
						f.putObject("id", region.getRegion().getId());
						return f;
					}

					@Override
					public void deserialize(SkriptRegion region, Fields fields) {
						assert false;
					}

					@Override
					protected SkriptRegion deserialize(Fields fields) throws StreamCorruptedException {
						World world = fields.getObject("world", World.class);
						String id = fields.getObject("id", String.class);
						if (world == null || id == null) {
							throw new StreamCorruptedException();
						}
						ProtectedRegion region = RegionUtils.getRegion(world, id);
						if (region == null) {
							throw new StreamCorruptedException("The " + RegionUtils.toString(world, id) + " from WorldGuard could not be found. Does it still exist?");
						}
						return new SkriptRegion(world, region);
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false;
					}
				})
		);

	}

}
