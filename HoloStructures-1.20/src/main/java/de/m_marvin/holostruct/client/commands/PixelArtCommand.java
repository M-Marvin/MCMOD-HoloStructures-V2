package de.m_marvin.holostruct.client.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.ClientConfig;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintArgument;
import de.m_marvin.holostruct.client.commands.arguments.FilePathArgument;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor.Brightness;

public class PixelArtCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("pixelart")
		.then(
				Commands.literal("image")
				.then(
						Commands.argument("image", FilePathArgument.loadImage())
						.executes(source ->
								loadImage(source, FilePathArgument.getPath(source, "image"))
						)
				)
		)
		.then(
				Commands.literal("export")
				.then(
						Commands.argument("image", FilePathArgument.saveImage())
						.executes(source ->
								saveImage(source, FilePathArgument.getPath(source, "image"), false)
						).then(
								Commands.literal("override")
								.executes(source ->
										saveImage(source, FilePathArgument.getPath(source, "image"), true)
								)
						)
				)
		).then(
				Commands.literal("folder")
				.executes(source -> 
						openFolder(source, ClientConfig.DEFAULT_IMAGE_FOLDER.get())
				)
				.then(
						Commands.argument("folder", FilePathArgument.loadImage())
						.executes(source -> 
								openFolder(source, FilePathArgument.getPath(source, "folder"))
						)
				)
		).then(
				Commands.literal("blocklist")
				.executes(source ->
						listBlocks(source)
				)
		).then(
				Commands.literal("blacklist")
				.executes(source ->
						listBlacklist(source)
				).then(
						Commands.literal("add")
						.then(
								Commands.argument("block", BlockStateArgument.block(context))
								.executes(source ->
										addBlacklist(source, BlockStateArgument.getBlock(source, "block").getState().getBlock())
								)
						)
				).then(
						Commands.literal("remove")
						.then(
								Commands.argument("block", BlockStateArgument.block(context))
								.executes(source ->
										removeBlacklist(source, BlockStateArgument.getBlock(source, "block").getState().getBlock())
								)
						)
				)
		).then(
				Commands.literal("whitelist")
				.executes(source ->
						listWhitelist(source)
				).then(
						Commands.literal("add")
						.then(
								Commands.argument("block", BlockStateArgument.block(context))
								.executes(source ->
										addWhitelist(source, BlockStateArgument.getBlock(source, "block").getState().getBlock())
								)
						)
				).then(
						Commands.literal("remove")
						.then(
								Commands.argument("block", BlockStateArgument.block(context))
								.executes(source ->
										removeWhitelist(source, BlockStateArgument.getBlock(source, "block").getState().getBlock())
								)
						)
				)
		).then(
				Commands.literal("preview")
				.executes(source -> 
						updatePreview(source, false)
				).then(
						Commands.literal("hide")
						.executes(source ->
								updatePreview(source, true)
						)
				)
		).then(
				Commands.literal("build")
				.then(
						Commands.argument("blueprint", BlueprintArgument.blueprint())
						.executes(source -> 
								buildBlueprint(source, BlueprintArgument.getBlueprint(source, "blueprint"))
						)
				)
		));
	}
	
	// TODO better command usage
	
	public static int buildBlueprint(CommandContext<CommandSourceStack> source, String blueprintName) {
		
		Blueprint blueprint = HoloStruct.CLIENT.PIXELART_GENERATOR.buildBlueprint();
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.build.failed"));
			return 0;
		}
		
		HoloStruct.CLIENT.BLUEPRINTS.setLoadedBlueprint(blueprintName, blueprint);
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.build.saved", blueprintName), false);
		return 1;
		
	}
	
	public static int updatePreview(CommandContext<CommandSourceStack> source, boolean hide) {
		
		if (hide) {
			HoloStruct.CLIENT.PIXELART_GENERATOR.hidePreview();
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.preview.hide"), false);
		} else {
			HoloStruct.CLIENT.PIXELART_GENERATOR.updatePreview();
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.preview.update"), false);
		}
		return 1;
	}
	
	public static int listBlocks(CommandContext<CommandSourceStack> source) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getBlockList().isEmpty()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.blocklist.empty"));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.blocklist.head"), false);
		for (Entry<Block, Integer> block : HoloStruct.CLIENT.PIXELART_GENERATOR.getBlockList().entrySet()) {
			source.getSource().sendSuccess(() -> Component.literal("- ").append(block.getKey().getName()).append(Component.literal(" x " + block.getValue())), false);
		}
		
		File f = new File("C:\\Users\\marvi\\Desktop\\dump.txt");
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			
			Map<Integer, Set<Block>> items = new HashMap<>();
			HoloStruct.CLIENT.PIXELART_GENERATOR.colorMap.forEach((rgb, config) -> {
				if (config.brightness() != Brightness.HIGH) return;
				if (!items.containsKey(rgb)) items.put(rgb, new HashSet<>());
				items.get(rgb).add(config.state().getBlock());
			});
			
			items.forEach((rgb, blocks) -> {
				try {
					int red = rgb >> 16 & 0xFF;
					int green = rgb >> 8 & 0xFF;
					int blue = rgb & 0xFF;
					
					writer.write(red + " " + green + " " + blue + "\t");
					for (Block s : blocks) {
						writer.write(BuiltInRegistries.BLOCK.getKey(s).toString() + "\t");
					}
					
					writer.write("\n");
					
				} catch (Throwable e) {}
			});
			
			writer.close();
		} catch (Throwable e) {}
		
		return 1;
	}

	public static int listWhitelist(CommandContext<CommandSourceStack> source) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getWhitelist().isEmpty()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.whitelist.empty"));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.whitelist.head"), false);
		for (Block block : HoloStruct.CLIENT.PIXELART_GENERATOR.getWhitelist()) {
			source.getSource().sendSuccess(() -> Component.literal("- ").append(block.getName()), false);
		}
		
		return 1;
	}
	
	public static int addWhitelist(CommandContext<CommandSourceStack> source, Block block) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getWhitelist().add(block))
			HoloStruct.CLIENT.PIXELART_GENERATOR.rebuild();
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.whitelist.added", block.getName()), false);
		return 1;
		
	}
	
	public static int removeWhitelist(CommandContext<CommandSourceStack> source, Block block) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getWhitelist().remove(block))
			HoloStruct.CLIENT.PIXELART_GENERATOR.rebuild();
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.whitelist.removed", block.getName()), false);
		return 1;
		
	}
	
	public static int listBlacklist(CommandContext<CommandSourceStack> source) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getBlacklist().isEmpty()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.blacklist.empty"));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.blacklist.head"), false);
		for (Block block : HoloStruct.CLIENT.PIXELART_GENERATOR.getBlacklist()) {
			source.getSource().sendSuccess(() -> Component.literal("- ").append(block.getName()), false);
		}
		
		return 1;
	}
	
	public static int addBlacklist(CommandContext<CommandSourceStack> source, Block block) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getBlacklist().add(block))
			HoloStruct.CLIENT.PIXELART_GENERATOR.rebuild();
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.blacklist.added", block.getName()), false);
		return 1;
		
	}
	
	public static int removeBlacklist(CommandContext<CommandSourceStack> source, Block block) {
		
		if (HoloStruct.CLIENT.PIXELART_GENERATOR.getBlacklist().remove(block))
			HoloStruct.CLIENT.PIXELART_GENERATOR.rebuild();
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.blacklist.removed", block.getName()), false);
		return 1;
		
	}
	
	public static int openFolder(CommandContext<CommandSourceStack> source, String folder) {
		File folderPath = FilePathArgument.resolvePath(folder);
		System.out.println(folder + " -> " + folderPath);
		if (folderPath.isFile()) folderPath = folderPath.getParentFile();
		Util.getPlatform().openFile(folderPath);
		return 1;
	}
	
	public static int loadImage(CommandContext<CommandSourceStack> source, String imagePath) {
		
		File imageFile = FilePathArgument.resolvePath(imagePath);
		if (!imageFile.exists()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.image.nofile", imagePath));
			return 0;
		}
		
		if (!HoloStruct.CLIENT.PIXELART_GENERATOR.loadImage(imageFile)) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.image.failed", imagePath));
			return 0;
		}

		HoloStruct.CLIENT.PIXELART_GENERATOR.rebuild();
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.image.loaded", imagePath), false);
		return 1;
	}
	
	public static int saveImage(CommandContext<CommandSourceStack> source, String imagePath, boolean overrideExisting) {
		
		File imageFile = FilePathArgument.resolvePath(imagePath);
		if (imageFile.exists() && !overrideExisting) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.export.fileexists", imagePath));
			return 0;
		}
		
		if (!HoloStruct.CLIENT.PIXELART_GENERATOR.saveImage(imageFile)) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.pixelart.export.failed", imagePath));
			return 0;
		}

		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.pixelart.export.saved", imagePath), false);
		return 1;
	}
	
}
