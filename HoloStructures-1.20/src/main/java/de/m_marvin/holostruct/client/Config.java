package de.m_marvin.holostruct.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
	
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static ModConfigSpec CONFIG;
	
	public static final String LEVELBOUND = "levelbound";
	public static ModConfigSpec.IntValue COMMAND_TIMEOUT;
	public static ModConfigSpec.IntValue PACKAGE_TIMEOUT;
	
	public static final String CATAGORY_BLUEPRINTS = "blueprints";
	public static ModConfigSpec.IntValue PLACEMENT_STATE_FIX_ITERATIONS;
	public static ModConfigSpec.ConfigValue<String> FIX_IGNORED_BLOCK_STATE_PROPS;
	
	public static final String CATEGORY_FILES = "files";
	public static ModConfigSpec.ConfigValue<String> DEFAULT_BLUEPRINT_FOLDER;
	public static ModConfigSpec.ConfigValue<String> ADDITIONAL_BLUEPRINT_FOLDERS; // TODO replace {$worldname$}
	public static ModConfigSpec.EnumValue<BlueprintFormat> DEFAULT_BLUEPRINT_FORMAT;
	
	static {
		BUILDER.comment("Settings of the client command dispatcher");
		BUILDER.push(LEVELBOUND);
		COMMAND_TIMEOUT = BUILDER.comment("Timeout in ms to wait for response when executing an command").defineInRange("command_timeout", 2000, 500, 60000);
		PACKAGE_TIMEOUT = BUILDER.comment("Timeout in ms to wait for response when sending a network package").defineInRange("package_timeout", 2000, 100, 60000);
		BUILDER.comment("Settings of the blueprints (copy paste selection etc)");
		BUILDER.push(CATAGORY_BLUEPRINTS);		
		PLACEMENT_STATE_FIX_ITERATIONS = BUILDER.comment("Ammount of itterations to try fixing misconnected blocks after pasting a blueprint.").defineInRange("placement_state_fix_iterations", 4, 1, 64);
		FIX_IGNORED_BLOCK_STATE_PROPS = BUILDER.comment("Blockstate properties ignored by the validation algorithm.").define("fix_ignored_block_state_props", "power,powered");
		BUILDER.pop();
		BUILDER.comment("Settings of the files loaded and created by the mod");
		BUILDER.push(CATEGORY_FILES);
		DEFAULT_BLUEPRINT_FOLDER = BUILDER.comment("Default blueprint folder used if no path specified in the commands.").define("default_blueprint_folder", "/schematics");
		ADDITIONAL_BLUEPRINT_FOLDERS = BUILDER.comment("Additional paths that are aviable in the commands via there names. (name=path,name=path,...)").define("additional_blueprint_folders", "worldedit=/config/worldedit/schematics");
		DEFAULT_BLUEPRINT_FORMAT = BUILDER.comment("The default format used by the save and load command.").defineEnum("default_blueprint_format", BlueprintFormat.NBT);
		
		CONFIG = BUILDER.build();
	}

	public static String getDefaultFolder() {
		return Config.DEFAULT_BLUEPRINT_FOLDER.get();
	}
	
	public static Map<String, String> getAdditionalFolders() {
		return Config.parseMap(Config.ADDITIONAL_BLUEPRINT_FOLDERS.get(), (path) -> path);
	}
	
	public static String getFolder(String name) {
		return getAdditionalFolders().getOrDefault(name, getDefaultFolder());
	}
	
	public static <T> Map<String, T> parseMap(String configMapString, Function<String, T> valueParser) {
		Map<String, T> map = new HashMap<>();
		Stream.of(configMapString.split(",")).forEach((s) -> {
			String[] ss = s.split("=");
			map.put(ss[0], valueParser.apply(ss[1]));
		});
		return map;
	}
	
	public static <T> String makeMap(Map<String, T> map, Function<T, String> valueMapper) {
		return map.entrySet().stream().map((entry) -> entry.getKey().toString() + " = " + valueMapper.apply(entry.getValue())).reduce((a, b) -> a + "," + b).get();
	}
	
	public static void register() {
		ModLoadingContext.get().registerConfig(Type.CLIENT, CONFIG);
	}
	
}
