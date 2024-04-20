package de.m_marvin.holostruct.client;

import java.util.concurrent.Executor;

import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.BlueprintManager;
import de.m_marvin.holostruct.client.commands.BlueprintCommand;
import de.m_marvin.holostruct.client.commands.HologramCommand;
import de.m_marvin.holostruct.client.holograms.HologramManager;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostruct.client.rendering.HolographicRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod.EventBusSubscriber(modid=HoloStruct.MODID, value=Dist.CLIENT, bus=Bus.FORGE)
public class HoloStructClient {
	
	public final Levelbound LEVELBOUND = new Levelbound();
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
	}
	
	@SubscribeEvent
	public static void onBlockChange(net.neoforged.neoforge.event.VanillaGameEvent event) {
		BlockPos position = event.getPos();
		System.out.println("UPDATE AT " + position);
//		HoloStruct.CLIENT.HOLOGRAMS.updateHoloSectionAt(position);
	}
	
	@SubscribeEvent
	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggingIn event) {
		// TODO access level
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance()));
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor());
	}
	
}
