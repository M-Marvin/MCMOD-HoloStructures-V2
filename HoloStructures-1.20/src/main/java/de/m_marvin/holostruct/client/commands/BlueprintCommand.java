package de.m_marvin.holostruct.client.commands;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.blueprints.BlueprintLoader;
import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.ClientConfig;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintArgument;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintFormatArgument;
import de.m_marvin.holostruct.client.commands.arguments.FilePathArgument;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Command used to manage blueprints and their files
 * @author Marvin Koehler
 */
public class BlueprintCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("blueprint")
		.then(
				Commands.literal("load")
				.then(
						Commands.argument("file", FilePathArgument.loadBlueprint())
						.then(
								Commands.argument("name", BlueprintArgument.blueprint())
								.executes(source ->
										loadBlueprint(source, FilePathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "name"))
								)
						)
				)
		).then(
				Commands.literal("save")
				.then(
						Commands.argument("blueprint", BlueprintArgument.blueprint())
						.then(
								Commands.argument("file", FilePathArgument.saveBlueprint())
								.executes(source ->
										saveBlueprint(source, FilePathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), null, false)
								)
								.then(
										Commands.literal("override")
										.executes(source ->
												saveBlueprint(source, FilePathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), null, true)
										)
								)
								.then(
										Commands.argument("format", BlueprintFormatArgument.format())
										.executes(source ->
												saveBlueprint(source, FilePathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), BlueprintFormatArgument.getFormat(source, "format"), false)
										)
										.then(
												Commands.literal("override")
												.executes(source ->
														saveBlueprint(source, FilePathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), BlueprintFormatArgument.getFormat(source, "format"), true)
												)
										)
								)
						)
				)
				).then(
						Commands.literal("discard")
						.then(
								Commands.argument("blueprint", BlueprintArgument.blueprint())
								.executes(source ->
										discardBlueprint(source, BlueprintArgument.getBlueprint(source, "blueprint"))
								)
						)
				).then(
						Commands.literal("folder")
						.executes(source -> 
							openFolder(source, ClientConfig.DEFAULT_BLUEPRINT_FOLDER.get())
						)
						.then(
								Commands.argument("folder", FilePathArgument.saveBlueprint())
								.executes(source -> 
									openFolder(source, FilePathArgument.getPath(source, "folder")
								)
						)
				)
		)
		.then(
				Commands.literal("show_logs")
				.then(
						Commands.argument("blueprint", BlueprintArgument.blueprint())
						.executes(source ->
								showParsingLogs(source, BlueprintArgument.getBlueprint(source, "blueprint"))
						)
				)
		));
	}
	
	public static int showParsingLogs(CommandContext<CommandSourceStack> source, String blueprintName) {
		Blueprint blueprint = HoloStruct.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.logs.invalidblueprint", blueprintName));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.logs.listhead"), false);
		if (blueprint.getParsingErrors().size() > 0) {
			for (String logEnty : blueprint.getParsingErrors()) {
				source.getSource().sendSuccess(() -> Component.literal(String.format("- %s", logEnty)), false);
			}
		} else {
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.logs.noentry"), false);
		}
		return 1;
	}
	
	public static int openFolder(CommandContext<CommandSourceStack> source, String folder) {
		File folderPath = FilePathArgument.resolvePath(folder);
		if (folderPath.isFile()) folderPath = folderPath.getParentFile();
		Util.getPlatform().openFile(folderPath);
		return 1;
	}
	
	public static int discardBlueprint(CommandContext<CommandSourceStack> source, String blueprintName) {
		Blueprint blueprint = HoloStruct.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.discard.invalidblueprint", blueprintName));
			return 0;
		}
		
		HoloStruct.CLIENT.BLUEPRINTS.unloadBlueprint(blueprintName);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.discard.discarded", blueprintName), false);
		return 1;
	}
	
	public static int saveBlueprint(CommandContext<CommandSourceStack> source, String blueprintPath, String blueprintName, BlueprintFormat format, boolean overrideExisting) {
		File blueprintFile = FilePathArgument.resolvePath(blueprintPath);
		if (blueprintFile.exists() && !overrideExisting) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.fileexists", blueprintPath));
			return 0;
		}
		
		Blueprint blueprint = HoloStruct.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.invalidblueprint", blueprintName));
			return 0;
		}

		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.save.started", blueprintName), false);
		
		CompletableFuture.runAsync(() -> {
			if (!BlueprintLoader.saveBlueprint(blueprint, blueprintFile, format)) {
				source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.failed", blueprintPath));
				return;
			}
			
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.save.saved", blueprintPath), false);
			if (!blueprint.getParsingErrors().isEmpty()) {
				source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.save.parsewarning"), false);
			}
		});
		return 1;
	}
	
	public static int loadBlueprint(CommandContext<CommandSourceStack> source, String blueprintPath, String blueprintName) {
		File blueprintFile = FilePathArgument.resolvePath(blueprintPath);
		if (!blueprintFile.exists()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.load.nofile", blueprintPath));
			return 0;
		}

		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.load.started", blueprintName), false);
		
		CompletableFuture.runAsync(() -> {
			Blueprint blueprint = BlueprintLoader.loadBlueprint(blueprintFile);
			if (blueprint == null) {
				source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.load.invalidfile", blueprintPath));
				return;
			}
			
			HoloStruct.CLIENT.BLUEPRINTS.setLoadedBlueprint(blueprintName, blueprint);
			
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.load.loaded", blueprintName), false);
			if (!blueprint.getParsingErrors().isEmpty()) {
				source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.load.parsewarning"), false);
			}
		});

		return 1;
	}
	
}
