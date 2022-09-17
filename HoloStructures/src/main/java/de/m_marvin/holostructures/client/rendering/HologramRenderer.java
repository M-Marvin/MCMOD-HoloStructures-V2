package de.m_marvin.holostructures.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.holograms.Hologram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HologramRenderer {
	
	@SubscribeEvent
	public static void onLevelRenderLast(RenderLevelStageEvent event) {

		if (event.getStage() == Stage.AFTER_PARTICLES) {
			
			MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
			PoseStack matrixStack = event.getPoseStack();
			
			@SuppressWarnings("resource")
			Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			matrixStack.pushPose();
			matrixStack.translate(-offset.x, -offset.y, -offset.z);
			renderHolograms(matrixStack, source);
			matrixStack.popPose();

		}
		
	}
	
	public static void renderHolograms(PoseStack matrixStack, MultiBufferSource buffer) {
		
		
		
	}
	
	public static void renderHologramm(Hologram hologram) {
		
		
		
	}
	
	public static void renderBlock(PoseStack matrixStack, MultiBufferSource buffer, BlockState block, Blueprint.EntityData blockentity) {
		
		
		
	}
	
}
