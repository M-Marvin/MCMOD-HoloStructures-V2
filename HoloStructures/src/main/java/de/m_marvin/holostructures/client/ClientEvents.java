package de.m_marvin.holostructures.client;

import de.m_marvin.holostructures.HoloStructures;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
	
	@SubscribeEvent
	public static final void onChatEvent(ClientChatEvent event) {
		
		System.out.println("TEST");
		//event.setCanceled(true);
		
	}
	
}
