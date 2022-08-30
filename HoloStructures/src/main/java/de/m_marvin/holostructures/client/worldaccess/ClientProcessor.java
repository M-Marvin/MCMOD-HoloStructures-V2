package de.m_marvin.holostructures.client.worldaccess;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.Formater;
import de.m_marvin.holostructures.commandargs.DirectionArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class ClientProcessor implements ITaskProcessor {
	
	public BlockPos selectionCorner1 = null;
	public BlockPos selectionCorner2 = null;
	
	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(commandSelectionBuild());
		dispatcher.register(commandBlueprintBuild());
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
		if (requiresBlueprintManager && HoloStructures.getInstance().getBlueprints().isWorking()) {
			Formater.build().translate("commands.unaviable.busyblueprintmanager").commandErrorStyle().send(source);
			return false;
		}
		return true;
	}
	
	/* Commands */
	
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
				);
	}
	
	public int commandCopy(CommandSourceStack source, boolean copyEntities, BlockPos origin) {
		if (checkRunnable(source, true, false, true)) {
			BlockPos copyOrigin = new BlockPos(Math.min(selectionCorner1.getX(), selectionCorner2.getX()), Math.min(selectionCorner1.getY(), selectionCorner2.getY()), Math.min(selectionCorner1.getZ(), selectionCorner2.getZ()));
			BlockPos blueprintOrigin = origin.subtract(copyOrigin);
			HoloStructures.getInstance().getBlueprints().copySelection(this.getAccessor().get(), this.selectionCorner1, this.selectionCorner2, copyEntities, () -> {
				HoloStructures.getInstance().getBlueprints().getClipboard().setOrigin(blueprintOrigin);
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
			boolean started = HoloStructures.getInstance().getBlueprints().pasteClipboard(getAccessor().get(), pasteOrigin, pasteEntities, () -> {
				if (!HoloStructures.getInstance().getBlueprints().getResult()) {
					Formater.build().translate("commands.blueprint.paste.incomplete").commandWarnStyle().send(source); // TODO 
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
		if (HoloStructures.getInstance().getBlueprints().isWorking()) {
			HoloStructures.getInstance().getBlueprints().abbortTask();
			Formater.build().translate("commands.blueprint.abborted").commandWarnStyle().send(source); // TODO
			return 1;
		}
		return 0;
	}
	
}
