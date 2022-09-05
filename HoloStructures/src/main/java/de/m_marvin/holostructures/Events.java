package de.m_marvin.holostructures;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
	
	@SubscribeEvent
	public static void onCommandRegister(RegisterCommandsEvent event) {
		//TestCommand.register(event.getDispatcher());
	}
	
}
