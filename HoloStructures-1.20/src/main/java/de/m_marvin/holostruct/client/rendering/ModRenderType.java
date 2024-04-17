package de.m_marvin.holostruct.client.rendering;
//package de.m_marvin.holostructures.client.rendering;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.function.Function;
//
//import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.blaze3d.vertex.VertexFormat;
//import com.mojang.blaze3d.vertex.VertexFormat.Mode;
//
//import de.m_marvin.holostructures.HoloStructures;
//import net.minecraft.Util;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.ShaderInstance;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.PreparableReloadListener;
//import net.minecraft.server.packs.resources.ResourceManager;
//import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
//import net.minecraft.util.profiling.ProfilerFiller;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.Mod;
//import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
//import net.neoforged.neoforge.client.event.RegisterShadersEvent;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//
//@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
//public class ModRenderType extends RenderType {
//	
//	private ModRenderType(String a1, VertexFormat a2, Mode a3, int a4, boolean a5, boolean a6, Runnable a7, Runnable a8) {
//		super(a1, a2, a3, a4, a5, a6, a7, a8);
//		throw new IllegalStateException("This clas is not meant to be constructed!");
//	}
//	
//	/* Register ShaderInstances */
//	
//	@SubscribeEvent
//	public static void onShaderRegister(RegisterShadersEvent event) throws IOException {
//		try {
//			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("holostructures", "rendertype_hologram_blocks"), DefaultVertexFormat.BLOCK), shader -> {
//				hologramBlocksShaderInstance = shader;
//			});
//		} catch (IOException e) {
//			HoloStructures.LOGGER.error("Failed to load shader!");
//			e.printStackTrace();
//		}
//	}
//	
//	/* Create ShaderStateShard */
//	
//	public static ShaderInstance hologramBlocksShaderInstance;
//	public static final ShaderStateShard HOLOGRAM_BLOCKS_SHADER = new ShaderStateShard(() -> hologramBlocksShaderInstance);
//	
//	/* Create RenderType factory */
//	
//	public static RenderType hologramBlocks() { return HOLOGRAM_BLOCKS_MEMOIZE.apply(new ResourceLocation("no_texture")); }
//	private static final Function<ResourceLocation, RenderType> HOLOGRAM_BLOCKS_MEMOIZE = Util.memoize((texture) -> {
//		RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
//			.setLightmapState(LIGHTMAP)
//			.setShaderState(HOLOGRAM_BLOCKS_SHADER)
//			.setTextureState(BLOCK_SHEET)
//			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//			.setOutputState(TRANSLUCENT_TARGET)
//			.createCompositeState(true);
//		return create("holostructures_hologram_blocks", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, rendertype$state);
//	});
//	
//	/* RenderType utility methods */
//	
//	
//}
