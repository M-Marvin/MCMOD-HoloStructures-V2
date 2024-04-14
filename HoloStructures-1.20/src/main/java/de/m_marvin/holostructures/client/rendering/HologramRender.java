package de.m_marvin.holostructures.client.rendering;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import de.m_marvin.holostructures.client.holograms.BlockHoloState;
import de.m_marvin.holostructures.client.holograms.Hologram;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;

public class HologramRender {

	public static class HolographicChunk {

		public static class HolographicChunkCompiled {
			
			public static final HolographicChunkCompiled UNCOMPILED = new HolographicChunkCompiled();
			
			public Map<BlockHoloState, Map<RenderType, VertexBuffer>> renderBuffers;
			
			public void discardBuffers() {
				if (this.renderBuffers == null) return;
				this.renderBuffers.values().forEach((map) -> map.values().forEach(VertexBuffer::close));
				this.renderBuffers.clear();
			}
			
			public VertexBuffer bindBuffer(BlockHoloState holoState, RenderType renderLayer) {
				VertexBuffer buffer = this.renderBuffers.get(holoState).get(renderLayer);
				buffer.bind();
				return buffer;
			}
			
			public void genBuffers() {
				this.renderBuffers = BlockHoloState.renderedStates().stream().collect(Collectors.toMap((holoState) -> holoState, (holoState) -> {
					return RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (renderType) -> {
						return new VertexBuffer(VertexBuffer.Usage.STATIC);
					}));
				}));
			}
			
		}
		
//		protected static Queue<Map<BlockHoloState, Map<RenderType, BufferBuilder>>> emptyBuffers = Queues.newArrayDeque();
		
		public ChunkPos pos;
//		public Map<BlockHoloState, Map<RenderType, BufferBuilder>> renderBuilders;
		public boolean dirty;
		public AtomicReference<HolographicChunkCompiled> compiled = new AtomicReference<>(HolographicChunkCompiled.UNCOMPILED);
		
		public HolographicChunk(ChunkPos pos) {
			this.pos = pos;
			this.dirty = true;
//			this.renderBuilders = getFreshBuffers();
		}
		
//		private Map<BlockHoloState, Map<RenderType, BufferBuilder>> getFreshBuffers() {
//			if (emptyBuffers.size() > 0) {
//				return emptyBuffers.poll();
//			} else {
//				return BlockHoloState.renderedStates().stream().collect(Collectors.toMap((holoState) -> holoState, (holoState) -> {
//					return RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderLayer) -> renderLayer, (renderLayer) -> {
//						return new BufferBuilder(ModRenderType.hologramBlocks().bufferSize());
//					}));
//				}));
//			}
//		}

//		public void discardBuilders() {
//			this.renderBuilders.values().forEach((map) -> map.values().forEach(BufferBuilder::discard));
//		}
		
//		public void freeBuffers() {
//			discardBuilders();
//			emptyBuffers.add(renderBuilders);
//			renderBuilders = null;
//		}
		
//		public BufferBuilder builder(BlockHoloState holoState, RenderType renderLayer) {
//			return this.renderBuilders.get(holoState).get(renderLayer);
//		}
		
	}
	
	public Hologram hologram;
	public Long2ObjectMap<HolographicChunk> renderChunks;
	
	public HologramRender(Hologram hologram) {
		this.hologram = hologram;
		this.renderChunks = new Long2ObjectArrayMap<>();
	}
	
}