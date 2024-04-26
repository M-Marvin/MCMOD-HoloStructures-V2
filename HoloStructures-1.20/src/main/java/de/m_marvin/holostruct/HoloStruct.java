package de.m_marvin.holostruct;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.m_marvin.holostruct.client.Config;
import de.m_marvin.holostruct.client.HoloStructClient;
import de.m_marvin.holostruct.client.registries.CommandArguments;
import de.m_marvin.holostruct.levelbound.ServerLevelboundPackageHandler;
import de.m_marvin.holostruct.levelbound.network.AddEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockStatePackage;
import de.m_marvin.holostruct.levelbound.network.GetEntitiesPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod(HoloStruct.MODID)
@Mod.EventBusSubscriber(modid=HoloStruct.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class HoloStruct {
	
	public static final String MODID = "holostruct";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final ServerLevelboundPackageHandler SERVER_LEVELBOUND = new ServerLevelboundPackageHandler();
	public static final HoloStructClient CLIENT = FMLEnvironment.dist.isClient() ? new HoloStructClient() : null;
	
	public HoloStruct(IEventBus bus) {
		Config.register();
		CommandArguments.register(bus);
	}

	@SubscribeEvent
	public static void onPayloadRegister(RegisterPayloadHandlerEvent event) {
		IPayloadRegistrar registrar = event.registrar(MODID);
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
	}
	
	/* TODO */
	// Schem format mit Mod-Blöcken die fehlen
	// Fehlende-Mods liste beim laden einer Blaupause
	// Blaupausen rotieren/spiegeln
	// In-Game editieren von Hologrammen
	// Layer-Ansicht der Hologramme
	// Entities und BlockEntities in Hologrammen
	// GUI-Version der Befehle
	// Mod-API f�r mod-specifische Dinge (z.B. Industria Conduits, Create Klebstoff)
	// Hologram shader: https://gist.github.com/gigaherz/b8756ff463541f07a644ef8f14cb10f5
	// https://github.com/XFactHD/FramedBlocks/blob/1.19.x/src/main/java/xfacthd/framedblocks/api/util/client/OutlineRender.java#L74-L87
	
}
