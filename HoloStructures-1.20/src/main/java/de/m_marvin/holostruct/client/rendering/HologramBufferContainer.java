package de.m_marvin.holostruct.client.rendering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;

import de.m_marvin.holostruct.client.holograms.BlockHoloState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class HologramBufferContainer {

	private static Set<RenderType> allocatedTypes = new HashSet<>();
	private Map<BlockHoloState, HolographicBufferSource> renderBuilders;
	
	public HologramBufferContainer(Supplier<HolographicBufferSource> bufferSourceSource) {
		this.renderBuilders = new HashMap<>();
		for (BlockHoloState holoState : BlockHoloState.values()) {
			this.renderBuilders.put(holoState, bufferSourceSource.get());
		}
	}
	
	public HolographicBufferSource getBufferSource(BlockHoloState holoState) {
		return this.renderBuilders.get(holoState);
	}

	public static synchronized Set<RenderType> getAlocatedRenderTypes() {
		return allocatedTypes;
	}
	
	public static class HolographicBufferSource implements MultiBufferSource {
		
		private Map<RenderType, BufferBuilder> bufferBuilders;
		private Function<RenderType, BufferBuilder> allocator;
		
		public HolographicBufferSource(Function<RenderType, BufferBuilder> bufferAllocator) {
			this.bufferBuilders = new HashMap<>();
			this.allocator = bufferAllocator;
		}
		
		@Override
		public BufferBuilder getBuffer(RenderType pRenderType) {
			BufferBuilder buffer = getBufferRaw(pRenderType);
			if (!buffer.building()) buffer.begin(pRenderType.mode(), pRenderType.format());
			return buffer;
		}
		
		public BufferBuilder getBufferRaw(RenderType pRenderType) {
			BufferBuilder buffer = this.bufferBuilders.get(pRenderType);
			if (buffer == null) {
				buffer = this.allocator.apply(pRenderType);
				this.bufferBuilders.put(pRenderType, buffer);
				if (!allocatedTypes.contains(pRenderType)) {
					synchronized (this) {
						allocatedTypes.add(pRenderType);
					}
				};
			}
			return buffer;
		}
		
		public RenderedBuffer endBatch(RenderType pRenderType) {
			BufferBuilder builder = getBuffer(pRenderType);
			builder.setQuadSorting(RenderSystem.getVertexSorting());
			return builder.end();
		}
		
		public void discard() {
			this.bufferBuilders.values().forEach(BufferBuilder::discard);
			this.bufferBuilders.clear();
		}
		
	}

	public void discard() {
		this.renderBuilders.values().forEach(HolographicBufferSource::discard);
		this.renderBuilders.clear();
	}
	
}