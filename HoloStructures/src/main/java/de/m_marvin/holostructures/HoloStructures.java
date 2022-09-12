package de.m_marvin.holostructures;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.m_marvin.holostructures.client.worldaccess.ServerLevelAccessorImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod("holostructures")
public class HoloStructures {
	
	public static final String MODID = "holostructures";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final SimpleChannel NETWORK_WORLD_ACCESS = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "world_access"), () -> ServerLevelAccessorImpl.PROTOCOL_VERSION, ServerLevelAccessorImpl.PROTOCOL_VERSION::equals, ServerLevelAccessorImpl.PROTOCOL_VERSION::equals);
	private static HoloStructures INSTANCE;
		
	public HoloStructures() {
		INSTANCE = this;
	}
	
	public static HoloStructures getInstance() {
		return INSTANCE;
	}
	
}
