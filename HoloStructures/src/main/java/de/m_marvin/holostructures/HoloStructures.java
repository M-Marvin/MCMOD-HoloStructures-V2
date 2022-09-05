package de.m_marvin.holostructures;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.mojang.logging.LogUtils;

import de.m_marvin.holostructures.client.blueprints.BlueprintManager;
import de.m_marvin.holostructures.client.worldaccess.ClientLevelAccessorImpl;
import de.m_marvin.holostructures.client.worldaccess.ClientProcessor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("holostructures")
@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class HoloStructures {
	
	public static final String MODID = "holostructures";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static HoloStructures INSTANCE;
	
	public BlueprintManager blueprints = new BlueprintManager();
	public ClientProcessor clientLevelProcessor = new ClientProcessor();
	// TODO serverProcessor
	public ILevelAccessor levelAccessor;
	
	public HoloStructures() {
		INSTANCE = this;
	}
	
	public static HoloStructures getInstance() {
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
	
	public void createAccessor() {
		// TODO Server Accessor
		this.levelAccessor = new ClientLevelAccessorImpl(() -> Minecraft.getInstance().level, () -> Minecraft.getInstance().player);
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
