package de.m_marvin.holostructures.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import de.m_marvin.holostructures.client.blueprints.BlueprintLoader.BlueprintFormat;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class Config {
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static ForgeConfigSpec CONFIG;
	
	public static final String CATAGORY_BLUEPRINTS = "blueprints";
	public static ForgeConfigSpec.IntValue PLACEMENT_STATE_FIX_ITERATIONS;
	
	public static final String CATEGORY_FILES = "files";
	public static ForgeConfigSpec.ConfigValue<String> DEFAULT_BLUEPRINT_FOLDER;
	public static ForgeConfigSpec.ConfigValue<String> ADDITIONAL_BLUEPRINT_FOLDERS; // TODO replace {$worldname$}
	public static ForgeConfigSpec.EnumValue<BlueprintFormat> DEFAULT_BLUEPRINT_FORMAT;
	
	static {
		BUILDER.comment("Settings of the blueprints (copy paste selection etc)");
		BUILDER.push(CATAGORY_BLUEPRINTS);
		PLACEMENT_STATE_FIX_ITERATIONS = BUILDER.comment("Ammount of itterations to try fixing misconnected blocks after pasting a blueprint. Used as multiplier of the ammount of blocks to fix.").defineInRange("placement_state_fix_iterations", 1, 1, 64);
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
