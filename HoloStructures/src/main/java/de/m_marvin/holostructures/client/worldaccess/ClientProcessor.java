package de.m_marvin.holostructures.client.worldaccess;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.Config;
import de.m_marvin.holostructures.client.Formater;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.holograms.Corner;
import de.m_marvin.holostructures.client.holograms.Hologram;
import de.m_marvin.holostructures.commandargs.BlueprintPathArgument;
import de.m_marvin.holostructures.commandargs.DirectionArgumentType;
import de.m_marvin.holostructures.commandargs.SuggestingStringArgument;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraftforge.server.command.EnumArgument;

public class ClientProcessor implements ITaskProcessor {
	
	public BlockPos selectionCorner1 = null;
	public BlockPos selectionCorner2 = null;
	
	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(commandSelectionBuild());
		dispatcher.register(commandBlueprintBuild());
		dispatcher.register(commandInfoBuild());
		dispatcher.register(commandHologramBuild());
	}
	
	@Override
	public boolean canOperate() {
		return getAccessor().isPresent();
	}

	public boolean checkRunnable(CommandSourceStack source, boolean requiresSelection, boolean requiresOp, boolean requiresBlueprintManager) {
		if (!canOperate()) {
			Formater.build().translate("commands.unaviable.internalerror").commandErrorStyle().send(source);
			return false;
		}
		if (requiresOp && !hasOperator()) {
			Formater.build().translate("commands.unaviable.nopermissions").commandErrorStyle().send(source);
			return false;
		}
		if (requiresSelection && (this.selectionCorner1 == null || selectionCorner2 == null)) {
			Formater.build().translate("commands.unaviable.noselection").commandErrorStyle().send(source);
			return false;
		}
		if (requiresBlueprintManager && ClientHandler.getInstance().getBlueprints().isWorking()) {
			Formater.build().translate("commands.unaviable.busyblueprintmanager").commandErrorStyle().send(source);
			return false;
		}
		return true;
	}
	
	/* Commands */
	
	public LiteralArgumentBuilder<CommandSourceStack> commandInfoBuild() {
		return Commands.literal("holostructures")
			.then(Commands.literal("status").executes((ctx) -> 
				commandInfo(ctx.getSource())
				)
			);
	}
	
	public int commandInfo(CommandSourceStack source) {
		if (checkRunnable(source, false, false, false)) {
			Formater.build().translate("commands.info.title").commandInfoStyle().send(source);
			Formater.build().translate("commands.info.opaccess", this.getAccessor().get().hasWriteAccess()).commandInfoStyle().send(source);
			return 1;
		}
		return 0;
	}
	
	public LiteralArgumentBuilder<CommandSourceStack> commandSelectionBuild() {
		return Commands.literal("select")
			.then(Commands.literal("corner1").executes((ctx) -> 
				commandSelectionPos(ctx.getSource(), new BlockPos(ctx.getSource().getPosition()), 1)
				)
				.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((ctx) -> 
					commandSelectionPos(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 1)
					)
				)
			)
			.then(Commands.literal("corner2").executes(
				(ctx) -> commandSelectionPos(ctx.getSource(), new BlockPos(ctx.getSource().getPosition()), 2)
				)
				.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(
					(ctx) -> commandSelectionPos(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 2)
					)
				)
			)
			.then(Commands.literal("expand").executes(
				(ctx) -> commandExpand(ctx.getSource(), Direction.orderedByNearest(Minecraft.getInstance().player)[0], 1)
				)
				.then(Commands.argument("offset", IntegerArgumentType.integer()).executes(
					(ctx) -> commandExpand(ctx.getSource(), Direction.orderedByNearest(Minecraft.getInstance().player)[0], IntegerArgumentType.getInteger(ctx, "offset"))
					)
					.then(Commands.argument("direction", DirectionArgumentType.directions()).executes(
						(ctx) -> commandExpand(ctx.getSource(), DirectionArgumentType.getDirection(ctx, "direction"), IntegerArgumentType.getInteger(ctx, "offset"))
						)
					)
				)
			)
			.then(Commands.literal("clear").executes(
				(ctx) -> commandDeselect(ctx.getSource()
				)
			)
		);
	}
	
	public int commandSelectionPos(CommandSourceStack source, BlockPos pos, int selectionId) {
		if (checkRunnable(source, false, false, false)) {
			if (selectionId == 1) this.selectionCorner1 = pos;
			if (selectionId == 2) this.selectionCorner2 = pos;
			Formater.build().translate("commands.selection.corners." + (selectionId == 1 ? "first" : "second")).commandInfoStyle().send(source);
			return 1;
		}
		return 0;
	}
	
	public int commandDeselect(CommandSourceStack source) {
		if (checkRunnable(source, false, false, false)) {
			this.selectionCorner1 = null;
			this.selectionCorner2 = null;
			Formater.build().translate("commands.selection.clear").commandInfoStyle().send(source);
			return 1;
		}
		return 0;
	}
	
	public int commandExpand(CommandSourceStack source, Direction direction, int ammount) {
		if (checkRunnable(source, true, false, false)) {
			if (ammount < 0 && -ammount >= Math.abs(this.selectionCorner1.get(direction.getAxis()) - this.selectionCorner2.get(direction.getAxis()))) {
				Formater.build().translate("commands.selection.expand.invalidshrink").commandErrorStyle().send(source);
				return 0;
			}
			if ((direction.getAxisDirection() == AxisDirection.POSITIVE) == (this.selectionCorner1.get(direction.getAxis()) > this.selectionCorner2.get(direction.getAxis()))) {
				this.selectionCorner1 = this.selectionCorner1.relative(direction, ammount);
			} else {
				this.selectionCorner2 = this.selectionCorner2.relative(direction, ammount);
			}
			if (ammount > 0) {
				Formater.build().translate("commands.selection.expand.grow", ammount, direction.getName()).commandInfoStyle().send(source);
			} else {
				Formater.build().translate("commands.selection.expand.shrink", -ammount, direction.getName()).commandInfoStyle().send(source);
			}
			return 1;
		}
		return 0;
	}
	
	public LiteralArgumentBuilder<CommandSourceStack> commandBlueprintBuild() {
		return Commands.literal("blueprint")
				.then(Commands.literal("copy").executes((ctx) -> 
						commandCopy(ctx.getSource(), false, Minecraft.getInstance().player.blockPosition())
						)
						.then(Commands.argument("copyEntities", BoolArgumentType.bool()).executes((ctx) -> 
								commandCopy(ctx.getSource(), BoolArgumentType.getBool(ctx, "copyEntities"), Minecraft.getInstance().player.blockPosition())
								)
								.then(Commands.argument("origin", BlockPosArgument.blockPos()).executes((ctx) ->
										commandCopy(ctx.getSource(), BoolArgumentType.getBool(ctx, "copyEntities"), BlockPosArgument.getLoadedBlockPos(ctx, "origin"))
										)
								)
						)
				)
				.then(Commands.literal("paste").executes((ctx) ->
						commandPaste(ctx.getSource(), false, Minecraft.getInstance().player.blockPosition())
						)
						.then(Commands.argument("pasteEntities", BoolArgumentType.bool()).executes((ctx) ->
								commandPaste(ctx.getSource(), BoolArgumentType.getBool(ctx, "pasteEntities"), Minecraft.getInstance().player.blockPosition())
								)
								.then(Commands.argument("origin", BlockPosArgument.blockPos()).executes((ctx) ->
										commandPaste(ctx.getSource(), BoolArgumentType.getBool(ctx, "pasteEntities"), BlockPosArgument.getLoadedBlockPos(ctx, "origin"))
										)
								)
						)
				)
				.then(Commands.literal("abbort").executes((ctx) ->
						commandAbbort(ctx.getSource())
						)
				)
				.then(Commands.literal("save")
						.then(Commands.argument("path", BlueprintPathArgument.save()).executes((ctx) -> 
								commandSave(ctx.getSource(), BlueprintPathArgument.getPath(ctx, "path"), false)
								)
								.then(Commands.literal("override").executes((ctx) -> 
										commandSave(ctx.getSource(), BlueprintPathArgument.getPath(ctx, "path"), true)
										)
								)
						)
				)
				.then(Commands.literal("load")
						.then(Commands.argument("path", BlueprintPathArgument.loadOnlyExisting()).executes((ctx) ->
								commandLoad(ctx.getSource(), BlueprintPathArgument.getPath(ctx, "path"))
								)
						)
				)
				.then(Commands.literal("folder").executes((ctx) -> 
						commandFolder(ctx.getSource(), Config.getDefaultFolder())
						)
						.then(Commands.argument("folder", BlueprintPathArgument.save()).executes((ctx) -> 
								commandFolder(ctx.getSource(), BlueprintPathArgument.getPath(ctx, "folder"))
								)
						)
				);
	}
	
	@SuppressWarnings("deprecation")
	public int commandFolder(CommandSourceStack source, String folder) {
		if (checkRunnable(source, false, false, false)) {
			File folderPath = UtilHelper.resolvePath(folder);
			if (folderPath.isFile()) folderPath = folderPath.getParentFile();
			try {
				Runtime.getRuntime().exec("explorer " + folderPath.toString());
			} catch (IOException e) {
				Formater.build().translate("commands.blueprint.folder.ioexception").commandErrorStyle().send(source);
				return 0;
			}
			return 1;
		}
		return 0;
	}
	
	public int commandSave(CommandSourceStack source, String path, boolean overrideExisting) throws CommandSyntaxException {
		if (checkRunnable(source, false, false, true)) {
			if (ClientHandler.getInstance().getBlueprints().getClipboard() == null)  {
				Formater.build().translate("commands.blueprint.save.noclipboard").commandErrorStyle().send(source);
				return 0;
			}
			boolean success;
			success = ClientHandler.getInstance().getBlueprints().saveClipboard(path, overrideExisting);
			if (!success) {
				Formater.build().translate("commands.blueprint.save.failed", path).commandErrorStyle().send(source);
				return 0;
			} else {
				Formater.build().translate("commands.blueprint.save.success", path).commandInfoStyle().send(source);
				return 1;
			}
		}
		return 0;
	}
	
	public int commandLoad(CommandSourceStack source, String path) throws CommandSyntaxException {
		if (checkRunnable(source, false, false, false)) {
			boolean success = ClientHandler.getInstance().getBlueprints().loadClipboard(path);
			if (!success) {
				Formater.build().translate("commands.blueprint.load.failed", path).commandErrorStyle().send(source);
				return 0;
			} else {
				Formater.build().translate("commands.blueprint.load.success", path).commandInfoStyle().send(source);
				return 1;
			}
		}
		return 0;
	}
	
	public int commandCopy(CommandSourceStack source, boolean copyEntities, BlockPos origin) {
		if (checkRunnable(source, true, false, true)) {
			BlockPos copyOrigin = new BlockPos(Math.min(selectionCorner1.getX(), selectionCorner2.getX()), Math.min(selectionCorner1.getY(), selectionCorner2.getY()), Math.min(selectionCorner1.getZ(), selectionCorner2.getZ()));
			BlockPos blueprintOrigin = origin.subtract(copyOrigin);
			ClientHandler.getInstance().getBlueprints().copySelection(this.getAccessor().get(), this.selectionCorner1, this.selectionCorner2, copyEntities, () -> {
				ClientHandler.getInstance().getBlueprints().getClipboard().setOrigin(blueprintOrigin);
				Formater.build().translate("commands.blueprint.copy.completed").commandInfoStyle().send(source);
			});
			Formater.build().translate("commands.blueprint.copy.started").commandInfoStyle().send(source);
			return 1;
		}
		Formater.build().translate("commands.blueprint.copy.unable").commandErrorStyle().send(source);
		return 0;
	}
	
	public int commandPaste(CommandSourceStack source, boolean pasteEntities, BlockPos pasteOrigin) {
		if (checkRunnable(source, false, true, true)) {
			boolean started = ClientHandler.getInstance().getBlueprints().pasteClipboard(getAccessor().get(), pasteOrigin, pasteEntities, () -> {
				if (!ClientHandler.getInstance().getBlueprints().getResult()) {
					Formater.build().translate("commands.blueprint.paste.incomplete").commandWarnStyle().send(source);
				} else {
					Formater.build().translate("commands.blueprint.paste.completed").commandInfoStyle().send(source);
				}
			});
			if (started) {
				Formater.build().translate("commands.blueprint.paste.started").commandInfoStyle().send(source);
				return 1;
			} else {
				Formater.build().translate("commands.blueprint.paste.failed").commandErrorStyle().send(source);
				return 0;
			}
		}
		Formater.build().translate("commands.blueprint.paste.unable").commandErrorStyle().send(source);
		return 0;
	}
	
	public int commandAbbort(CommandSourceStack source) {
		if (checkRunnable(source, false, false, false) && ClientHandler.getInstance().getBlueprints().isWorking()) {
			ClientHandler.getInstance().getBlueprints().abbortTask();
			getAccessor().get().abbortAccessing();
			Formater.build().translate("commands.blueprint.abborted").commandWarnStyle().send(source);
			return 1;
		}
		return 0;
	}
	
	public LiteralArgumentBuilder<CommandSourceStack> commandHologramBuild() {
		return Commands.literal("hologram")
				.then(Commands.literal("create")
						.then(Commands.literal("empty")
								.then(Commands.argument("name", StringArgumentType.word()).executes((ctx) ->
										commandCreateEmpty(ctx.getSource(), StringArgumentType.getString(ctx, "name"), Minecraft.getInstance().player.blockPosition())
										)
										.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((ctx) ->
												commandCreateEmpty(ctx.getSource(), StringArgumentType.getString(ctx, "name"), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))
												)
												.then(Commands.argument("position", BlockPosArgument.blockPos()).executes((ctx) -> 
														commandCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name"), BoolArgumentType.getBool(ctx, "includeEntities"), BlockPosArgument.getLoadedBlockPos(ctx, "position"))
														)
												)
										)
								)
						)
						.then(Commands.literal("blueprint")
								.then(Commands.argument("name", StringArgumentType.word()).executes((ctx) ->
										commandCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name"), false, Minecraft.getInstance().player.blockPosition())
										)
										.then(Commands.argument("includeEntities", BoolArgumentType.bool()).executes((ctx) -> 
												commandCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name"), BoolArgumentType.getBool(ctx, "includeEntities"), Minecraft.getInstance().player.blockPosition())
												)
												.then(Commands.argument("position", BlockPosArgument.blockPos()).executes((ctx) -> 
														commandCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name"), BoolArgumentType.getBool(ctx, "includeEntities"), BlockPosArgument.getLoadedBlockPos(ctx, "position"))
														)
												)
										)
								)
						)
				)
				.then(Commands.literal("remove")
						.then(Commands.argument("name", SuggestingStringArgument.wordSuggesting(() -> ClientHandler.getInstance().getHolograms().getHologramNames())).executes((ctx) ->
								commandRemove(ctx.getSource(), SuggestingStringArgument.getString(ctx, "name"))
								)
						)
				)
				.then(Commands.literal("position")
						.then(Commands.argument("name", SuggestingStringArgument.wordSuggesting(() -> ClientHandler.getInstance().getHolograms().getHologramNames())).executes((ctx) -> 
								commandPosition(ctx.getSource(), SuggestingStringArgument.getString(ctx, "name"), Minecraft.getInstance().player.blockPosition(), Corner.origin)
								)
								.then(Commands.argument("position", BlockPosArgument.blockPos()).executes((ctx) -> 
										commandPosition(ctx.getSource(), SuggestingStringArgument.getString(ctx, "name"), BlockPosArgument.getLoadedBlockPos(ctx, "position"), Corner.origin)
										)
										.then(Commands.argument("corner", EnumArgument.enumArgument(Corner.class)).executes((ctx) -> 
												commandPosition(ctx.getSource(), SuggestingStringArgument.getString(ctx, "name"), BlockPosArgument.getLoadedBlockPos(ctx, "position"), ctx.getArgument("corner", Corner.class))
												)
										)
								)
						)
				)
				.then(Commands.literal("list").executes((ctx) ->
						commandList(ctx.getSource())
						)
				);
	}
	
	public int commandRemove(CommandSourceStack source, String name) {
		if (checkRunnable(source, false, false, false)) {
			if (ClientHandler.getInstance().getHolograms().removeHologram(name)) {
				Formater.build().translate("commands.hologram.remove.success", name).commandInfoStyle().send(source);
				return 1;
			} else {
				Formater.build().translate("commands.hologram.remove.invalidhologram", name).commandErrorStyle().send(source);
				return 0;
			}
		}
		return 0;
	}
	
	public int commandCreateEmpty(CommandSourceStack source, String name, BlockPos position) {
		if (checkRunnable(source, false, false, false)) {
			Hologram hologram = ClientHandler.getInstance().getHolograms().createHologram(null, position, name, false);
			if (hologram != null) {
				Formater.build().translate("commands.hologram.create.empty.success", name).commandInfoStyle().send(source);
				return 1;
			} else {
				Formater.build().translate("commands.hologram.create.empty.doubledname", name).commandErrorStyle().send(source);
				return 0;
			}
		}
		return 0;
	}
	
	public int commandCreate(CommandSourceStack source, String name, boolean includeEntities, BlockPos position) {
		if (checkRunnable(source, false, false, false)) {
			Blueprint blueprint = ClientHandler.getInstance().getBlueprints().getClipboard();
			if (blueprint == null) {
				Formater.build().translate("commands.hologram.create.blueprint.noclipboard").commandErrorStyle().send(source);
				return 0;
			}
			Hologram hologram = ClientHandler.getInstance().getHolograms().createHologram(blueprint, position, name, includeEntities);
			if (hologram != null) {
				Formater.build().translate("commands.hologram.create.blueprint.success", name).commandInfoStyle().send(source);
				return 1;
			} else {
				Formater.build().translate("commands.hologram.create.blueprint.doublename", name).commandErrorStyle().send(source);
				return 0;
			}
		}
		return 0;
	}
	
	public int commandList(CommandSourceStack source) {
		if (checkRunnable(source, false, false, false)) {
			Collection<Hologram> holograms = ClientHandler.getInstance().getHolograms().getHolograms();
			if (holograms.size() > 0) {
				Formater.build().translate("commands.hologram.list.title").commandInfoStyle().send(source);
				boolean line = false;
				for (Hologram hologram : holograms) {
					line = !line;
					Formater.build().translate("commands.hologram.list.entry", hologram.getName(), UtilHelper.formatBlockPos(hologram.getPosition()), UtilHelper.formatAABB(hologram.getBoundingBox())).withStyle(line ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY).send(source);
				}
				return 1;
			} else {
				Formater.build().translate("commands.hologram.list.noholograms").commandInfoStyle().send(source);
				return 1;
			}
		}
		return 0;
	}
	
	public int commandPosition(CommandSourceStack source, String name, BlockPos position, Corner corner) {
		if (checkRunnable(source, false, false, false)) {
			Hologram hologram = ClientHandler.getInstance().getHolograms().getHologram(name);
			if (hologram == null) {
				Formater.build().translate("commands.hologram.position.invalidhologram", name).commandErrorStyle().send(source);
				return 0;
			}
			hologram.setCornerWorldPosition(corner, position);
			Formater.build().translate("commands.hologram.position.success", name, UtilHelper.formatBlockPos(position)).commandInfoStyle().send(source);
			return 1;
		}
		return 0;
	}
	
}
