package de.m_marvin.holostruct;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The server side config file.
 * Mostly called "permisson config"
 * @author Marvin Koehler
 *
 */
public class ServerConfig {
	
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static ModConfigSpec CONFIG;
	
	public static final String ACCESS_LEVEL = "access_level";
	public static ModConfigSpec.BooleanValue ALLOW_READ;
	public static ModConfigSpec.BooleanValue ALLOW_COPY;
	public static ModConfigSpec.BooleanValue ALLOW_WRITE;
	
	static {
		BUILDER.comment("Settings of the client command dispatcher");
		BUILDER.push(ACCESS_LEVEL);
		ALLOW_READ = BUILDER.comment("If the client is allowed to read blocks to confirm the correct placement of holograms").define("allow_read", true);
		ALLOW_COPY = BUILDER.comment("If the client is allowed to read blocks and (if previleged) nbt data to copy structures from the world").define("allow_copy", true);
		ALLOW_WRITE = BUILDER.comment("If the client is allowed to (if previleged) automatically place blocks via commands like /setblock").define("allow_write", true);
		
		CONFIG = BUILDER.build();
	}
	
	public static void loadDefault() {
		load("");
	}
	
	public static void load(String config) {
		try {
			CommentedConfig configuration = TomlFormat.instance().createParser().parse(config);
			CONFIG.acceptConfig(configuration);
		} catch (Throwable e) {
			HoloStruct.LOGGER.error("Failed to read remote server configuration string: {}", e);
		}
	}
	
	public static String write() {
		try {
			Field field = ModConfigSpec.class.getDeclaredField("childConfig");
			field.setAccessible(true);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			TomlFormat.instance().createWriter().write((UnmodifiableConfig) field.get(CONFIG), buffer);
			return new String(buffer.toByteArray());
		} catch (Throwable e) {
			HoloStruct.LOGGER.error("Failed to write remote server configuration string: {}", e);
			e.printStackTrace();
			return null;
		}
	}

	public static void register() {
		ModLoadingContext.get().registerConfig(Type.SERVER, CONFIG);
	}
	
}
