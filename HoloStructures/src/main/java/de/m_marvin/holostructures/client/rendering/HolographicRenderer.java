package de.m_marvin.holostructures.client.rendering;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.holograms.Corner;
import de.m_marvin.holostructures.client.holograms.Hologram;
import de.m_marvin.holostructures.client.holograms.HologramChunk;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer.HologramRender.HolographicChunk;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer.HologramRender.HolographicChunk.HolographicChunkCompiled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HolographicRenderer {
	
	public static final Supplier<MultiBufferSource> BUFFER_SOURCE = () -> Minecraft.getInstance().renderBuffers().bufferSource();
	public static final Supplier<Camera> CAMERA = () -> Minecraft.getInstance().gameRenderer.getMainCamera();
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDERER = () -> Minecraft.getInstance().getBlockRenderer();
	
	protected static Queue<Map<RenderType, BufferBuilder>> emptyBuffers = Queues.newArrayDeque();
	protected static Int2ObjectMap<HologramRender> hologramRenders = new Int2ObjectArrayMap<>();
	protected static Queue<HologramUpdate> updateQueue = Queues.newArrayDeque();
	
	public static enum UpdateType {DIRTY_CHUNK, ADD_HOLOGRAM, DISCARD_HOLOGRAM}
	public static record HologramUpdate(Hologram hologram, long section, UpdateType flag) {};
	
	public static class HologramRender {
		
		public Hologram hologram;
		public Long2ObjectMap<HolographicChunk> renderChunks;
		
		public HologramRender(Hologram hologram) {
			this.hologram = hologram;
			this.renderChunks = new Long2ObjectArrayMap<>();
		}
		
		public static class HolographicChunk {
			
			boolean dirty;
			public ChunkPos pos;
			public Map<RenderType, BufferBuilder> renderBuilders;
			
			public HolographicChunk(ChunkPos pos) {
				this.pos = pos;
				this.dirty = true;
				this.renderBuilders = getFreshBuffers();
			}
			
			public AtomicReference<HolographicChunkCompiled> compiled = new AtomicReference<>(HolographicChunkCompiled.UNCOMPILED);
			
			public static class HolographicChunkCompiled {
				
				public static final HolographicChunkCompiled UNCOMPILED = new HolographicChunkCompiled();
				
				public Map<RenderType, VertexBuffer> renderBuffers;
				
				public void discardBuffers() {
					this.renderBuffers.values().forEach(VertexBuffer::close);
					this.renderBuffers.clear();
				}
				
				public VertexBuffer buffer(RenderType renderLayer) {
					return this.renderBuffers.get(renderLayer);
				}
				
				public void genBuffers() {
					this.renderBuffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (renderType) -> {
						return new VertexBuffer();
					}));
				}
				
			}
			
			public void discardBuilders() {
				this.renderBuilders.values().forEach(BufferBuilder::discard);
			}
			
			private Map<RenderType, BufferBuilder> getFreshBuffers() {
				if (emptyBuffers.size() > 0) {
					return emptyBuffers.poll();
				} else {
					return RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (renderType) -> {
						return new BufferBuilder(renderType.bufferSize());
					}));
				}
			}
			
			public void freeBuffers() {
				discardBuilders();
				emptyBuffers.add(renderBuilders);
				renderBuilders = null;
			}
			
			public BufferBuilder builder(RenderType renderLayer) {
				return this.renderBuilders.get(renderLayer);
			}
			
		}
		
	}
	
	
	@SubscribeEvent
	public static void onRenderLevelLast(RenderLevelStageEvent event) {
		if (event.getStage() == Stage.AFTER_SKY) {
			PoseStack poseStack = new PoseStack();
			translateToWorld(poseStack);
			
			processUpdateQueue();
			recompileDirtyChunks();

			poseStack.popPose();
			
		} else {
			PoseStack poseStack = event.getPoseStack();
			translateToWorld(poseStack);
			
			// WORK INPROGRESS: seprating diffrent render layers
			if (event.getStage() == Stage.AFTER_SOLID_BLOCKS) {
				renderHologramBounds(poseStack, BUFFER_SOURCE.get());
				//renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.solid());
			} else if (event.getStage() == Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
				//renderHolograms(poseStack, event.getProjectionMatrix(), RenderType. cutoutMipped());
			} else if (event.getStage() == Stage.AFTER_CUTOUT_BLOCKS) {
				//renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.cutout());
			} else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
				renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.translucent());
			} else if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
				//renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.tripwire());
			}
			
			poseStack.popPose();
		}
	}
	
	public static void markDirty(Hologram hologram, ChunkPos pos) {
		updateQueue.add(new HologramUpdate(hologram, pos.toLong(), UpdateType.DIRTY_CHUNK));
	}
	
	public static void addHologram(Hologram hologram) {
		updateQueue.add(new HologramUpdate(hologram, 0, UpdateType.ADD_HOLOGRAM));
	}
	
	public static void discardHologram(Hologram hologram) {
		updateQueue.add(new HologramUpdate(hologram, 0, UpdateType.DISCARD_HOLOGRAM));
	}
	
	protected static void processUpdateQueue() {
		
		if (updateQueue.size() > 0) {
			HologramUpdate updateRequest = updateQueue.poll();
			
			if (updateRequest.flag() == UpdateType.DIRTY_CHUNK) {
				HologramRender holoRender = hologramRenders.get(updateRequest.hologram().hashCode());
				if (holoRender != null) {
					HolographicChunk chunk = holoRender.renderChunks.get(updateRequest.section());
					if (chunk == null) {
						chunk = new HolographicChunk(new ChunkPos(updateRequest.section()));
						holoRender.renderChunks.put(updateRequest.section(), chunk);
					} else {
						chunk.dirty = true;
					}
				}
			} else if (updateRequest.flag() == UpdateType.ADD_HOLOGRAM) {
				HologramRender holoRender = new HologramRender(updateRequest.hologram());
				hologramRenders.put(updateRequest.hologram().hashCode(), holoRender);
			} else if (updateRequest.flag() == UpdateType.DISCARD_HOLOGRAM) {
				HologramRender holoRender = hologramRenders.remove(updateRequest.hologram().hashCode());
				holoRender.renderChunks.forEach((lp, chunk) -> {
					chunk.compiled.get().discardBuffers();
					chunk.freeBuffers();
				});
			}
		}
		
	}
	
	protected static void recompileDirtyChunks() {
		
		hologramRenders.forEach((hid, hologram) -> {
			hologram.renderChunks.forEach((pos, chunk) -> {
				if (chunk.dirty) {
					chunk.dirty = false;
					compileHolographicChunk(hologram, chunk);
				}
			});
		});
		
	}
	
	@SuppressWarnings("deprecation")
	protected static void compileHolographicChunk(HologramRender hologram, HolographicChunk chunk) {
		
		chunk.discardBuilders();
		
		PoseStack poseStack = new PoseStack();
		
		RenderType.chunkBufferLayers().forEach((renderLayer) -> {
			BufferBuilder builder = chunk.builder(renderLayer);
			builder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
			
			Optional<HologramChunk> holoChunk = hologram.hologram.getChunk(chunk.pos);
			if (holoChunk.isPresent()) {
				
				holoChunk.get().getSections().forEach((yi, section) -> {
					
					int posY = yi << 4;
					
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 16; y++) {
								BlockPos chunkPos = new BlockPos((chunk.pos.x << 4) + x, posY + y, (chunk.pos.z << 4) + z);
								BlockState state = holoChunk.get().getBlock(chunkPos);
								if (!state.isAir()) {
									poseStack.pushPose();
									poseStack.translate(x, posY + y, z);
									BLOCK_RENDERER.get().renderBatched(state, chunkPos.offset(chunkPos), hologram.hologram.level, poseStack, builder, false, hologram.hologram.level.random);
									poseStack.popPose();
								}
							}
						}
					}
					
				});
				
			}
			
			builder.end();
		});
		
		HolographicChunkCompiled compiled = new HolographicChunkCompiled();
		compiled.genBuffers();
		RenderType.chunkBufferLayers().forEach((renderLayer) -> {
			compiled.buffer(renderLayer).upload(chunk.builder(renderLayer));
		});
		
		HolographicChunkCompiled old = chunk.compiled.get();
		chunk.compiled.set(compiled);
		if (old != HolographicChunkCompiled.UNCOMPILED) old.discardBuffers();
		
	}
	
	protected static void renderHologramBounds(PoseStack poseStack, MultiBufferSource source) {
		
		hologramRenders.forEach((hid, hologram) -> {
			
			BlockPos position0 = hologram.hologram.getCornerWorldPosition(Corner.lowest_corner);
			BlockPos position1 = hologram.hologram.getCornerWorldPosition(Corner.highest_corner);
			
			poseStack.pushPose();
			
			VertexConsumer buffer = source.getBuffer(RenderType.lineStrip());
			
			int lx = position0.getX();
			int ly = position0.getY();
			int lz = position0.getZ();
			int hx = position1.getX();
			int hy = position1.getY();
			int hz = position1.getZ();
			
			renderHologramLine(poseStack, buffer, lx, ly, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, ly, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, ly, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, ly, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, ly, lz, 1, 1, 1, 1);
			
			renderHologramLine(poseStack, buffer, lx, hy, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, ly, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, hy, lz, 1, 1, 1, 1);
			
			renderHologramLine(poseStack, buffer, hx, hy, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, ly, lz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, hy, lz, 1, 1, 1, 1);
			
			renderHologramLine(poseStack, buffer, hx, hy, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, ly, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, hx, hy, hz, 1, 1, 1, 1);
			
			renderHologramLine(poseStack, buffer, lx, hy, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, ly, hz, 1, 1, 1, 1);
			renderHologramLine(poseStack, buffer, lx, hy, hz, 1, 1, 1, 1);
			
			renderHologramLine(poseStack, buffer, lx, hy, lz, 1, 1, 1, 1);
			
			poseStack.popPose();
			
		});
		
	}
	
	protected static void renderHologramLine(PoseStack poseStack, VertexConsumer buffer, float fx, float fy, float fz, float r, float g, float b, float a) {
		buffer.vertex(poseStack.last().pose(), fx, fy, fz).color(r, g, b, a).normal(poseStack.last().normal(), 1, 1, 1).endVertex();
	}
	
	protected static void renderHolograms(PoseStack poseStack, Matrix4f projectionMatrix, RenderType renderLayer) {
		
		ShaderInstance shader = initChunkRenderShader(poseStack, projectionMatrix);
		Uniform chunkOffset = shader.CHUNK_OFFSET;
		
		for (HologramRender hologramRender : hologramRenders.values()) {
			BlockPos origin = hologramRender.hologram.getPosition();
			for (HolographicChunk chunkRender : hologramRender.renderChunks.values()) {
				if (chunkRender.compiled.get() != HolographicChunkCompiled.UNCOMPILED) {
					if (chunkOffset != null) {
						chunkOffset.set((float)(origin.getX() + chunkRender.pos.x * 16), (float)(origin.getY()), (float)(origin.getZ() + chunkRender.pos.z * 16));
						chunkOffset.upload();
					}
					VertexBuffer buffer = chunkRender.compiled.get().buffer(renderLayer);
					buffer.drawChunkLayer();
				}
			}
		}
		
		shader.clear();
		
	}
	
	protected static void translateToWorld(PoseStack poseStack) {
		Vec3 cameraOffset = CAMERA.get().getPosition();
		poseStack.pushPose();
		poseStack.translate(-cameraOffset.x, -cameraOffset.y, -cameraOffset.z);
	}

	protected static ShaderInstance initChunkRenderShader(PoseStack poseStack, Matrix4f projectionMatrix) {
		ShaderInstance shader = RenderSystem.getShader();
		shader.apply();
		if (shader.MODEL_VIEW_MATRIX != null) {
			shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
			shader.MODEL_VIEW_MATRIX.upload();
		}
		if (shader.PROJECTION_MATRIX != null) {
			shader.PROJECTION_MATRIX.set(projectionMatrix);
			shader.PROJECTION_MATRIX.upload();
		}
		if (shader.COLOR_MODULATOR != null) {
			shader.COLOR_MODULATOR.set(new float[] {1, 1, 1, 0.2F});
			shader.COLOR_MODULATOR.upload();
		}
		if (shader.FOG_START != null) {
			shader.FOG_START.set(RenderSystem.getShaderFogStart());
			shader.FOG_START.upload();
		}
		if (shader.FOG_END != null) {
			shader.FOG_END.set(RenderSystem.getShaderFogEnd());
			shader.FOG_END.upload();
		}
 		if (shader.FOG_COLOR != null) {
 			shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
			shader.FOG_COLOR.upload();
 		}
 		if (shader.FOG_SHAPE != null) {
 			shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
			shader.FOG_SHAPE.upload();
 		}
 		if (shader.TEXTURE_MATRIX != null) {
 			shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
			shader.TEXTURE_MATRIX.upload();
 		}
 		if (shader.GAME_TIME != null) {
 			shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
			shader.GAME_TIME.upload();
 		}
 		RenderSystem.setupShaderLights(shader);
 		return shader;	
	}
	
}
