package de.m_marvin.holostruct.client.commands.arguments;

import de.m_marvin.holostruct.HoloStruct;
import net.minecraft.resources.ResourceLocation;

/**
 * Argument for the viewmoder used for holograms
 * @author Marvin Koehler
 */
public enum ViewMode {
	
	VANILLA("vanilla", null),
	BASE("base", ResourceLocation.fromNamespaceAndPath(HoloStruct.MODID, "base")),
	SCANLINE("scanline", ResourceLocation.fromNamespaceAndPath(HoloStruct.MODID, "scanline")),
	CREEPER("creeper", ResourceLocation.fromNamespaceAndPath(HoloStruct.MODID, "creeper"));
	
	private final ResourceLocation postEffect;
	private final String name;
	
	ViewMode(String name, ResourceLocation postEffect) {
		this.name = name;
		this.postEffect = postEffect;
	}
	
	public String getName() {
		return name;
	}
	
	public ResourceLocation getPostEffect() {
		return postEffect;
	}
	
}
