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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/*
 * Main class of the mod, exists on server and client side.
 * Mainly registers other classes to the mod bus.
 */
@Mod(HoloStruct.MODID)
@EventBusSubscriber(modid=HoloStruct.MODID, bus=EventBusSubscriber.Bus.MOD)
public class HoloStruct {
	
	public static final String MODID = "holostruct";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final ServerLevelboundPackageHandler SERVER_LEVELBOUND = new ServerLevelboundPackageHandler();
	public static final HoloStructClient CLIENT = FMLEnvironment.dist.isClient() ? new HoloStructClient() : null;
	
	public HoloStruct(ModContainer container, IEventBus bus) {
		ClientConfig.register(container);
		ServerConfig.register(container);
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
	public static void onPayloadRegister(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(MODID).optional();
		registrar.playBidirectional(SetBlockStatePackage.TYPE, SetBlockStatePackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerSetBlockstate(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(SetBlockEntityPackage.TYPE, SetBlockEntityPackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerSetBlockEntity(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(AddEntityPackage.TYPE, AddEntityPackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerAddEntity(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(GetBlockStatePackage.TYPE, GetBlockStatePackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerGetBlockState(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(GetBlockEntityPackage.TYPE, GetBlockEntityPackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerGetBlockEntity(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(GetEntitiesPackage.TYPE, GetEntitiesPackage.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				SERVER_LEVELBOUND.handlerGetEntities(payload, context);
			} else {
				CLIENT.CLIENT_LEVELBOUND.handlerTaskResponse(payload, context);
			}
		});
		registrar.playBidirectional(QueryAccessPermissions.TYPE, QueryAccessPermissions.CODEC, (payload, context) -> {
			if (!context.player().level().isClientSide()) {
				HoloStruct.handlePermissonRequest(payload, context);
			} else {
				CLIENT.onAccessPermissionsReceived(payload, context);
			}
		});
	}
	
	public static void handlePermissonRequest(QueryAccessPermissions pkg, IPayloadContext context) {
		LOGGER.info("HS2/Permisson Access permissions requested!");
		String config = ServerConfig.write();
		context.reply(new QueryAccessPermissions(config));
	}
	
	/** TODO Feature liste 
	 * - .schematic parser
	 * - rotieren/scalieren/spiegeln von blaupausen und hologrammen
	 * - in-game editieren von hologrammen
	 * - materialliste/vortschrittsanzeige von hologrammen
	 * - pixel-art generator
	 */
	
}
