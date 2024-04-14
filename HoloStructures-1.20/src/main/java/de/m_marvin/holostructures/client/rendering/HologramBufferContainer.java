package de.m_marvin.holostructures.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostructures.client.holograms.BlockHoloState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class HologramBufferContainer {

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
	
	public static class HolographicBufferSource implements MultiBufferSource {
		
		private Map<RenderType, BufferBuilder> bufferBuilders;
		private BufferBuilder fallback;
		
		public HolographicBufferSource(Map<RenderType, BufferBuilder> builders, BufferBuilder fallback) {
			this.bufferBuilders = builders;
			this.fallback = fallback;
		}
		
		@Override
		public VertexConsumer getBuffer(RenderType pRenderType) {
			BufferBuilder buffer = getBufferRaw(pRenderType);
			buffer.begin(pRenderType.mode(), pRenderType.format());
			return buffer;
		}
		
		public BufferBuilder getBufferRaw(RenderType pRenderType) {
			return this.bufferBuilders.getOrDefault(pRenderType, this.fallback);
		}
		
		public BufferBuilder.RenderedBuffer endBatch(RenderType pRenderType) {
			return getBufferRaw(pRenderType).end();
		}
		
	}
	
}
