package de.m_marvin.holostruct.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Command used to print the status of the levelbound access level
 * @author Marvin Koehler
 */
public class StatusCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("hs2status")
		.executes(source ->
				printStatus(source)
		));
	}
	
	public static int printStatus(CommandContext<CommandSourceStack> source) {
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.title"), false);
		
		AccessLevel accessLevel = HoloStruct.CLIENT.LEVELBOUND.getAccessLevel();
		switch (accessLevel) {
		case NO_ACCESS:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.no_access"), false);
			break;
		case READ_CLIENT:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.read_client"), false);
			break;
		case COPY_CLIENT:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.copy_client"), false);
			break;
		case FULL_CLIENT:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.full_client"), false);
			break;
		case READ_SERVER:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.read_server"), false);
			break;
		case COPY_SERVER:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.copy_server"), false);
			break;
		case FULL_SERVER:
			source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.full_server"), false);
			break;
		}

		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.details"), false);
		
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.details.read." + (accessLevel.hasRead() ? "yes" : "no")), false);
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.details.copy." + (accessLevel.hasCopy() ? "yes" : "no")), false);
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.details.write." + (accessLevel.hasWrite() ? "yes" : "no")), false);
		source.getSource().sendSuccess(() -> Component.translatable("holostruct.commands.status.details.server." + (accessLevel.isServer() ? "yes" : "no")), false);
		
		return 1;
		
	}
	
}
