package de.m_marvin.holostructures.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.worldaccess.ClientProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class SelectionRenderer {
	
	@SubscribeEvent
	public static void onLevelRenderLast(RenderLevelStageEvent event) {
		
		if (event.getStage() == Stage.AFTER_PARTICLES) {
			
			MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
			PoseStack matrixStack = event.getPoseStack();
			
			@SuppressWarnings("resource")
			Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			matrixStack.pushPose();
			matrixStack.translate(-offset.x, -offset.y, -offset.z);
			renderSelctionBoundingBox(matrixStack, source);
			matrixStack.popPose();

		}
		
	}
	
	public static void renderSelctionBoundingBox(PoseStack matrixStack, MultiBufferSource bufferSource) {
		
		ClientProcessor clientProcessor = ClientHandler.getInstance().getClientOnlyProcessor();
		if (clientProcessor.selectionCorner1 != null && clientProcessor.selectionCorner2 != null) {
			
			Vec3i pos1 = clientProcessor.selectionCorner1;
			Vec3i pos2 = clientProcessor.selectionCorner2;
			Vec3 c1 = new Vec3(pos1.getX() + 0.5F, pos1.getY() + 0.5F, pos1.getZ() + 0.5F);
			Vec3 c2 = new Vec3(pos2.getX() + 0.5F, pos2.getY() + 0.5F, pos2.getZ() + 0.5F);
			
			Vec3 corner1 = new Vec3(Math.min(c1.x(), c2.x()) - 0.5F, Math.min(c1.y(), c2.y()) - 0.5F, Math.min(c1.z(), c2.z()) - 0.5F);
			Vec3 corner2 = new Vec3(Math.max(c1.x(), c2.x()) + 0.5F, Math.max(c1.y(), c2.y()) + 0.5F, Math.max(c1.z(), c2.z()) + 0.5F);
			
			VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
			LevelRenderer.renderLineBox(matrixStack, buffer, corner1.x() ,corner1.y(), corner1.z(), corner2.x(), corner2.y(), corner2.z(), 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
			
		}
		
	}
	
}
