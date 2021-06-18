package io.github.apickledwalrus.skriptworldguard;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnumUtils;
import ch.njol.yggdrasil.Fields;
import com.sk89q.worldguard.session.MoveType;
import io.github.apickledwalrus.skriptworldguard.worldguard.RegionUtils;
import io.github.apickledwalrus.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionClasses {

	public RegionClasses() {

		Classes.registerClass(new ClassInfo<>(WorldGuardRegion.class, "worldguardregion")
				.user("worldguard regions?")
				.name("Region")
				.description("A WorldGuard region.")
				.examples("region \"region\" in world(\"world\"")
				.requiredPlugins("WorldGuard 7")
				.since("1.0")
				.parser(new Parser<WorldGuardRegion>() {
					// TODO maybe we should do something else here... perhaps make use of SkriptParser methods?
					final Pattern regionPattern = Pattern.compile(
							"(?:the )?(?:worldguard )?region (?:with (?:the )?(?:name|id) |named )?\"(.+)\" (?:in|of) (?:(?:the )?world )?\"(.+)\""
					);

					@Nullable
					@Override
					public WorldGuardRegion parse(String s, ParseContext context) {
						if (context == ParseContext.EVENT || context == ParseContext.COMMAND) {
							Matcher matcher = regionPattern.matcher(s);
							if (matcher.matches()) {
								String id = matcher.group(1);
								World world = Bukkit.getWorld(matcher.group(2));
								return world == null ? null : RegionUtils.getRegion(world, id);
							}
						}
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return true;
					}

					@Override
					@NotNull
					public String toString(WorldGuardRegion region, int flags) {
						return region.toString();
					}

					@Override
					@NotNull
					public String toVariableNameString(WorldGuardRegion region) {
						return "worldguardregion:" + region;
					}

					@Override
					@NotNull
					public String getVariableNamePattern() {
						return "worldguardregion:.+";
					}
				})
				.serializer(new Serializer<WorldGuardRegion>() {
					@Override
					@NotNull
					public Fields serialize(WorldGuardRegion region) {
						Fields f = new Fields();
						f.putObject("world", region.getWorld());
						f.putObject("id", region.getRegion().getId());
						return f;
					}

					@Override
					public void deserialize(WorldGuardRegion region, Fields fields) {
						assert false;
					}

					@Override
					protected WorldGuardRegion deserialize(Fields fields) throws StreamCorruptedException {
						World world = fields.getObject("world", World.class);
						String id = fields.getObject("id", String.class);
						if (world == null || id == null) {
							throw new StreamCorruptedException();
						}
						WorldGuardRegion region = RegionUtils.getRegion(world, id);
						if (region == null) {
							throw new StreamCorruptedException("The " + RegionUtils.toString(world, id) + " from WorldGuard could not be found. Does it still exist?");
						}
						return region;
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

		@SuppressWarnings("NullableProblems") EnumUtils<MoveType> moveTypes = new EnumUtils<>(MoveType.class, "worldguard move types");
		Classes.registerClass(new ClassInfo<>(MoveType.class, "worldguardmovetype")
				.user("worldguard move ?types?")
				.name("WorldGuard Move Type")
				.description("The move type in a WorldGuard enter/leave event.")
				.usage(moveTypes.getAllNames())
				.requiredPlugins("WorldGuard 7")
				.examples("on region enter:",
						"\tsend \"The move type is %the move type%\"")
				.since("1.0")
				.parser(new Parser<MoveType>() {
					@Override
					public MoveType parse(String s, ParseContext context) {
						return moveTypes.parse(s);
					}

					@Override
					public boolean canParse(ParseContext context) {
						return true;
					}

					@Override
					@NotNull
					public String toString(MoveType moveType, int flags) {
						return moveTypes.toString(moveType, flags);
					}

					@Override
					@NotNull
					public String toVariableNameString(MoveType moveType) {
						return "clicktype:" + moveType.name();
					}

					@Override
					@NotNull
					public String getVariableNamePattern() {
						return "clicktype:\\S+";
					}
				})
				.serializer(new EnumSerializer<>(MoveType.class))
		);
	}

}
