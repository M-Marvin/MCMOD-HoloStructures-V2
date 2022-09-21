package de.m_marvin.holostructures.client.rendering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HologramRenderer {
	
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDER_DISPATCHER = () -> Minecraft.getInstance().getBlockRenderer();
	public static final Supplier<BlockEntityRenderDispatcher> BLOCK_ENTITY_RENDER_DISPATCHER = () -> Minecraft.getInstance().getBlockEntityRenderDispatcher();
	public static final Supplier<EntityRenderDispatcher> ENTITY_RENDER_DISPATCHER = () -> Minecraft.getInstance().getEntityRenderDispatcher();
	
	protected static Queue<ChunkBuffer> freeBuffers = Queues.newArrayDeque();
	protected static Queue<ChunkPos> dirtyChunks = Queues.newArrayDeque();
	protected static Queue<ChunkPos> discardedChunks = Queues.newArrayDeque();
	protected static Queue<ChunkBuffer> clearedBuffers = Queues.newArrayDeque();
	protected static Map<ChunkPos, ChunkBuffer> chunkCache = new HashMap<>();
	
	public static class ChunkBuffer {
		private boolean dirty = true;
		private final Map<RenderType, BufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((p_108845_) -> {
			return p_108845_;
		}, (p_108843_) -> {
			return new BufferBuilder(p_108843_.bufferSize());
		}));
	
		public BufferBuilder builder(RenderType p_108840_) {
			return this.builders.get(p_108840_);
		}
		public void clearAll() {
			this.builders.values().forEach(BufferBuilder::clear);
		}
		public void discardAll() {
			this.builders.values().forEach(BufferBuilder::discard);
		}
		public boolean isDirty() {
			return dirty;
		}
		public void setDirty(boolean dirty) {
			this.dirty = dirty;
		}
	}
	
	@SubscribeEvent
	public static void onLevelRenderLast(RenderLevelStageEvent event) {
		if (event.getStage() == Stage.AFTER_SKY) {
			manageBuffers();
		} else if (event.getStage() == Stage.AFTER_SOLID_BLOCKS) {
			render(event.getPoseStack(), RenderType.solid(), event.getPartialTick());
		} else if (event.getStage() == Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
			render(event.getPoseStack(), RenderType.cutoutMipped(), event.getPartialTick());
		} else if (event.getStage() == Stage.AFTER_CUTOUT_BLOCKS) {
			render(event.getPoseStack(), RenderType.cutout(), event.getPartialTick());
		} else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
			render(event.getPoseStack(), RenderType.translucent(), event.getPartialTick());
		} else if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
			render(event.getPoseStack(), RenderType.tripwire(), event.getPartialTick());
		}
	}
	
	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		ChunkPos chunk = event.getChunk().getPos();
		if (!dirtyChunks.contains(chunk)) dirtyChunks.add(chunk);
	}
	
	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		ChunkPos chunk = event.getChunk().getPos();
		if (!discardedChunks.contains(chunk)) discardedChunks.add(chunk);
	}
	
	@SubscribeEvent
	public static void onWorldClose(WorldEvent.Unload event) {
		cleanup();
	}
	
	public static void cleanup() {
		chunkCache.forEach((chunk, buffer) -> freeBuffer(buffer));
		chunkCache.clear();
		dirtyChunks.clear();
		discardedChunks.clear();
		clearedBuffers.clear();
	}
	
	public static ChunkBuffer getFreeBuffer() {
		if (freeBuffers.size() > 0) {
			return freeBuffers.poll();
		} else {
			return new ChunkBuffer();
		}
	}
	
	public static void freeBuffer(ChunkBuffer buffer) {
		buffer.discardAll();
		freeBuffers.add(buffer);
	}
	
	// TODO: Eventuell überschüsssige buffer löschen
	
	public static void manageBuffers() {
		if (clearedBuffers.size() > 0) {
			clearedBuffers.poll().setDirty(false);
			System.out.println("FINISH BUFFER " + chunkCache.size() + "/" + freeBuffers.size());
		}
		if (dirtyChunks.size() > 0) {
			ChunkPos chunkToUpdate = dirtyChunks.poll();
			if (chunkCache.containsKey(chunkToUpdate)) {
				ChunkBuffer buffer = chunkCache.get(chunkToUpdate);
				buffer.clearAll();
				buffer.setDirty(true);
				clearedBuffers.add(buffer);
				System.out.println("UPDATE BUFFER " + chunkCache.size() + "/" + freeBuffers.size());
			} else {
				ChunkBuffer buffer = getFreeBuffer();
				chunkCache.put(chunkToUpdate, buffer);
				clearedBuffers.add(buffer);
				System.out.println("UPDATE BUFFER " + chunkCache.size() + "/" + freeBuffers.size());
			}
		}
		if (discardedChunks.size() > 0) {
			ChunkPos chunkToDiscard = discardedChunks.poll();
			if (chunkCache.containsKey(chunkToDiscard)) {
				freeBuffer(chunkCache.remove(chunkToDiscard));
				System.out.println("DISCARDED BUFFER " + chunkCache.size() + "/" + freeBuffers.size());
			}
		}
	}
	
	public static void render(PoseStack matrixStack, RenderType renderLayer, float partialTick) {
		
		@SuppressWarnings("resource")
		Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		matrixStack.pushPose();
		matrixStack.translate(-offset.x, -offset.y, -offset.z);
			
		chunkCache.forEach((chunkPos, buffer) -> {
			renderHoloChunk(matrixStack, renderLayer, partialTick, buffer, chunkPos);
		});
		
		matrixStack.popPose();
		
	}
	
	public static void renderHoloChunk(PoseStack matrixStack, RenderType renderLayer, float partialTick, ChunkBuffer bufferSource, ChunkPos chunkPos) {
		
		if (bufferSource.isDirty()) {
			
			// Rerender content
			BlockAndTintGetter level = ClientHandler.getInstance().getLevelAccessor().get().getLevelGetter();
			matrixStack.pushPose();
			matrixStack.translate(chunkPos.x * 16, 0, chunkPos.z * 16);
			renderHologramChunk(matrixStack, bufferSource.builder(renderLayer), partialTick, level, chunkPos);
			matrixStack.popPose();
			
		} else {
			
			// Resuse buffer
			
		}
		
	}
	
	public static void renderHologramChunk(PoseStack matrixStack, VertexConsumer buffer, float partialTick, BlockAndTintGetter blockAndTintGetter, ChunkPos chunkPos) {
		
		// TODO: Besserer itterator
		
//		for (int x = 0; x < 16; x++) {
//			for (int z = 0; z < 16; z++) {
//				for (int y = -64; y < 320; y++) {
//					BlockPos worldPos = new BlockPos(chunkPos.x * 16 + x, y, chunkPos.z * 16 + z);
//					List<BlockState> state = ClientHandler.getInstance().getHolograms().getHologramBlocksAt(worldPos);
//					List<Optional<Blueprint.EntityData>> blockentities = ClientHandler.getInstance().getHolograms().getHologramBlockentitesAt(worldPos);
//					
//					matrixStack.pushPose();
//					matrixStack.translate(x, y, z);
//					for (int i = 0; i < state.size(); i++) {
//						renderBlock(matrixStack, buffer, partialTick, blockAndTintGetter, worldPos, state.get(i), blockentities.get(i));
//					}
//					matrixStack.popPose();
//				}
//			}
//		}
		
	}
	
	public static void renderBlock(PoseStack matrixStack, VertexConsumer vertexBuffer, float partialTick, BlockAndTintGetter blockAndTintGetter, BlockPos worldPosition, BlockState state, Optional<EntityData> blockentity) {
		
		BLOCK_RENDER_DISPATCHER.get().renderBatched(state, worldPosition, blockAndTintGetter, matrixStack, vertexBuffer, true, new Random(), EmptyModelData.INSTANCE);
		
//		if (blockentity.isPresent() && state.getBlock() instanceof EntityBlock entityBlock) {
//			BlockEntity blockEntityInstance = entityBlock.newBlockEntity(worldPosition, state);
//			if (blockEntityInstance == null) return;
//			blockEntityInstance.setLevel(Minecraft.getInstance().level);
//			if (blockentity.get().nbt().get().isPresent()) blockEntityInstance.load(blockentity.get().nbt().get().get());
//			BLOCK_ENTITY_RENDER_DISPATCHER.get().render(blockEntityInstance, partialTick, matrixStack, buffer);
//		}
		
	}
	
}
