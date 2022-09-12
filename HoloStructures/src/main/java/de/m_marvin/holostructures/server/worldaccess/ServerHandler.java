package de.m_marvin.holostructures.server.worldaccess;

import de.m_marvin.holostructures.HoloStructures;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.DEDICATED_SERVER)
public class ServerHandler {

	private static ServerHandler INSTANCE;
	
	static {
		new ServerHandler();
	}
	
	public ServerHandler() {
		INSTANCE = this;
		ServerLevelAccessHandler.registerPackages(HoloStructures.NETWORK_WORLD_ACCESS);
	}
	
	public static ServerHandler getInstance() {
		return INSTANCE;
	}
	
}
