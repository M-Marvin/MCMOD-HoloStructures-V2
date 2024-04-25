package de.m_marvin.holostruct.client;

import java.util.concurrent.Executor;

import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.BlueprintManager;
import de.m_marvin.holostruct.client.commands.BlueprintCommand;
import de.m_marvin.holostruct.client.commands.DebugCommand;
import de.m_marvin.holostruct.client.commands.HologramCommand;
import de.m_marvin.holostruct.client.event.ClientBlockEvent;
import de.m_marvin.holostruct.client.event.ClientLanguageInjectEvent;
import de.m_marvin.holostruct.client.holograms.HologramManager;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientCommandDispatcher;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostruct.client.rendering.HolographicRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@Mod.EventBusSubscriber(modid=HoloStruct.MODID, value=Dist.CLIENT, bus=Bus.FORGE)
public class HoloStructClient {
	
	public final Levelbound LEVELBOUND = new Levelbound();
	public final ClientCommandDispatcher COMMAND_DISPATCHER = new ClientCommandDispatcher();
	public final BlueprintManager BLUEPRINTS = new BlueprintManager();
	public final HologramManager HOLOGRAMS = new HologramManager();
	public final HolographicRenderer HOLORENDERER = new HolographicRenderer();
	
	public final Executor RENDER_EXECUTOR = new Executor() {
		@Override
		public void execute(Runnable command) {
			RenderSystem.recordRenderCall(() -> command.run());
		}
	};
	
	@SubscribeEvent
	public static final void onCommandsRegister(RegisterClientCommandsEvent event) {
		BlueprintCommand.register(event.getDispatcher());
		HologramCommand.register(event.getDispatcher());
		DebugCommand.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onBlockChange(ClientBlockEvent event) {
		HoloStruct.CLIENT.HOLOGRAMS.updateHoloSectionAt(event.getPosition());
	}
	
	@SubscribeEvent
	public static void onChatMessageReceived(ClientChatReceivedEvent event) {
		if (!event.getBoundChatType().name().getString().equals("System")) return;
		if (HoloStruct.CLIENT.COMMAND_DISPATCHER.handleSysteMessage(event.getMessage().getString()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onLanguageInject(ClientLanguageInjectEvent event) {
		HoloStruct.CLIENT.COMMAND_DISPATCHER.reloadReverseMap(event.getLanguage());
	}
	
	@SubscribeEvent
	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggingIn event) {
		// Ensures the initial language is loaded
		HoloStruct.CLIENT.COMMAND_DISPATCHER.reloadReverseMap(Language.getInstance());
		
		// TODO access level
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance(), true, true));
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor());
	}
	
}
