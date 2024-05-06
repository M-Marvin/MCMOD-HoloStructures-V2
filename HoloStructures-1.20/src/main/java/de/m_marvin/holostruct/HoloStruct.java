package de.m_marvin.holostruct;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import de.m_marvin.blueprints.BlueprintLoader;
import de.m_marvin.holostruct.client.ClientConfig;
import de.m_marvin.holostruct.client.HoloStructClient;
import de.m_marvin.holostruct.client.registries.CommandArguments;
import de.m_marvin.holostruct.levelbound.ServerLevelboundPackageHandler;
import de.m_marvin.holostruct.levelbound.network.AddEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockStatePackage;
import de.m_marvin.holostruct.levelbound.network.GetEntitiesPackage;
import de.m_marvin.holostruct.levelbound.network.QueryAccessPermissions;
import de.m_marvin.holostruct.levelbound.network.SetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

/*
 * Main class of the mod, exists on server and client side.
 * Mainly registers other classes to the mod bus.
 */
@Mod(HoloStruct.MODID)
@Mod.EventBusSubscriber(modid=HoloStruct.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class HoloStruct {
	
	public static final String MODID = "holostruct";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final ServerLevelboundPackageHandler SERVER_LEVELBOUND = new ServerLevelboundPackageHandler();
	public static final HoloStructClient CLIENT = FMLEnvironment.dist.isClient() ? new HoloStructClient() : null;
	
	public HoloStruct(IEventBus bus) {
		ClientConfig.register();
		ServerConfig.register();
		CommandArguments.register(bus);
		
		try {
			InputStream lagacyMappings = HoloStruct.class.getClassLoader().getResourceAsStream("legacy.json");
			JsonObject mappingJson = new Gson().fromJson(new InputStreamReader(lagacyMappings), JsonObject.class);
			mappingJson.keySet().forEach(index -> BlueprintLoader.LAGACY_STATE_MAP.put(Integer.parseInt(index), mappingJson.get(index).getAsString()));
			LOGGER.info("loaded " + BlueprintLoader.LAGACY_STATE_MAP.size() + " lagacy block ids");
		} catch (Throwable e) {
			LOGGER.warn("could not load lagacy block id mappings!");
			e.printStackTrace();
		}
		
	}

	@SubscribeEvent
	public static void onPayloadRegister(RegisterPayloadHandlerEvent event) {
		IPayloadRegistrar registrar = event.registrar(MODID).optional();
		registrar.play(SetBlockStatePackage.ID, SetBlockStatePackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerSetBlockstate)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(SetBlockEntityPackage.ID, SetBlockEntityPackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerSetBlockEntity)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(AddEntityPackage.ID, AddEntityPackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerAddEntity)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(GetBlockStatePackage.ID, GetBlockStatePackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerGetBlockState)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(GetBlockEntityPackage.ID, GetBlockEntityPackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerGetBlockEntity)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(GetEntitiesPackage.ID, GetEntitiesPackage::new, handler -> handler
				.server(SERVER_LEVELBOUND::handlerGetEntities)
				.client(CLIENT.CLIENT_LEVELBOUND::handlerTaskResponse));
		registrar.play(QueryAccessPermissions.ID, QueryAccessPermissions::new, handler -> handler
				.server(HoloStruct::handlePermissonRequest)
				.client(CLIENT::onAccessPermissionsReceived));
	}
	
	public static void handlePermissonRequest(QueryAccessPermissions pkg, PlayPayloadContext context) {
		LOGGER.info("HS2/Permisson Access permissions requested!");
		String config = ServerConfig.write();
		context.replyHandler().send(new QueryAccessPermissions(config));
	}
	
	/** TODO Feature liste 
	 * - .schematic parser
	 * - rotieren/scalieren/spiegeln von blaupausen und hologrammen
	 * - in-game editieren von hologrammen
	 * - materialliste/vortschrittsanzeige von hologrammen
	 * - pixel-art generator
	 */
	
}
