package de.m_marvin.holostructures.client;

import com.google.common.base.Optional;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.ILevelAccessor;
import de.m_marvin.holostructures.client.blueprints.BlueprintManager;
import de.m_marvin.holostructures.client.worldaccess.ClientLevelAccessorImpl;
import de.m_marvin.holostructures.client.worldaccess.ClientProcessor;
import de.m_marvin.holostructures.client.worldaccess.ServerLevelAccessorImpl;
import de.m_marvin.holostructures.server.worldaccess.ServerLevelAccessHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ClientHandler {
	
	public BlueprintManager blueprints = new BlueprintManager();
	public ClientProcessor clientLevelProcessor = new ClientProcessor();
	// TODO serverProcessor
	public ILevelAccessor levelAccessor;
	private static ClientHandler INSTANCE;
	
	static {
		new ClientHandler();
	}
	
	public ClientHandler() {
		INSTANCE = this;
		ServerLevelAccessHandler.registerPackages(HoloStructures.NETWORK_WORLD_ACCESS);
	}
	
	public static ClientHandler getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public static final void onCommandsRegister(RegisterClientCommandsEvent event) {
		getInstance().getClientOnlyProcessor().registerCommands(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
		getInstance().createAccessor();
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		getInstance().clearAccessor();
	}
	
	public boolean runsOnServerSide() {
		// TODO Does not work
		return HoloStructures.NETWORK_WORLD_ACCESS.isRemotePresent(Minecraft.getInstance().getConnection().getConnection());
	}
	
	public void createAccessor() {
		if (runsOnServerSide()) {
			this.levelAccessor = new ClientLevelAccessorImpl(() -> Minecraft.getInstance().level, () -> Minecraft.getInstance().player);
		} else {
			this.levelAccessor = new ServerLevelAccessorImpl(HoloStructures.NETWORK_WORLD_ACCESS, () -> Minecraft.getInstance().level, () -> Minecraft.getInstance().player);
		}
	}
	
	public void clearAccessor() {
		this.levelAccessor = null;
	}
	
	public ClientProcessor getClientOnlyProcessor() {
		return this.clientLevelProcessor;
	}
	
	public Optional<ILevelAccessor> getLevelAccessor() {
		return this.levelAccessor != null ? Optional.of(this.levelAccessor) : Optional.absent();
	}
	
	public BlueprintManager getBlueprints() {
		return blueprints;
	}
	
}
