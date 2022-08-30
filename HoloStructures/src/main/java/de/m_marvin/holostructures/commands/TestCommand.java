package de.m_marvin.holostructures.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

public class TestCommand {
	
	public static void register (CommandDispatcher<CommandSourceStack> dispatcher) {
		
		dispatcher.register(Commands.literal("testcommand").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((source) -> {
			return execute(BlockPosArgument.getLoadedBlockPos(source, "pos"));
		})));
		
	}
	
	public static int execute(BlockPos pos) {
		
		System.out.println("TESTCOM");
		return 1;
		
	}
	
}
