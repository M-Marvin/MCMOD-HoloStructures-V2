package de.m_marvin.holostruct.client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.ServerConfig;
import de.m_marvin.holostruct.client.blueprints.BlueprintManager;
import de.m_marvin.holostruct.client.commands.BlueprintCommand;
import de.m_marvin.holostruct.client.commands.DebugCommand;
import de.m_marvin.holostruct.client.commands.HologramCommand;
import de.m_marvin.holostruct.client.commands.PixelArtCommand;
import de.m_marvin.holostruct.client.commands.StatusCommand;
import de.m_marvin.holostruct.client.event.ClientBlockEvent;
import de.m_marvin.holostruct.client.event.ClientLanguageInjectEvent;
import de.m_marvin.holostruct.client.holograms.HologramManager;
import de.m_marvin.holostruct.client.holograms.rendering.HolographicRenderer;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientCommandDispatcher;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ClientLevelboundPackageHandler;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ServerLevelAccessorImpl;
import de.m_marvin.holostruct.client.pixelart.PixelArtGenerator;
import de.m_marvin.holostruct.client.pixelart.rendering.PreviewRenderer;
import de.m_marvin.holostruct.levelbound.network.QueryAccessPermissions;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The client side only instance of the mod.
 * This does only exist on the client side.
 * @author Marvin Koehler
 */
@EventBusSubscriber(modid=HoloStruct.MODID, value=Dist.CLIENT, bus=Bus.GAME)
public class HoloStructClient {
	
	/** The levelbound instance used to access the server level */
	public final Levelbound LEVELBOUND = new Levelbound();
	/** The command dispatcher instance used to execute server commands and get responses for the client {@link IRemoteLevelAccessor} implementation */
	public final ClientCommandDispatcher COMMAND_DISPATCHER = new ClientCommandDispatcher();
	/** The client side package handler for the sever {@link IRemoteLevelAccessor} implementation */
	public final ClientLevelboundPackageHandler CLIENT_LEVELBOUND = new ClientLevelboundPackageHandler();
	/** The blueprint manager instance used to manage loaded blueprints */
	public final BlueprintManager BLUEPRINTS = new BlueprintManager();
	/** The hologram manager instance used to manage all holograms */
	public final HologramManager HOLOGRAMS = new HologramManager();
	/** The holographic renderer instance used to render the holograms */
	public final HolographicRenderer HOLORENDERER = new HolographicRenderer();
	/** The pixelart generator instance used when creating pixel arts */
	public final PixelArtGenerator PIXELART_GENERATOR = new PixelArtGenerator();
	/** The pixelart preview renderer used to render an image of the pixel art */
	public final PreviewRenderer PIXELART_PREVIEW = new PreviewRenderer();
	
	/**
	 * Executor used to execute {@link CompletableFuture} tasks on the render thread
	 */
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
		StatusCommand.register(event.getDispatcher());
		PixelArtCommand.register(event.getDispatcher(), event.getBuildContext());
	}
	
	@SubscribeEvent
	public static void onBlockChange(ClientBlockEvent event) {
		HoloStruct.CLIENT.HOLOGRAMS.updateHoloSectionAt(event.getPosition());
	}
	
	@SubscribeEvent
	public static void onChatMessageReceived(ClientChatReceivedEvent event) {
		if (event.getBoundChatType() == null) return;
		if (!event.getBoundChatType().name().getString().equals("System")) return;
		if (HoloStruct.CLIENT.COMMAND_DISPATCHER.handleSysteMessage(event.getMessage().getString()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onLanguageInject(ClientLanguageInjectEvent event) {
		HoloStruct.CLIENT.COMMAND_DISPATCHER.reloadReverseMap(event.getLanguage());
	}
	
	private static void updateAccessPermisson() {
		if (ServerConfig.ALLOW_READ.get()) {
			@SuppressWarnings("resource")
			boolean serverMode = Minecraft.getInstance().player.connection.hasChannel(SetBlockStatePackage.TYPE);
			
			if (serverMode) {
				HoloStruct.LOGGER.info("Connect to server with HS2 installed!");
				HoloStruct.CLIENT.LEVELBOUND.setAccess(new ServerLevelAccessorImpl(Minecraft.getInstance(), ServerConfig.ALLOW_COPY.get(), ServerConfig.ALLOW_WRITE.get()));
			} else {
				HoloStruct.LOGGER.info("Connect to server without HS2 installed!");
				HoloStruct.CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance(), ServerConfig.ALLOW_COPY.get(), ServerConfig.ALLOW_WRITE.get()));
			}
		} else {
			HoloStruct.LOGGER.info("Connect to server with HS2 disabled!");
			HoloStruct.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor());
		}
	}
	
	@SubscribeEvent
	public static void onConnectoToServer(ClientPlayerNetworkEvent.LoggingIn event) {
		// Ensures the initial language is loaded
		HoloStruct.CLIENT.COMMAND_DISPATCHER.reloadReverseMap(Language.getInstance());
		
		ServerConfig.loadDefault();
		updateAccessPermisson();
		
		boolean serverHasPermissonConfig = 
				event.getPlayer().connection.hasChannel(QueryAccessPermissions.TYPE) &&
				event.getPlayer().connection.hasChannel(QueryAccessPermissions.TYPE);
		
		if (serverHasPermissonConfig) {
			HoloStruct.LOGGER.info("HS2/Permissons Querry access permissions from server");
			String defaultConfig = ServerConfig.write();
			event.getPlayer().connection.send(new QueryAccessPermissions(defaultConfig)); 
		}
	}
	
	public void onAccessPermissionsReceived(QueryAccessPermissions pkg, IPayloadContext context) {
		HoloStruct.LOGGER.info("HS2/Permissons Received configuation from server!");
		String config = pkg.config();
		ServerConfig.load(config);
		updateAccessPermisson();
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor());
	}
	
	/**
	 * Returns the name of the level folder of the currently loaded game on the client.
	 */
	public static Optional<String> getLocalLevelFolderName() {
		IntegratedServer localServer = Minecraft.getInstance().getSingleplayerServer();
		if (localServer == null) return Optional.empty();
		String levelFolderName = localServer.storageSource.getLevelDirectory().directoryName();
		return Optional.of(levelFolderName);
	}
	
}
