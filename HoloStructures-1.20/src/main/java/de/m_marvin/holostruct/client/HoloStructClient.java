package de.m_marvin.holostruct.client;

import java.util.Optional;
import java.util.concurrent.Executor;

import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.ServerConfig;
import de.m_marvin.holostruct.client.blueprints.BlueprintManager;
import de.m_marvin.holostruct.client.commands.BlueprintCommand;
import de.m_marvin.holostruct.client.commands.DebugCommand;
import de.m_marvin.holostruct.client.commands.HologramCommand;
import de.m_marvin.holostruct.client.commands.StatusCommand;
import de.m_marvin.holostruct.client.event.ClientBlockEvent;
import de.m_marvin.holostruct.client.event.ClientLanguageInjectEvent;
import de.m_marvin.holostruct.client.holograms.HologramManager;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientCommandDispatcher;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ClientLevelboundPackageHandler;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ServerLevelAccessorImpl;
import de.m_marvin.holostruct.client.rendering.HolographicRenderer;
import de.m_marvin.holostruct.levelbound.network.GetAccessPermissions;
import de.m_marvin.holostruct.levelbound.network.SendAccessPermissons;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

@Mod.EventBusSubscriber(modid=HoloStruct.MODID, value=Dist.CLIENT, bus=Bus.FORGE)
public class HoloStructClient {
	
	public final Levelbound LEVELBOUND = new Levelbound();
	public final ClientCommandDispatcher COMMAND_DISPATCHER = new ClientCommandDispatcher();
	public final BlueprintManager BLUEPRINTS = new BlueprintManager();
	public final HologramManager HOLOGRAMS = new HologramManager();
	public final HolographicRenderer HOLORENDERER = new HolographicRenderer();
	public final ClientLevelboundPackageHandler CLIENT_LEVELBOUND = new ClientLevelboundPackageHandler();
	
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
	
	public static void updateAccessPermisson() {
		if (ServerConfig.ALLOW_READ.get()) {
			@SuppressWarnings("resource")
			boolean serverMode = Minecraft.getInstance().player.connection.isConnected(SetBlockStatePackage.ID);
			
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
				event.getPlayer().connection.isConnected(GetAccessPermissions.ID) &&
				event.getPlayer().connection.isConnected(GetAccessPermissions.ID);
		
		if (serverHasPermissonConfig) {
			HoloStruct.LOGGER.info("HS2/Permissons Querry access permissions from server");
			String defaultConfig = ServerConfig.write();
			event.getPlayer().connection.send(new GetAccessPermissions(defaultConfig)); 
		}
	}
	
	public void onAccessPermissionsReceived(SendAccessPermissons pkg, PlayPayloadContext context) {
		HoloStruct.LOGGER.info("HS2/Permissons Received configuation from server!");
		String config = pkg.permissonConfig();
		ServerConfig.load(config);
		updateAccessPermisson();
	}
	
	@SubscribeEvent
	public static void onDisconnectServer(ClientPlayerNetworkEvent.LoggingOut event) {
		HoloStruct.CLIENT.LEVELBOUND.setAccess(new NoAccessAccessor());
	}
	
	public static Optional<String> getLocalLevelFolderName() {
		IntegratedServer localServer = Minecraft.getInstance().getSingleplayerServer();
		if (localServer == null) return Optional.empty();
		String levelFolderName = localServer.storageSource.getLevelDirectory().directoryName();
		return Optional.of(levelFolderName);
	}
	
}
