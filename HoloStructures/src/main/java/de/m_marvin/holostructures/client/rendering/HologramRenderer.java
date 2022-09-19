package de.m_marvin.holostructures.client.rendering;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import de.m_marvin.holostructures.client.holograms.Hologram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HologramRenderer {
	
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDER_DISPATCHER = () -> Minecraft.getInstance().getBlockRenderer();
	public static final Supplier<BlockEntityRenderDispatcher> BLOCK_ENTITY_RENDER_DISPATCHER = () -> Minecraft.getInstance().getBlockEntityRenderDispatcher();
	public static final Supplier<EntityRenderDispatcher> ENTITY_RENDER_DISPATCHER = () -> Minecraft.getInstance().getEntityRenderDispatcher();
	
	@SubscribeEvent
	public static void onLevelRenderLast(RenderLevelStageEvent event) {

		if (event.getStage() == Stage.AFTER_PARTICLES) {
			
			MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
			PoseStack matrixStack = event.getPoseStack();
			
			@SuppressWarnings("resource")
			Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			matrixStack.pushPose();
			matrixStack.translate(-offset.x, -offset.y, -offset.z);
			renderHolograms(matrixStack, source, event.getPartialTick());
			matrixStack.popPose();

		}
		
	}
	
	public static void renderHolograms(PoseStack matrixStack, MultiBufferSource buffer, float partialTick) {
		
		if (ClientHandler.getInstance().getLevelAccessor().isPresent()) {
			
			
			
			Vec3i size = hologram.getBlueprint().getSize().offset(1, 1, 1);
			int chunkCountX = Math.ceilDiv(size.getX(), 16);
			int chunkCountY = Math.ceilDiv(size.getY(), 16);
			int chunkCountZ = Math.ceilDiv(size.getZ(), 16);
			
			for (int cx = 0; cx < chunkCountX; cx++) {
				for (int cz = 0; cz < chunkCountZ; cz++) {
					for (int cy = 0; cy < chunkCountY; cy++) {
						Vec3i holoChunk = new Vec3i(cx, cy, cz);
						
						
						
					}
				}
			}
			
			
			Collection<Hologram> holograms = ClientHandler.getInstance().getHolograms().getHolograms();
			BlockAndTintGetter blockAndTintGetter = ClientHandler.getInstance().getLevelAccessor().get().getLevelGetter();
					
			holograms.forEach((hologram) -> {
				
				BlockPos hologramPosition = hologram.getPosition();
				matrixStack.pushPose();
				matrixStack.translate(hologramPosition.getX(), hologramPosition.getY(), hologramPosition.getZ());
				renderHologram(matrixStack, buffer, partialTick, blockAndTintGetter, hologram);
				matrixStack.popPose();
				
			});
			
		}
		
	}
	
	public static void renderHologram(PoseStack matrixStack, MultiBufferSource buffer, float partialTick, BlockAndTintGetter blockAndTintGetter, Hologram hologram) {
		
	}
	
	public static void renderHologramChunk(PoseStack matrixStack, MultiBufferSource buffer, float partialTick, BlockAndTintGetter blockAndTintGetter) {
		
	}
	
	@SuppressWarnings("resource")
	public static void renderBlock(PoseStack matrixStack, MultiBufferSource buffer, float partialTick, BlockAndTintGetter blockAndTintGetter, BlockPos worldPosition, BlockState state, Optional<EntityData> blockentity) {
		
		VertexConsumer vertexBuffer = buffer.getBuffer(RenderType.translucent());
		BLOCK_RENDER_DISPATCHER.get().renderBatched(state, worldPosition, blockAndTintGetter, matrixStack, vertexBuffer, true, new Random(), EmptyModelData.INSTANCE);
		
		if (blockentity.isPresent() && state.getBlock() instanceof EntityBlock entityBlock) {
			BlockEntity blockEntityInstance = entityBlock.newBlockEntity(worldPosition, state);
			if (blockEntityInstance == null) return;
			blockEntityInstance.setLevel(Minecraft.getInstance().level);
			if (blockentity.get().nbt().get().isPresent()) blockEntityInstance.load(blockentity.get().nbt().get().get());
			BLOCK_ENTITY_RENDER_DISPATCHER.get().render(blockEntityInstance, partialTick, matrixStack, buffer);
		}
		
		
//		if (!this.hasChanged && this.activateRenderBuffer) {
//			// TODO RBO not working
//			matrixStackIn.push();
//			matrixStackIn.translate(posX, posY, posZ);
//			GL11.glCallList(this.listId);
//			matrixStackIn.pop();
//		} else {
//			
//			if (this.activateRenderBuffer) {
//				// TODO: RBO not working
//				if (this.listId != 0) GL11.glDeleteLists(this.listId, 1);
//				this.listId = GL11.glGenLists(1);
//				matrixStackIn.push();
//				GL11.glNewList(this.listId, GL11.GL_COMPILE);
//				renderBlockObjects(matrixStackIn, bufferIn, partialTicks);
//				GL11.glEndList();
//				matrixStackIn.pop();
//				this.hasChanged = false;
//			} else {
//				matrixStackIn.push();
//				matrixStackIn.translate(posX, posY, posZ);
//				renderBlockObjects(matrixStackIn, bufferIn, partialTicks);
//				matrixStackIn.pop();
//			}
//			
//		}
		
	}
	
}
