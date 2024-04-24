package de.m_marvin.holostruct;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.m_marvin.holostruct.client.Config;
import de.m_marvin.holostruct.client.HoloStructClient;
import de.m_marvin.holostruct.client.registries.CommandArguments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(HoloStruct.MODID)
//@Mod.EventBusSubscriber(modid=HoloStruct.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class HoloStruct {
	
	public static final String MODID = "holostruct";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final HoloStructClient CLIENT = new HoloStructClient();
	
	private static HoloStruct INSTANCE;
	
	public HoloStruct(IEventBus bus) {
		INSTANCE = this;
		
		Config.register();
		CommandArguments.register(bus);
	}
	
	public static HoloStruct getInstance() {
		return INSTANCE;
	}
	
//	@SubscribeEvent
//	public static void onServerLogin(PlayerEvent.PlayerLoggedInEvent event) {
////		CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance()), AccessLevel.FULL_CLIENT);
//	}
	
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
