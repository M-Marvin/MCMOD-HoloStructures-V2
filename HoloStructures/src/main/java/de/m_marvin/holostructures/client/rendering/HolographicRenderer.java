package de.m_marvin.holostructures.client.rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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

import de.m_marvin.holostructures.client.holograms.Hologram;
import de.m_marvin.holostructures.client.rendering.HologramRenderer.CompiledChunkBuffer;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer.HologramRender.HolograghicChunk;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer.HologramRender.HolograghicChunk.HolographicChunkCompiled;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class HolographicRenderer {
	
	public static final Supplier<Camera> CAMERA = () -> Minecraft.getInstance().gameRenderer.getMainCamera();
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDERER = () -> Minecraft.getInstance().getBlockRenderer();
	
	protected static Queue<HologramUpdate> updateQueue = Queues.newArrayDeque();
	protected static Collection<HologramRender> hologramRenders = new ArrayList<HologramRender>();
	
	public static final record HologramUpdate(Hologram hologram, ChunkPos section, boolean dirty) {};
	
	public static class HologramRender {
		
		public Hologram hologram;
		public Collection<HolograghicChunk> renderChunks;
		
		public static class HolograghicChunk {
			
			public ChunkPos section;
			public Map<RenderType, BufferBuilder> renderBuilders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (renderType) -> {
				return new BufferBuilder(renderType.bufferSize());
			}));
			
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
			
			public BufferBuilder builder(RenderType renderLayer) {
				return this.renderBuilders.get(renderLayer);
			}
			
		}
		
	}
	
	
	
	
	
	
	
	
	public static void compileHolographicChunk(HologramRender hologram, HolograghicChunk chunk) {
		
		chunk.discardBuilders();
		
		RenderType.chunkBufferLayers().forEach((renderLayer) -> {
			BufferBuilder builder = chunk.builder(renderLayer);
			builder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
			
			hologram.hologram.get
			
			builder.end();
		});
		
	}
	
	public static void renderHolograms(PoseStack poseStack, Matrix4f projectionMatrix, RenderType renderLayer) {
		
		Vec3 cameraOffset = CAMERA.get().getPosition();
		poseStack.pushPose();
		poseStack.translate(-cameraOffset.x, -cameraOffset.y, -cameraOffset.z);
		
		ShaderInstance shader = initChunkRenderShader(poseStack, projectionMatrix);
		Uniform chunkOffset = shader.CHUNK_OFFSET;
		
		for (HologramRender hologramRender : hologramRenders) {
			BlockPos origin = hologramRender.hologram.getPosition();
			for (HolograghicChunk chunkRender : hologramRender.renderChunks) {
				if (chunkRender.compiled.get() != HolographicChunkCompiled.UNCOMPILED) {
					if (chunkOffset != null) {
						chunkOffset.set((float)(origin.getX() + chunkRender.section.x * 16), (float)(origin.getY()), (float)(origin.getZ() + chunkRender.section.z * 16));
						chunkOffset.upload();
					}
					VertexBuffer buffer = chunkRender.compiled.get().buffer(renderLayer);
					buffer.drawChunkLayer();
				}
			}
		}
		
		shader.clear();
		poseStack.popPose();
		
	}

	public static ShaderInstance initChunkRenderShader(PoseStack poseStack, Matrix4f projectionMatrix) {
		ShaderInstance shader = RenderSystem.getShader();
		shader.apply();
		if (shader.MODEL_VIEW_MATRIX != null) shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
		if (shader.PROJECTION_MATRIX != null) shader.PROJECTION_MATRIX.set(projectionMatrix);
		if (shader.COLOR_MODULATOR != null) shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		if (shader.FOG_START != null) shader.FOG_START.set(RenderSystem.getShaderFogStart());
		if (shader.FOG_END != null) shader.FOG_END.set(RenderSystem.getShaderFogEnd());
 		if (shader.FOG_COLOR != null) shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
 		if (shader.FOG_SHAPE != null) shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
 		if (shader.TEXTURE_MATRIX != null) shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
 		if (shader.GAME_TIME != null) shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
 		RenderSystem.setupShaderLights(shader);
 		return shader;	
	}
	
	
	
}
