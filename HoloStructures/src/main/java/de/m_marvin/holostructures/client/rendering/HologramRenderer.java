package de.m_marvin.holostructures.client.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
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
	public static final Supplier<ChunkRenderDispatcher> CHUNK_RENDER_DISPATCHER = () -> Minecraft.getInstance().levelRenderer.getChunkRenderDispatcher();
	
	protected static Queue<ChunkBuffer> freeBuffers = Queues.newArrayDeque();
	protected static Queue<ChunkPos> dirtyChunks = Queues.newArrayDeque();
	protected static Queue<ChunkPos> discardedChunks = Queues.newArrayDeque();
	protected static Queue<ChunkBuffer> clearedBuffers = Queues.newArrayDeque();
	protected static Map<ChunkPos, ChunkBuffer> chunkCache = new HashMap<>();
	
	public static class CompiledChunkBuffer {
		public static final CompiledChunkBuffer UNCOMPILEDED = new CompiledChunkBuffer();
		public Map<RenderType, VertexBuffer> buffers = null;
		public VertexBuffer buffer(RenderType renderLayer) {
			return this.buffers.get(renderLayer);
		}
		public void discard() {
			this.buffers.values().forEach(VertexBuffer::close);
		}
	}
	
	public static class ChunkBuffer {
		private boolean dirty = true;
		public AtomicReference<CompiledChunkBuffer> compiled = new AtomicReference<CompiledChunkBuffer>(CompiledChunkBuffer.UNCOMPILEDED);
		private final Map<RenderType, BufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((p_108845_) -> {
			return p_108845_;
		}, (p_108843_) -> {
			return new BufferBuilder(p_108843_.bufferSize());
		}));
		private Map<RenderType, VertexBuffer> buffers = null;
		
		public BufferBuilder builder(RenderType renderLayer) {
			return this.builders.get(renderLayer);
		}
		public VertexBuffer buffer(RenderType renderLayer) {
			return this.buffers.get(renderLayer);
		}
		public void setupBuffers() {
			this.buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((p_108845_) -> {
				return p_108845_;
			}, (p_108843_) -> {
				return new VertexBuffer();
			}));
		}
		public void clearAll() {
			//System.out.println("TEST");
			this.buffers.values().forEach(VertexBuffer::close);
			this.buffers.clear();
			this.builders.values().forEach(BufferBuilder::clear);
		}
		public void discardAll() {
			//System.out.println("TEST");
			this.buffers.values().forEach(VertexBuffer::close);
			this.buffers.clear();
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
//		chunkCache.forEach((chunk, buffer) -> freeBuffer(buffer));
//		chunkCache.clear();
//		dirtyChunks.clear();
//		discardedChunks.clear();
//		clearedBuffers.clear();
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
			ChunkBuffer buffer = clearedBuffers.poll();
			buffer.setDirty(false);
			
			
			
			//List<CompletableFuture<Void>> list = new ArrayList<>();
			for (RenderType renderLayer : RenderType.chunkBufferLayers()) {
				BufferBuilder vertexBuilder = buffer.builder(renderLayer);
				VertexBuffer vertexBuffer = buffer.buffer(renderLayer);
				if (vertexBuffer == null || vertexBuilder == null) continue;
				vertexBuffer.upload(vertexBuilder);
				//list.add(CHUNK_RENDER_DISPATCHER.get().uploadChunkLayer(vertexBuilder, vertexBuffer));
			}
			
			CompiledChunkBuffer compiledBuffer = new CompiledChunkBuffer();
			compiledBuffer.buffers = buffer.buffers;
			buffer.compiled.set(compiledBuffer);
			
//			Util.sequenceFailFast(list).handle((voidList, exception) -> {
//				if (exception != null && !(exception instanceof CancellationException) && !(exception instanceof InterruptedException)) {
//					CrashReport crashreport = CrashReport.forThrowable(exception, "Rendering hologram chunk");
//					Minecraft.getInstance().delayCrash(() -> {
//						return crashreport;
//					});
//				}
//				CompiledChunkBuffer compiledBuffer = new CompiledChunkBuffer();
//				compiledBuffer.buffers = buffer.buffers;
//				buffer.compiled.set(compiledBuffer);
//				return null;
//			});
			
		}
		if (dirtyChunks.size() > 0) {
			ChunkPos chunkToUpdate = dirtyChunks.poll();
			if (chunkCache.containsKey(chunkToUpdate)) {
				ChunkBuffer buffer = chunkCache.get(chunkToUpdate);
				buffer.clearAll();
				buffer.setDirty(true);
				buffer.setupBuffers();
				clearedBuffers.add(buffer);
			} else {
				ChunkBuffer buffer = getFreeBuffer();
				buffer.setDirty(true);
				buffer.setupBuffers();
				chunkCache.put(chunkToUpdate, buffer);
				clearedBuffers.add(buffer);
			}
		}
		if (discardedChunks.size() > 0) {
			ChunkPos chunkToDiscard = discardedChunks.poll();
			if (chunkCache.containsKey(chunkToDiscard)) {
				ChunkBuffer buffer = chunkCache.remove(chunkToDiscard);
				freeBuffer(buffer);
				buffer.compiled.get().discard();
			}
		}
	}
	
	public static void render(PoseStack matrixStack, RenderType renderLayer, float partialTick) {

		chunkCache.forEach((chunkPos, buffer) -> {
			renderHoloChunk(matrixStack, renderLayer, partialTick, buffer, chunkPos);
		});
		
	}
	
	public static void renderHoloChunk(PoseStack matrixStack, RenderType renderLayer, float partialTick, ChunkBuffer bufferSource, ChunkPos chunkPos) {
		
		if (bufferSource.isDirty()) {
			
			// Rerender content
			BlockAndTintGetter level = ClientHandler.getInstance().getLevelAccessor().get().getLevelGetter();
			//matrixStack = new PoseStack();
		//matrixStack.setIdentity();
			
			//matrixStack.pushPose();
			//matrixStack.translate(chunkPos.x * 16, 0, chunkPos.z * 16);
			BufferBuilder bufferBuilder = bufferSource.builder(renderLayer);
			bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
			//VertexConsumer bufferBuilder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderLayer);
			renderHologramChunk(matrixStack, bufferBuilder, partialTick, level, chunkPos);
			bufferBuilder.end();
			//matrixStack.popPose();
			
		} else {

			@SuppressWarnings("resource")
			Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			matrixStack.pushPose();
			matrixStack.translate(-offset.x, -offset.y, -offset.z);
			matrixStack.translate(chunkPos.x * 16, 0, chunkPos.z * 16);
			
//			 ShaderInstance shaderinstance = RenderSystem.getShader();
//			 shaderinstance.apply();
			

//		      if (shaderinstance.MODEL_VIEW_MATRIX != null) {
//		          shaderinstance.MODEL_VIEW_MATRIX.set(matrixStack.last().pose());
//		       }
//		      
////		      if (shaderinstance.PROJECTION_MATRIX != null) {
////		         shaderinstance.PROJECTION_MATRIX.set(RenderRystem);
////		      }
//
//		      if (shaderinstance.COLOR_MODULATOR != null) {
//		        shaderinstance.COLOR_MODULATOR.set(new float[] {1, 1, 1, 1});
//		      }
//
//		      if (shaderinstance.FOG_START != null) {
//		         shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
//		      }
//
//		      if (shaderinstance.FOG_END != null) {
//		    	  //System.out.println(RenderSystem.getShaderFogEnd());
//		         shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
//		      }
//
//		      if (shaderinstance.FOG_COLOR != null) {
//		        shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
//		      }
//
//		      if (shaderinstance.FOG_SHAPE != null) {
//		         shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
//		      }
//
//		      if (shaderinstance.TEXTURE_MATRIX != null) {
//		         shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
//		      }
//		      
//		      if (shaderinstance.GAME_TIME != null) {
//		          shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
//		       }
		      
		     // RenderSystem.setupShaderLights(shaderinstance);
		      
//		      Uniform uniform = shaderinstance.CHUNK_OFFSET;
			     
//	            if (uniform != null) {
	            	//System.out.println(blockpos);
	                //uniform.set((float)(0), (float)(0), (float)(0));
	                //uniform.upload();
//	             }
		      
		      //Uniform uniform = shaderinstance.CHUNK_OFFSET;
				  // uniform.upload();
				
			// Resuse buffer
			if (bufferSource.compiled.get() != CompiledChunkBuffer.UNCOMPILEDED) {
				
				bufferSource.compiled.get().buffer(renderLayer).drawChunkLayer();
				
			}

//			shaderinstance.clear();
			
			matrixStack.popPose();
			
		 
		}
		
	}
	
	public static void renderHologramChunk(PoseStack matrixStack, VertexConsumer buffer, float partialTick, BlockAndTintGetter blockAndTintGetter, ChunkPos chunkPos) {
		
		// TODO: Besserer itterator
		renderBlock(matrixStack, buffer, partialTick, blockAndTintGetter, new BlockPos(chunkPos.x * 16, 0, chunkPos.z * 16), Blocks.STONE.defaultBlockState(), Optional.empty());
		
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
