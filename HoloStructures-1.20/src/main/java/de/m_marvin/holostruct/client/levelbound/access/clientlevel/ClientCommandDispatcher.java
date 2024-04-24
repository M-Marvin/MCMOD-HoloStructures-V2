package de.m_marvin.holostruct.client.levelbound.access.clientlevel;

import java.awt.image.ComponentSampleModel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

public class ClientCommandDispatcher {
	
	public static final Supplier<ClientPacketListener> PACKAGE_LISTENER = () -> Minecraft.getInstance().getConnection();
	
	public <T> CompletableFuture<T> startDispatch(Command<T> command) {

		System.out.println(command.command());
		
		try {

//			CommandDispatcher<SharedSuggestionProvider> dispatcher = Minecraft.getInstance().player.connection.getCommands();
//			ClientSuggestionProvider commandStack = Minecraft.getInstance().player.connection.getSuggestionsProvider();
//			ParseResults<SharedSuggestionProvider> commandParsed = dispatcher.parse(command.command(), commandStack);
//
//			if (commandParsed.getReader().canRead()) {
//				// TODO failed parsing
//			}
			
			System.out.println(command.command());
			
			PACKAGE_LISTENER.get().sendCommand(command.command());
			
//			int result = dispatcher.execute(commandParsed);
			
//			System.out.println("Finished with code " + result);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
//		Minecraft m;
//		m.player.getServer().getFunctions().getDispatcher().execute(null, null)
//		
		return null;
		
	}

	public boolean handleSysteMessage(String message) {
		
		Language language = Language.getInstance();
		
		
		
		System.out.println(message);
		
		return true;
	}
	
}
