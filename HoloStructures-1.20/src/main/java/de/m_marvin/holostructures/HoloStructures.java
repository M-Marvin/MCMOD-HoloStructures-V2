package de.m_marvin.holostructures;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.m_marvin.holostructures.client.Config;
import de.m_marvin.holostructures.client.HoloStructClient;
import de.m_marvin.holostructures.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostructures.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostructures.client.registries.CommandArguments;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod("holostructures")
@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class HoloStructures {
	
	public static final String MODID = "holostructures";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final HoloStructClient CLIENT = new HoloStructClient();
	
	private static HoloStructures INSTANCE;
	
	public HoloStructures(IEventBus bus) {
		INSTANCE = this;
		
		Config.register();
		CommandArguments.register(bus);
	}
	
	public static HoloStructures getInstance() {
		return INSTANCE;
	}
	
	@SubscribeEvent
	public static void onServerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		CLIENT.LEVELBOUND.setAccess(new ClientLevelAccessorImpl(Minecraft.getInstance()), AccessLevel.FULL_CLIENT);
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
