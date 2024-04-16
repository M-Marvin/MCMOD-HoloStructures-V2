package de.m_marvin.holostructures.client.rendering;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import de.m_marvin.holostructures.client.holograms.BlockHoloState;
import de.m_marvin.holostructures.client.holograms.Hologram;
import de.m_marvin.holostructures.client.rendering.HologramBufferContainer.HolographicBufferSource;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
			
			public boolean hasBufferFor(BlockHoloState holoState, RenderType renderLayer) {
				return this.renderBuffers.get(holoState).containsKey(renderLayer);
			}
			
			public VertexBuffer bindBuffer(BlockHoloState holoState, RenderType renderLayer) {
				VertexBuffer buffer = this.renderBuffers.get(holoState).get(renderLayer);
				buffer.bind();
				return buffer;
			}
			
			public void genBuffers(Collection<RenderType> renderLayers) {
				this.renderBuffers = BlockHoloState.renderedStates().stream().collect(Collectors.toMap((holoState) -> holoState, (holoState) -> {
					return renderLayers.stream().collect(Collectors.toMap((renderType) -> renderType, (renderType) -> {
						return new VertexBuffer(VertexBuffer.Usage.STATIC);
					}));
				}));
			}
			
		}

		public static final int BUFFER_BUILDER_CAPACITY = 786432; // copied this number from the RenderBuffers class
		
		public ChunkPos pos;
		public boolean dirty;
		public AtomicReference<HolographicChunkCompiled> compiled = new AtomicReference<>(HolographicChunkCompiled.UNCOMPILED);
		public HologramBufferContainer bufferContainer;
		
		public HolographicChunk(ChunkPos pos) {
			this.pos = pos;
			this.dirty = true;
			this.bufferContainer = new HologramBufferContainer(() -> {
				return new HolographicBufferSource(renderType -> new BufferBuilder(BUFFER_BUILDER_CAPACITY));
			});
		}
		
		public void discardBuffers() {
			this.bufferContainer.discard();
		}
		
	}
	
	public Hologram hologram;
	public Long2ObjectMap<HolographicChunk> renderChunks;
	
	public HologramRender(Hologram hologram) {
		this.hologram = hologram;
		this.renderChunks = new Long2ObjectArrayMap<>();
	}
	
}