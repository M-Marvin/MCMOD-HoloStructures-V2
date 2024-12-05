package de.m_marvin.holostruct;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import de.m_marvin.holostruct.client.ClientConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec.ILoadedConfig;
import net.neoforged.fml.config.ModConfig;
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
			
			// Get client loaded config
			Field loadedConfigField = ClientConfig.CONFIG.getClass().getDeclaredField("loadedConfig");
			loadedConfigField.setAccessible(true);
			ILoadedConfig clientLoadedConfig = (ILoadedConfig) loadedConfigField.get(ClientConfig.CONFIG);
			
			// Get mod config from client loaded config
			Field modConfigField = clientLoadedConfig.getClass().getDeclaredField("modConfig");
			modConfigField.setAccessible(true);
			ModConfig modConfig = (ModConfig) modConfigField.get(clientLoadedConfig);
			
			// Construct new server config from parsed commented config and client mod config
			Class<?> loadedConfigClass = Class.forName("net.neoforged.fml.config.LoadedConfig");
			Constructor<?> constr = loadedConfigClass.getConstructor(CommentedConfig.class, Path.class, ModConfig.class);
			ILoadedConfig serverLoadedConfig = (ILoadedConfig) constr.newInstance((CommentedConfig) configuration, null, modConfig);
			
			CONFIG.acceptConfig(serverLoadedConfig);
		} catch (Throwable e) {
			HoloStruct.LOGGER.error("Failed to read remote server configuration string: {}", e);
		}
	}
	
	public static String write() {
		try {
			Field field = ModConfigSpec.class.getDeclaredField("loadedConfig");
			field.setAccessible(true);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ILoadedConfig loadedConfig = (ILoadedConfig) field.get(CONFIG);
			TomlFormat.instance().createWriter().write(loadedConfig.config(), buffer);
			return new String(buffer.toByteArray());
		} catch (Throwable e) {
			HoloStruct.LOGGER.error("Failed to write remote server configuration string: {}", e);
			e.printStackTrace();
			return null;
		}
	}

	public static void register(ModContainer modContainer) {
		modContainer.registerConfig(Type.SERVER, CONFIG);
	}
	
}
