package de.m_marvin.holostruct.client;
//package de.m_marvin.holostructures.client;
//
//import com.google.common.base.Optional;
//
//import de.m_marvin.holostructures.HoloStructures;
//import de.m_marvin.holostructures.client.holograms.HologramManager;
//import de.m_marvin.holostructures.client.worldaccess.ClientLevelAccessorImpl;
//import de.m_marvin.holostructures.client.worldaccess.ClientProcessor;
//import de.m_marvin.holostructures.client.worldaccess.ILevelAccessorx;
//import net.minecraft.client.Minecraft;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.Mod;
//import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
//import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
//
//@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
//public class ClientHandler {
//	
//	public BlueprintManager blueprints = new BlueprintManager();
//	public HologramManager holograms = new HologramManager();
//	public ClientProcessor clientLevelProcessor = new ClientProcessor();
//	public ILevelAccessorx levelAccessor;
//	private static ClientHandler INSTANCE;
//	
//	static {
//		new ClientHandler();
//	}
//	
//	public ClientHandler() {
//		INSTANCE = this;
//		Config.register();
//	}
//	
//	public static ClientHandler getInstance() {
//		return INSTANCE;
//	}
//	
//	/* Event handling */
//
//	@SubscribeEvent
//	public static final void onCommandsRegister(RegisterClientCommandsEvent event) {
//		getInstance().getClientOnlyProcessor().registerCommands(event.getDispatcher());
//	}
//	
//	@SubscribeEvent
//	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggingIn event) {
//		getInstance().createAccessor();
//	}
//	
//	@SubscribeEvent
//	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
//		getInstance().clearAccessor();
//	}
//	
//	/* End of event handling */
//	
//	public void createAccessor() {
//		this.levelAccessor = new ClientLevelAccessorImpl(() -> Minecraft.getInstance().level, () -> Minecraft.getInstance().player);
//	}
//	
//	public void clearAccessor() {
//		this.levelAccessor = null;
//	}
//	
//	public ClientProcessor getClientOnlyProcessor() {
//		return this.clientLevelProcessor;
//	}
//	
//	public Optional<ILevelAccessorx> getLevelAccessor() {
//		return this.levelAccessor != null ? Optional.of(this.levelAccessor) : Optional.absent();
//	}
//	
//	public BlueprintManager getBlueprints() {
//		return blueprints;
//	}
//	
//	public HologramManager getHolograms() {
//		return holograms;
//	}
//	
//}
