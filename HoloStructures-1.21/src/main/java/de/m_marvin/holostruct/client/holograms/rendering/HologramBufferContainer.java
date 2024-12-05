package de.m_marvin.holostruct.client.holograms.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

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
		
		// ByteBufferBuilders are reused between draw calls
		private Map<RenderType, ByteBufferBuilder> byteBufferBuilders;
		// BufferBuilders are only used once, and get deleted after each draw call (see endBatch)
		private Map<RenderType, BufferBuilder> bufferBuilders;
		// Allocator for constructing the ByteBufferBuilders
		private Function<RenderType, ByteBufferBuilder> allocator;
		
		public HolographicBufferSource(Function<RenderType, ByteBufferBuilder> bufferAllocator) {
			this.byteBufferBuilders = new HashMap<>();
			this.bufferBuilders = new HashMap<>();
			this.allocator = bufferAllocator;
		}
		
		@Override
		public BufferBuilder getBuffer(RenderType pRenderType) {
			BufferBuilder buffer = this.bufferBuilders.get(pRenderType);
			if (buffer == null) {
				ByteBufferBuilder byteBuffer = this.byteBufferBuilders.get(pRenderType);
				if (byteBuffer == null) {
					byteBuffer = this.allocator.apply(pRenderType);
					this.byteBufferBuilders.put(pRenderType, byteBuffer);
				}
				
				buffer = new BufferBuilder(byteBuffer, pRenderType.mode(), pRenderType.format());
				this.bufferBuilders.put(pRenderType, buffer);
				if (!getAlocatedRenderTypes().contains(pRenderType)) getAlocatedRenderTypes().add(pRenderType);
			}
			return buffer;
		}
		
		public Set<RenderType> localyAllocated() {
			return this.bufferBuilders.keySet();
		}
		
		public MeshData endBatch(RenderType pRenderType) {
			try {
				BufferBuilder builder = getBuffer(pRenderType);
				return endBatch(pRenderType, builder);
			} catch (IllegalStateException e) {
				if (e.getMessage().equals("Not building!")) return null; // There is no "isBuilding()" method for some reason ...
				throw e;
			}
		}
		
		public MeshData endBatch(RenderType renderType, BufferBuilder builder) {
			MeshData meshdata = builder.build();
			this.bufferBuilders.remove(renderType, builder);
			if (meshdata != null) {
				if (renderType.sortOnUpload()) {
					ByteBufferBuilder bytebufferbuilder = this.byteBufferBuilders.get(renderType);
					meshdata.sortQuads(bytebufferbuilder, RenderSystem.getProjectionType().vertexSorting());
				}
			}
			return meshdata;
		}
		
		public void discard() {
			this.byteBufferBuilders.values().forEach(ByteBufferBuilder::discard);
			this.byteBufferBuilders.clear();
			this.bufferBuilders.clear();
		}
		
	}

	public void discard() {
		this.renderBuilders.values().forEach(HolographicBufferSource::discard);
		this.renderBuilders.clear();
	}
	
}
