package de.m_marvin.holostruct.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

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
	public static ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_STATE_PROP_BLACKLIST;
	
	public static final String CATAGORY_BLUEPRINTS = "blueprints";
	public static ModConfigSpec.IntValue PLACEMENT_STATE_FIX_ITERATIONS;
	public static ModConfigSpec.ConfigValue<String> FIX_IGNORED_BLOCK_STATE_PROPS;
	
	public static final String CATEGORY_FILES = "files";
	public static ModConfigSpec.ConfigValue<String> DEFAULT_BLUEPRINT_FOLDER;
	public static ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_BLUEPRINT_FOLDERS;
	public static ModConfigSpec.EnumValue<BlueprintFormat> DEFAULT_BLUEPRINT_FORMAT;
	public static ModConfigSpec.ConfigValue<String> DEFAULT_IMAGE_FOLDER;
	public static ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_IMAGE_FOLDERS;

	public static final String CATEGORY_PIXELART = "pixelart";
	public static ModConfigSpec.ConfigValue<List<? extends String>> DEFAULT_BLOCK_WHITELIST;
	public static ModConfigSpec.ConfigValue<List<? extends String>> DEFAULT_BLOCK_BLACKLIST;
	
	public static final List<String> DEFAULT_BLOCK_PROP_BLACKLIST = Arrays.asList("minecraft:oak_leaves.distance,minecraft:birch_leaves.distance,minecraft:jungle_leaves.distance,minecraft:acacia_leaves.distance,minecraft:dark_oak_leaves.distance,minecraft:mangrove_leaves.distance,minecraft:cherry_leaves.distance,minecraft:azalea_leaves.distance,minecraft:flowering_azalea_leaves.distance".split(","));
	public static final List<String> DEFAULT_WHITELIST = Arrays.asList("minecraft:yellow_wool,minecraft:netherrack,minecraft:bone_block,minecraft:brown_concrete,minecraft:acacia_planks,minecraft:mud,minecraft:cyan_concrete,minecraft:cobbled_deepslate,minecraft:dripstone_block,minecraft:dark_oak_wood,minecraft:calcite,minecraft:blackstone,minecraft:raw_gold_block,minecraft:red_sand,minecraft:purple_terracotta,minecraft:pearlescent_froglight,minecraft:orange_concrete,minecraft:lime_concrete,minecraft:lime_wool,minecraft:warped_wart_block,minecraft:light_blue_concrete,minecraft:ochre_froglight,minecraft:stone_bricks,minecraft:moss_block,minecraft:brewing_stand,minecraft:bricks,minecraft:light_gray_wool,minecraft:light_gray_terracotta,minecraft:mangrove_planks,minecraft:oak_leaves,minecraft:granite,minecraft:pink_wool,minecraft:white_candle,minecraft:soul_soil,minecraft:polished_diorite,minecraft:red_concrete,minecraft:stone,minecraft:sand,minecraft:jungle_planks,minecraft:sculk,minecraft:basalt,minecraft:white_concrete,minecraft:amethyst_block,minecraft:mushroom_stem,minecraft:crimson_nylium,minecraft:warped_planks,minecraft:cyan_wool,minecraft:lapis_block,minecraft:hay_block,minecraft:prismarine,minecraft:cherry_planks,minecraft:iron_block,minecraft:cherry_leaves,minecraft:verdant_froglight,minecraft:cherry_wood,minecraft:cyan_terracotta,minecraft:gravel,minecraft:dark_prismarine,minecraft:soul_sand,minecraft:pink_terracotta,minecraft:snow_block,minecraft:birch_wood,minecraft:nether_wart_block,minecraft:white_terracotta,minecraft:green_wool,minecraft:magenta_concrete,minecraft:red_wool,minecraft:acacia_wood,minecraft:crimson_planks,minecraft:orange_terracotta,minecraft:purple_concrete,minecraft:glow_lichen,minecraft:black_concrete,minecraft_blue_concrete,minecraft:black_wool,minecraft:white_wool,minecraft:flowering_azalea_leaves,minecraft:light_blue_wool,minecraft:lime_terracotta,minecraft:magenta_wool,minecraft:prismarine_bricks,minecraft:purpur_block,minecraft:pink_concrete,minecraft:crafting_table,minecraft:brown_wool,minecraft:green_concrete,minecraft:polished_granite,minecraft:podzol,minecraft:light_gray_concrete,minecraft:gray_terracotta,minecraft:dried_kelp_block,minecraft:green_terracotta,minecraft_mycelium,minecraft_grass_block,minecraft:warped_nylium,minecraft:jungle_wood,minecraft:red_sandstone,minecraft:polished_andesite,minecraft:dark_oak_leaves,minecraft:blue_wool,minecraft:acacia_leaves,minecraft:spruce_wood,minecraft:purple_wool,minecraft:azalea_leaves,minecraft:pumpkin,minecraft:light_weighted_pressure_plate,minecraft:melon,minecraft:cobblestone,minecraft:jungle_leaves,minecraft:tuff,minecraft:diorite,minecraft:coal_block,minecraft:redstone_block,minecraft:red_nether_bricks,minecraft:gray_wool,minecraft:terracotta,minecraft:yellow_concrete,minecraft:gray_concrete,minecraft:sandstone,minecraft:light_blue_terracotta,minecraft:packed_ice,minecraft:warped_hyphae,minecraft:red_terracotta,minecraft:nether_bricks,minecraft:oak_wood,minecraft:emerald_block,minecraft:scaffolding,minecraft:blue_terracotta,minecraft:dirt,minecraft:stripped_cherry_wood,minecraft:raw_copper_block,minecraft:mangrove_wood,minecraft:gold_block,minecraft:oak_planks,minecraft:orange_wool,minecraft:spruce_leaves,minecraft:black_terracotta,minecraft:spruce_planks,minecraft:end_stone,minecraft:andesite,minecraft:deepslate,minecraft:dark_oak_planks,minecraft:magenta_terracotta,minecraft:birch_planks,minecraft:yellow_terracotta,minecraft:packed_mud,minecraft:crimson_hyphae,minecraft:mud_bricks,minecraft:clay,minecraft:brown_terracotta,minecraft:birch_leaves,minecraft:mangrove_roots,minecraft:raw_iron_block".split(","));
	public static final List<String> DEFAULT_BLACKLIST = Arrays.asList("minecraft:bedrock,minecraft:water,minecraft:lava".split(","));
	public static final Pattern BLOCK_PROP_PATTERN = Pattern.compile("([a-z0-9\\:_]+)\\.([a-z0-9_]+)");
	
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
		BLOCK_STATE_PROP_BLACKLIST = BUILDER.comment("Block states ignored when comparing placed blocks with hologram for placement state")
				.defineList("block_state_prop_blacklist", DEFAULT_BLOCK_PROP_BLACKLIST, item -> {
					Matcher m = BLOCK_PROP_PATTERN.matcher((String) item);
					if (m.find()) {
						Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(m.group(1)));
						return block.getStateDefinition().getProperty(m.group(2)) != null;
					}
					return false;
				});
		BUILDER.pop();
		
		BUILDER.comment("Settings of the files loaded and created by the mod");
		BUILDER.push(CATEGORY_FILES);
		DEFAULT_BLUEPRINT_FOLDER = BUILDER.comment("Default blueprint folder used if no path specified in the commands.")
				.define("default_blueprint_folder", "/schematics");
		ADDITIONAL_BLUEPRINT_FOLDERS = BUILDER.comment("Additional blueprint paths that are aviable in the commands via their names.")
				.defineList("aditional_blueprint_folders", (List<String>) Arrays.asList("world:/saves/" + WORLD_FOLDER_KEY + "/generated/minecraft/structures", "worldedit:/config/worldedit/schematics", "global:/schematics"), s -> true);
		DEFAULT_BLUEPRINT_FORMAT = BUILDER.comment("The default format used by the save and load command.")
				.defineEnum("default_blueprint_format", BlueprintFormat.NBT);
		DEFAULT_IMAGE_FOLDER = BUILDER.comment("Default image folder used if no path specified in the commands.")
				.define("default_image_folder", "/images");
		ADDITIONAL_IMAGE_FOLDERS = BUILDER.comment("Additional image paths that are aviable in the commands via their names.")
				.defineList("aditional_image_folders", (List<String>) Arrays.asList("world:/saves/" + WORLD_FOLDER_KEY + "/generated/industria/images", "global:/images"), s -> true);
		BUILDER.pop();
		
		BUILDER.comment("Settings for the pixel art generator");
		BUILDER.push(CATEGORY_PIXELART);
		DEFAULT_BLOCK_WHITELIST = BUILDER.comment("Default list of blocks to prefer when creating an pixel art.")
				.defineList("default_block_whitelist", DEFAULT_WHITELIST, s -> BuiltInRegistries.BLOCK.containsKey(new ResourceLocation((String) s)));
		DEFAULT_BLOCK_BLACKLIST = BUILDER.comment("Default list of blocks to never use when creating an pixel art.")
				.defineList("default_block_blacklist", DEFAULT_BLACKLIST, s -> BuiltInRegistries.BLOCK.containsKey(new ResourceLocation((String) s)));
		BUILDER.pop();
		
		CONFIG = BUILDER.build();
	}
	
	public static boolean isBlockStatePropBlacklisted(String block, String property) {
		return BLOCK_STATE_PROP_BLACKLIST.get().contains(block + "." + property);
	}
	
	public static Map<String, String> getAdditionalBlueprintFolders() {
		return getAdditionalBlueprintFolders(HoloStructClient.getLocalLevelFolderName());
	}
	
	public static Map<String, String> getAdditionalBlueprintFolders(Optional<String> worldFolderName) {
		return ADDITIONAL_BLUEPRINT_FOLDERS.get().stream()
				.filter(e -> !e.contains(WORLD_FOLDER_KEY) || worldFolderName.isPresent())
				.map(e -> e.replace(WORLD_FOLDER_KEY, worldFolderName.orElseGet(() -> "n/a")))
				.map(e -> FOLDER_ENTRY_PATTERN.matcher(e))
				.filter(Matcher::find)
				.collect(Collectors.toMap(m -> m.group(1), m -> m.group(2)));
	}

	public static Map<String, String> getAdditionalImageFolders() {
		return getAdditionalImageFolders(HoloStructClient.getLocalLevelFolderName());
	}
	
	public static Map<String, String> getAdditionalImageFolders(Optional<String> worldFolderName) {
		return ADDITIONAL_IMAGE_FOLDERS.get().stream()
				.filter(e -> !e.contains(WORLD_FOLDER_KEY) || worldFolderName.isPresent())
				.map(e -> e.replace(WORLD_FOLDER_KEY, worldFolderName.orElseGet(() -> "n/a")))
				.map(e -> FOLDER_ENTRY_PATTERN.matcher(e))
				.filter(Matcher::find)
				.collect(Collectors.toMap(m -> m.group(1), m -> m.group(2)));
	}
	
	public static void register(ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, CONFIG);
	}
	
}
