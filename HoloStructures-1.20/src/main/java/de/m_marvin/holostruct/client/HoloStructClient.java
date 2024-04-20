package de.m_marvin.holostruct.client;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.injection.Inject;

import com.google.j2objc.annotations.ReflectionSupport.Level;
import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.BlueprintManager;
import de.m_marvin.holostruct.client.commands.BlueprintCommand;
import de.m_marvin.holostruct.client.commands.DebugCommand;
import de.m_marvin.holostruct.client.commands.HologramCommand;
import de.m_marvin.holostruct.client.event.ClientBlockEvent;
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
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

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
		DebugCommand.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onBlockChange(ClientBlockEvent event) {
		HoloStruct.CLIENT.HOLOGRAMS.updateHoloSectionAt(event.getPosition());
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
