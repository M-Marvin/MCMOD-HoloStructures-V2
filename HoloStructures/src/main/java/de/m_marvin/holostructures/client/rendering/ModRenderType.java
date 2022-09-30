package de.m_marvin.holostructures.client.rendering;

import java.io.IOException;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import de.m_marvin.holostructures.HoloStructures;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ModRenderType extends RenderType {
	
	private ModRenderType(String a1, VertexFormat a2, Mode a3, int a4, boolean a5, boolean a6, Runnable a7, Runnable a8) {
		super(a1, a2, a3, a4, a5, a6, a7, a8);
		throw new IllegalStateException("This clas is not meant to be constructed!");
	}
	
	public static ShaderInstance hologramBlocsShaderInstance;
	public static final ShaderStateShard HOLOGRAM_BLOCKS_SHADER = new ShaderStateShard(() -> hologramBlocsShaderInstance);
	
	@SubscribeEvent
	public static void onShaderRegister(RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation(HoloStructures.MODID, "hologram_blocks"), DefaultVertexFormat.BLOCK), shader -> hologramBlocsShaderInstance = shader);
	}
	
	private static final Function<ResourceLocation, RenderType> HOLOGRAM_BLOCKS_MEMOIZE = Util.memoize((texture) -> {
		RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(HOLOGRAM_BLOCKS_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(TRANSLUCENT_TARGET)
			.createCompositeState(true);
		return create("holostructures_hologram_blocks", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, rendertype$state);
	});
	
	public static RenderType hologramBlocks() {
		return HOLOGRAM_BLOCKS_MEMOIZE.apply(new ResourceLocation("no_texture"));
	}
		
}
