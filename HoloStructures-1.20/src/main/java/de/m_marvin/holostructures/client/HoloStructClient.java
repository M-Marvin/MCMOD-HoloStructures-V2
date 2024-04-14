package de.m_marvin.holostructures.client;

import java.util.concurrent.Executor;

import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.blueprints.BlueprintManager;
import de.m_marvin.holostructures.client.commands.BlueprintCommand;
import de.m_marvin.holostructures.client.commands.HologramCommand;
import de.m_marvin.holostructures.client.holograms.HologramManager;
import de.m_marvin.holostructures.client.levelbound.Levelbound;
import de.m_marvin.holostructures.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostructures.client.levelbound.access.NoAccessAccessor;
import de.m_marvin.holostructures.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, value=Dist.CLIENT, bus=Bus.FORGE)
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
	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggingIn event) {
		// TODO access level
		HoloStructures.CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance()), AccessLevel.FULL_CLIENT);
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
		HoloStructures.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor(), AccessLevel.NO_ACCESS);
	}
	
}
