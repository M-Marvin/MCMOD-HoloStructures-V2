package de.m_marvin.holostructures.client.commands;

import java.io.File;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.blueprints.BlueprintLoader;
import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.Config;
import de.m_marvin.holostructures.client.commands.arguments.BlueprintArgument;
import de.m_marvin.holostructures.client.commands.arguments.BlueprintFormatArgument;
import de.m_marvin.holostructures.client.commands.arguments.BlueprintPathArgument;
import de.m_marvin.holostructures.utility.UtilHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class BlueprintCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("blueprint")
		.then(
				Commands.literal("load")
				.then(
						Commands.argument("file", BlueprintPathArgument.loadOnlyExisting())
						.then(
								Commands.argument("name", BlueprintArgument.blueprint())
								.executes(source ->
										loadBlueprint(source, BlueprintPathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "name"))
								)
						)
				)
		).then(
				Commands.literal("save")
				.then(
						Commands.argument("blueprint", BlueprintArgument.blueprint())
						.then(
								Commands.argument("file", BlueprintPathArgument.save())
								.then(
										Commands.argument("format", BlueprintFormatArgument.format())
										.executes(source ->
												saveBlueprint(source, BlueprintPathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), BlueprintFormatArgument.getFormat(source, "format"), false)
										)
										.then(
												Commands.literal("override")
												.executes(source ->
														saveBlueprint(source, BlueprintPathArgument.getPath(source, "file"), BlueprintArgument.getBlueprint(source, "blueprint"), BlueprintFormatArgument.getFormat(source, "format"), true)
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
							openFolder(source, Config.getDefaultFolder())
						)
						.then(
								Commands.argument("folder", BlueprintPathArgument.save())
								.executes(source -> 
									openFolder(source, BlueprintPathArgument.getPath(source, "folder")
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
		Blueprint blueprint = HoloStructures.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.logs.invalidblueprint", blueprintName));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.logs.listhead"), false);
		for (String logEnty : blueprint.getParsingErrors()) {
			source.getSource().sendSuccess(() -> Component.literal(String.format("- %s", logEnty)), false);
		}
		return 1;
	}
	
	public static int openFolder(CommandContext<CommandSourceStack> source, String folder) {
		File folderPath = UtilHelper.resolvePath(folder);
		if (folderPath.isFile()) folderPath = folderPath.getParentFile();
		try {
			Runtime.getRuntime().exec(new String[] {"explorer " + folderPath.toString()}); // TODO not working
		} catch (IOException e) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.folder.ioexception"));
			return 0;
		}
		return 1;
	}
	
	public static int discardBlueprint(CommandContext<CommandSourceStack> source, String blueprintName) {
		Blueprint blueprint = HoloStructures.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.discard.invalidblueprint", blueprintName));
			return 0;
		}
		
		HoloStructures.CLIENT.BLUEPRINTS.unloadBlueprint(blueprintName);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.discard.discarded", blueprintName), false);
		return 1;
	}
	
	public static int saveBlueprint(CommandContext<CommandSourceStack> source, String blueprintPath, String blueprintName, BlueprintFormat format, boolean overrideExisting) {
		File blueprintFile = UtilHelper.resolvePath(blueprintPath);
		if (blueprintFile.exists() && !overrideExisting) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.fileexists", blueprintPath));
			return 0;
		}
		
		Blueprint blueprint = HoloStructures.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.invalidblueprint", blueprintName));
			return 0;
		}
		
		if (!BlueprintLoader.saveBlueprint(blueprint, blueprintFile, format)) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.save.failed", blueprintPath));
			return 0;
		}
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.save.saved", blueprintName), false);
		return 1;
	}
	
	public static int loadBlueprint(CommandContext<CommandSourceStack> source, String blueprintPath, String blueprintName) {
		File blueprintFile = UtilHelper.resolvePath(blueprintPath);
		if (!blueprintFile.exists()) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.load.nofile", blueprintPath));
			return 0;
		}
		
		Blueprint blueprint = BlueprintLoader.loadBlueprint(blueprintFile);
		if (blueprint == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.blueprint.load.invalidfile", blueprintPath));
			return 0;
		}
		
		HoloStructures.CLIENT.BLUEPRINTS.setLoadedBlueprint(blueprintName, blueprint);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.blueprint.load.loaded", blueprintName), false);
		return 1;
	}
	
}