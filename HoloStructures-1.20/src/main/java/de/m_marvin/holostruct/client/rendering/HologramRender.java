package de.m_marvin.holostruct.client.rendering;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import de.m_marvin.holostruct.client.holograms.BlockHoloState;
import de.m_marvin.holostruct.client.holograms.Hologram;
import de.m_marvin.holostruct.client.rendering.HologramBufferContainer.HolographicBufferSource;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;

public class HologramRender {

	public static class HolographicChunk {
		
		public static class HolographicSectionCompiled {
			
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

		public static final int BUFFER_BUILDER_CAPACITY = 786432;
		
		public ChunkPos pos;
		public IntList dirty = new IntArrayList();
		public Int2ObjectMap<HolographicSectionCompiled> compiled = new Int2ObjectArrayMap<>();
		public HologramBufferContainer bufferContainer;
		public Boolean bufferClaimed = false;
		
		public HolographicChunk(ChunkPos pos) {
			this.pos = pos;
			this.bufferContainer = new HologramBufferContainer(() -> {
				return new HolographicBufferSource(renderType -> new BufferBuilder(BUFFER_BUILDER_CAPACITY));
			});
		}
		
		public void claimBuffers() {
			try {
				while (this.bufferClaimed) {
					synchronized (this.bufferContainer) {
						this.bufferContainer.wait();
					}
				}
				this.bufferClaimed = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void releaseBuffers() {
			this.bufferClaimed = false;
			synchronized (this.bufferContainer) {
				this.bufferContainer.notify();	
			}
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