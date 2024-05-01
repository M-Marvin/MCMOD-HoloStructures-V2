package de.m_marvin.holostruct.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

/**
 * The client side configuration file.
 * @author Marvin Koehler
 */
public class ClientConfig {
	
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static ModConfigSpec CONFIG;
	
	public static final String LEVELBOUND = "levelbound";
	public static ModConfigSpec.IntValue COMMAND_TIMEOUT;
	public static ModConfigSpec.IntValue PACKAGE_TIMEOUT;
	
	public static final String HOLOGRAMS = "holograms";
	public static ModConfigSpec.IntValue SECTION_UPDATE_DELAY;
	
	public static final String CATAGORY_BLUEPRINTS = "blueprints";
	public static ModConfigSpec.IntValue PLACEMENT_STATE_FIX_ITERATIONS;
	public static ModConfigSpec.ConfigValue<String> FIX_IGNORED_BLOCK_STATE_PROPS;
	
	public static final String CATEGORY_FILES = "files";
	public static ModConfigSpec.ConfigValue<String> DEFAULT_BLUEPRINT_FOLDER;
	public static ConfigValue<List<? extends String>> ADDITIONAL_BLUEPRINT_FOLDERS;
	public static ModConfigSpec.EnumValue<BlueprintFormat> DEFAULT_BLUEPRINT_FORMAT;

	public static final Pattern FOLDER_ENTRY_PATTERN = Pattern.compile("([^\\|\\:\\<\\>\\?\\*\\\"]+):([^\\|\\:\\<\\>\\?\\*\\\"]+)");
	public static final String WORLD_FOLDER_KEY = "{$world_folder}";
	
	static {
		BUILDER.comment("Settings of the levelbound instance (allows access from the client to the server level)");
		BUILDER.push(LEVELBOUND);
		COMMAND_TIMEOUT = BUILDER.comment("Timeout in ms to wait for response when executing an command")
				.defineInRange("command_timeout", 2000, 500, 60000);
		PACKAGE_TIMEOUT = BUILDER.comment("Timeout in ms to wait for response when sending a network package")
				.defineInRange("package_timeout", 2000, 100, 60000);
		BUILDER.pop();
		
		BUILDER.push(HOLOGRAMS);
		SECTION_UPDATE_DELAY = BUILDER.comment("Time in ms to wait between triggering the updates of the individual hologram sections, samler timeouts make the updates faster, but increase the risk of losing individual blocks due to timeouts of the server")
				.defineInRange("section_update_delay", 1000, 100, 10000);
		BUILDER.pop();
		
		BUILDER.comment("Settings of the files loaded and created by the mod");
		BUILDER.push(CATEGORY_FILES);
		DEFAULT_BLUEPRINT_FOLDER = BUILDER.comment("Default blueprint folder used if no path specified in the commands.")
				.define("default_blueprint_folder", "/schematics");
		ADDITIONAL_BLUEPRINT_FOLDERS = BUILDER.comment("Additional paths that are aviable in the commands via their names.")
				.defineList("aditional_folders", (List<String>) Arrays.asList("world:/saves/" + WORLD_FOLDER_KEY + "/generated/minecraft/structures", "worldedit:/config/worldedit/schematics"), s -> true);
		DEFAULT_BLUEPRINT_FORMAT = BUILDER.comment("The default format used by the save and load command.")
				.defineEnum("default_blueprint_format", BlueprintFormat.NBT);
		BUILDER.pop();
		
		CONFIG = BUILDER.build();
	}

	public static Map<String, String> getAdditionalFolders() {
		return getAdditionalFolders(HoloStructClient.getLocalLevelFolderName());
	}
	
	public static Map<String, String> getAdditionalFolders(Optional<String> worldFolderName) {
		return ADDITIONAL_BLUEPRINT_FOLDERS.get().stream()
				.filter(e -> !e.contains(WORLD_FOLDER_KEY) || worldFolderName.isPresent())
				.map(e -> e.replace(WORLD_FOLDER_KEY, worldFolderName.orElseGet(() -> "n/a")))
				.map(e -> FOLDER_ENTRY_PATTERN.matcher(e))
				.filter(Matcher::find)
				.collect(Collectors.toMap(m -> m.group(1), m -> m.group(2)));
	}
	
	public static String getFolder(String name) {
		return getAdditionalFolders().getOrDefault(name, DEFAULT_BLUEPRINT_FOLDER.get());
	}
	
	public static void register() {
		ModLoadingContext.get().registerConfig(Type.CLIENT, CONFIG);
	}
	
}
