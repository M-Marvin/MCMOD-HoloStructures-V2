package de.m_marvin.holostruct.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintArgument;
import de.m_marvin.holostruct.client.commands.arguments.HologramArgument;
import de.m_marvin.holostruct.client.commands.arguments.ViewMode;
import de.m_marvin.holostruct.client.holograms.Hologram;
import de.m_marvin.holostruct.utility.UtilHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Command used to manage holograms
 * @author Marvin Koehler
 */
public class HologramCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hologram")
		.then(
				Commands.literal("create")
				.then(
						Commands.argument("hologram", HologramArgument.hologram())
						.then(
								Commands.literal("blueprint")
								.then(
										Commands.argument("blueprint", BlueprintArgument.blueprint())
										.executes(source ->
												createHologram(source, HologramArgument.getHologram(source, "hologram"), BlueprintArgument.getBlueprint(source, "blueprint"))
										)
								)	
						)
						.then(
								Commands.literal("empty")
								.executes(source ->
										createHologram(source, HologramArgument.getHologram(source, "hologram"), null)
								)
						)
				)
		)
		.then(
				Commands.literal("blueprint")
				.then(
						Commands.argument("hologram", HologramArgument.hologram())
						.then(
								Commands.argument("blueprint", BlueprintArgument.blueprint())
								.executes(source ->
										exportBlueprint(source, HologramArgument.getHologram(source, "hologram"), BlueprintArgument.getBlueprint(source, "blueprint"))
								)
						)
				)
		)
		.then(
				Commands.literal("remove")
				.then(
						Commands.argument("hologram", HologramArgument.hologram())
						.executes(source ->
								removeHologram(source, HologramArgument.getHologram(source, "hologram"))
						)	
				)
		)
		.then(
				Commands.literal("origin")
				.then(
						Commands.argument("hologram", HologramArgument.hologram())
						.then(
								Commands.argument("position", BlockPosArgument.blockPos())
								.executes(source ->
										changeOrigin(source, HologramArgument.getHologram(source, "hologram"), BlockPosArgument.getBlockPos(source, "position"))
								)
						)
				)
		)
		.then(
				Commands.literal("position")
				.then(
						Commands.argument("hologram", HologramArgument.hologram())
						.then(
								Commands.argument("position", BlockPosArgument.blockPos())
								.executes(source ->
										changePosition(source, HologramArgument.getHologram(source, "hologram"), BlockPosArgument.getBlockPos(source, "position"))
								)
						)
				)
		)
		.then(
				Commands.literal("layermode")
				.then(
						Commands.literal("enable")
						.executes(source ->
								layerMode(source, true, (int) Math.floor(source.getSource().getPosition().y))
						)
						.then(
								Commands.argument("layer", IntegerArgumentType.integer())
								.executes(source -> 
										layerMode(source, true, IntegerArgumentType.getInteger(source, "layer"))
								)
						)
				)
				.then(
						Commands.literal("disable")
						.executes(source ->
								layerMode(source, false, 0)
						)
				)
		)
		.then(
				Commands.literal("viewmode")
				.then(
						Commands.literal("vanilla")
						.executes(source -> 
								selectViewMode(source, ViewMode.VANILLA)
						)
				)
				.then(
						Commands.literal("base")
						.executes(source -> 
								selectViewMode(source, ViewMode.BASE)
						)
				)
				.then(
						Commands.literal("scanline")
						.executes(source -> 
								selectViewMode(source, ViewMode.SCANLINE)
						)
				)
				.then(
						Commands.literal("creeper")
						.executes(source -> 
								selectViewMode(source, ViewMode.CREEPER)
						)
				)
		));
	}

	public static int selectViewMode(CommandContext<CommandSourceStack> source, ViewMode viewMode) {
		if (!HoloStruct.CLIENT.HOLORENDERER.loadPostEffect(viewMode.getPostEffect())) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.viewmode.failed", viewMode.getName()));
			return 0;
		}
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.viewmode.enabled", viewMode.getName()), false);
		return 1;
	}
	
	public static int layerMode(CommandContext<CommandSourceStack> source, boolean layerMode, int layer) {
		HoloStruct.CLIENT.HOLOGRAMS.setOneLayerMode(layerMode);
		if (layerMode) {
			HoloStruct.CLIENT.HOLOGRAMS.setActiveLayer(layer);
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.layermode.enabled", layer), false);
		} else {
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.layermode.disabled"), false);
		}
		return 1;
	}
	
	public static int changePosition(CommandContext<CommandSourceStack> source, String hologramName, BlockPos position) {
		Hologram hologram = HoloStruct.CLIENT.HOLOGRAMS.getHologram(hologramName);
		if (hologram == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.position.invalidhologram", hologramName));
			return 0;
		}
		
		hologram.setPosition(position);
		hologram.updateHoloStates(HoloStruct.CLIENT.LEVELBOUND.getAccessor());
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.position.changed", hologramName, position.getX(), position.getY(), position.getZ()), false);
		return 1;
	}
	
	public static int changeOrigin(CommandContext<CommandSourceStack> source, String hologramName, BlockPos newOrigin) {
		Hologram hologram = HoloStruct.CLIENT.HOLOGRAMS.getHologram(hologramName);
		if (hologram == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.origin.invalidhologram", hologramName));
			return 0;
		}
		
		BlockPos diff = newOrigin.subtract(hologram.getPosition());
		hologram.setOrigin(hologram.getOrigin().offset(diff));
		hologram.setPosition(newOrigin);
		hologram.updateHoloStates(HoloStruct.CLIENT.LEVELBOUND.getAccessor());
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.origin.changed", hologramName, newOrigin.getX(), newOrigin.getY(), newOrigin.getZ()), false);
		return 1;
	}
	
	public static int removeHologram(CommandContext<CommandSourceStack> source, String hologramName) {
		if (!HoloStruct.CLIENT.HOLOGRAMS.getHolograms().containsKey(hologramName)) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.remove.invalidhologram", hologramName));
			return 0;
		}
		HoloStruct.CLIENT.HOLOGRAMS.removeHologram(hologramName);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.remove.removed", hologramName), false);
		return 1;
	}
	
	public static int exportBlueprint(CommandContext<CommandSourceStack> source, String hologramName, String blueprintName) {
		Hologram hologram = HoloStruct.CLIENT.HOLOGRAMS.getHologram(hologramName);
		if (hologram == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.blueprint.invalidhologram", hologramName));
			return 0;
		}
		
		Blueprint blueprint = new Blueprint();
		hologram.copyTo(blueprint);
		HoloStruct.CLIENT.BLUEPRINTS.setLoadedBlueprint(blueprintName, blueprint);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.blueprint.stored", hologramName, blueprintName), false);
		return 1;
	}
	
	public static int createHologram(CommandContext<CommandSourceStack> source, String hologramName, String blueprintName) {
		Blueprint blueprint = null;;
		if (blueprintName != null) {
			blueprint = HoloStruct.CLIENT.BLUEPRINTS.getLoadedBlueprint(blueprintName);
			if (blueprint == null) {
				source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.create.invalidblueprint", blueprintName));
				return 0;
			}
		}
		
		BlockPos position = UtilHelper.toBlockPos(source.getSource().getPosition());
		Hologram hologram = HoloStruct.CLIENT.HOLOGRAMS.createHologram(blueprint, position, hologramName);
		
		if (hologram == null) {
			source.getSource().sendFailure(Component.translatable("holostruct.commands.hologram.create.failed", hologramName));
			return 0;
		}
		
		hologram.updateHoloStates(HoloStruct.CLIENT.LEVELBOUND.getAccessor());
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.hologram.create.created", hologramName), false);
		return 1;
	}
	
}
