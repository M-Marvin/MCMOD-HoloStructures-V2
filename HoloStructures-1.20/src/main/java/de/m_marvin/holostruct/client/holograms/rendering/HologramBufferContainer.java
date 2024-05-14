package de.m_marvin.holostruct.client.holograms.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;

import de.m_marvin.holostruct.client.holograms.BlockHoloState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * The buffer contains holds the multi buffer sources for each {@link BlockHoloState}.
 * @author Marvin Koehler
 */
public class HologramBufferContainer {

	private static Set<RenderType> allocatedTypes = new CopyOnWriteArraySet<>();
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
	
	/**
	 * The holographic buffer source holds one buffer source for each {@link RenderType}
	 * @author Marvin Koehler
	 */
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
				if (!getAlocatedRenderTypes().contains(pRenderType)) getAlocatedRenderTypes().add(pRenderType);
			}
			return buffer;
		}
		
		public Set<RenderType> localyAllocated() {
			return this.bufferBuilders.keySet();
		}
		
		public RenderedBuffer endBatch(RenderType pRenderType) {
			BufferBuilder builder = getBuffer(pRenderType);
			return builder.end();
		}
		
		public SortState makeSortState(RenderType pRenderType) {
			BufferBuilder builder = getBuffer(pRenderType);
			return builder.getSortState();
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
