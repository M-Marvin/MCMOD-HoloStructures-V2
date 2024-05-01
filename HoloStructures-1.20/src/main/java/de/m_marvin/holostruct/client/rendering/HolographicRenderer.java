package de.m_marvin.holostruct.client.rendering;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.joml.Matrix4f;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.holograms.BlockHoloState;
import de.m_marvin.holostruct.client.holograms.Hologram;
import de.m_marvin.holostruct.client.holograms.HologramChunk;
import de.m_marvin.holostruct.client.holograms.HologramManager;
import de.m_marvin.holostruct.client.holograms.HologramSection;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.client.rendering.HologramBufferContainer.HolographicBufferSource;
import de.m_marvin.holostruct.client.rendering.HologramRender.HolographicChunk;
import de.m_marvin.holostruct.client.rendering.HologramRender.HolographicChunk.HolographicSectionCompiled;
import de.m_marvin.holostruct.client.rendering.posteffect.PostEffectUtil;
import de.m_marvin.holostruct.client.rendering.posteffect.SelectivePostChain;
import de.m_marvin.holostruct.utility.UtilHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Manages the rendering of the holograms in the world.
 * @author Marvin Koehler
 */
@Mod.EventBusSubscriber(modid=HoloStruct.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HolographicRenderer {
	
	public static final Supplier<HologramManager> HOLOGRAM_MANAGER = () -> HoloStruct.CLIENT.HOLOGRAMS;
	public static final Supplier<Camera> CAMERA = () -> Minecraft.getInstance().gameRenderer.getMainCamera();
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDERER = () -> Minecraft.getInstance().getBlockRenderer();
	public static final Supplier<BlockEntityRenderDispatcher> BLOCK_ENTITY_RENDERER = () -> Minecraft.getInstance().getBlockEntityRenderDispatcher();
	public static final Supplier<EntityRenderDispatcher> ENTITY_RENDERER = () -> Minecraft.getInstance().getEntityRenderDispatcher();
	public static final Supplier<TextureManager> TEXTURE_MANAGER = () -> Minecraft.getInstance().getTextureManager();
	public static final Supplier<ResourceManager> RESOURCE_MANAGER = () -> Minecraft.getInstance().getResourceManager();
	public static final Supplier<RenderTarget> MAIN_FRAMEBUFFER = () -> Minecraft.getInstance().getMainRenderTarget();
	public static final Map<BlockHoloState, ResourceLocation> HOLOGRAPHIC_TARGET = BlockHoloState.renderedStates().stream().collect(Collectors.toMap(state -> state, state -> new ResourceLocation(HoloStruct.MODID, "holographic/" + state.toString().toLowerCase())));
	
	private Int2ObjectMap<HologramRender> hologramRenders = new Int2ObjectArrayMap<>();
	private BufferSource staticSource = MultiBufferSource.immediate(new BufferBuilder(2000));
	private SelectivePostChain activePostEffect;
	
	@SubscribeEvent
	public static void onRenderLevelLast(RenderLevelStageEvent event) {

		HolographicRenderer renderer = HoloStruct.CLIENT.HOLORENDERER;
		
		if (event.getStage() == Stage.AFTER_SKY) {
			renderer.recompileDirtyChunks();
			
			if (renderer.activePostEffect != null) {
				PostEffectUtil.preparePostEffect(renderer.activePostEffect);
				
				for (BlockHoloState holoState : BlockHoloState.renderedStates()) {
					RenderTarget framebuffer = renderer.activePostEffect.getTempTarget(HOLOGRAPHIC_TARGET.get(holoState).toString());
					if (framebuffer != null) {
						PostEffectUtil.clearFramebuffer(framebuffer);
						PostEffectUtil.unbindFramebuffer(framebuffer);
					}
				}
			}
			
		} else if (event.getStage() == Stage.AFTER_WEATHER) {
			
			if (renderer.activePostEffect != null)
				renderer.activePostEffect.process(event.getPartialTick());
			Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
			
		} else {
			
			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();
			renderer.translateToWorld(poseStack);
			
			if (event.getStage() == Stage.AFTER_SOLID_BLOCKS) {
				renderer.renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.solid());
			} else if (event.getStage() == Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
				renderer.renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.cutoutMipped());
			} else if (event.getStage() == Stage.AFTER_CUTOUT_BLOCKS) {
				renderer.renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.cutout());
			} else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
				renderer.renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.translucent());
			} else if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
				renderer.renderHolograms(poseStack, event.getProjectionMatrix(), RenderType.tripwire());
			} else if (event.getStage() == Stage.AFTER_BLOCK_ENTITIES) {
				HologramBufferContainer.getAlocatedRenderTypes().stream().filter(r -> !RenderType.chunkBufferLayers().contains(r)).forEach(renderLayer -> {
					renderer.renderHolograms(poseStack, event.getProjectionMatrix(), renderLayer);
				});
				renderer.renderHologramBounds(poseStack);
			} else 
				if (event.getStage() == Stage.AFTER_PARTICLES) {
			}
			
			poseStack.popPose();
			
		}
	}
	
	/**
	 * Loads the specified shader as post effect for the holograms
	 * @param postEffect THe location of the shader to load.
	 * @return true if the shader could be loaded
	 */
	public boolean loadPostEffect(ResourceLocation postEffect) {
		if (RenderSystem.isOnGameThread()) {
			return _loadPostEffect(postEffect);
		} else {
			try {
				return CompletableFuture.supplyAsync(() -> _loadPostEffect(postEffect), HoloStruct.CLIENT.RENDER_EXECUTOR).get();
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	private boolean _loadPostEffect(ResourceLocation postEffect) {
		if (this.activePostEffect != null) {
			this.activePostEffect.close();
		}
		
		if (postEffect == null) {
			return true;
		}
		
		try {
			this.activePostEffect = new SelectivePostChain(TEXTURE_MANAGER.get(), RESOURCE_MANAGER.get(), MAIN_FRAMEBUFFER.get(), postEffect, this::setupPostEffectShader);
			return true;
		} catch (IOException e) {
			HoloStruct.LOGGER.warn("Failed to load holographic post effect: {}", postEffect, e);
			this.activePostEffect = null;
			return false;
		} catch (JsonSyntaxException e) {
			HoloStruct.LOGGER.warn("Failed to parse holographic post effect: {}", postEffect, e);
			this.activePostEffect = null;
			return false;
		}
	}
	
	/**
	 * Returns the currently active post effect
	 */
	public SelectivePostChain getActivePostEffect() {
		return activePostEffect;
	}
	
	public BufferSource getStaticSource() {
		return staticSource;
	}
	
	public Int2ObjectMap<HologramRender> getHologramRenders() {
		return hologramRenders;
	}
	
	/**
	 * Marks the specified chunk section to be redrawn
	 * @param hologram The hologram to which the section belongs to
	 * @param pos The position of the chunk in which the section is located
	 * @param section The Y index of the section
	 */
	public void markDirty(Hologram hologram, ChunkPos pos, int section) {
		if (hologram == null) return;
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = hologramRenders.get(hologram.hashCode());
			if (holoRender != null) {
				Optional<HologramChunk> hchunk = hologram.getChunk(pos);
				if (hchunk.isEmpty()) return;
				HolographicChunk chunk = holoRender.renderChunks.get(pos.toLong());
				if (chunk == null) {
					chunk = new HolographicChunk(new ChunkPos(pos.toLong()));
					holoRender.renderChunks.put(pos.toLong(), chunk);

					chunk.dirty.addAll(hchunk.get().getSections().keySet());
				} else {
					chunk.dirty.add(section);
				}
			}
		});
	}
	
	/**
	 * Adds an hologram to the list of holograms that are rendered
	 */
	public void addHologram(Hologram hologram) {
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = new HologramRender(hologram);
			hologramRenders.put(hologram.hashCode(), holoRender);
			
			hologram.getChunks().forEach(hchunk -> {
				ChunkPos pos = hchunk.getPosition();
				HolographicChunk chunk = holoRender.renderChunks.get(pos.toLong());
				
				if (chunk == null) {
					chunk = new HolographicChunk(pos);
					holoRender.renderChunks.put(pos.toLong(), chunk);
				}
				
				chunk.dirty.addAll(hchunk.getSections().keySet());
			});
		});
	}
	
	/**
	 * Removes an hologram from the list of holograms that are rendered.
	 */
	public void discardHologram(Hologram hologram) {
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = hologramRenders.remove(hologram.hashCode());
			if (holoRender != null) {
				holoRender.hologram = null;
				holoRender.renderChunks.forEach((lp, chunk) -> {
					chunk.compiled.values().forEach(HolographicSectionCompiled::discardBuffers);
					chunk.discardBuffers();
				});
			}
		});
	}
	
	private void recompileDirtyChunks() {
		hologramRenders.forEach((hid, hologram) -> {
			hologram.renderChunks.forEach((pos, chunk) -> {
				chunk.dirty.forEach(section ->
					prerenderHolographicSection(hologram, chunk, section)
				);
				chunk.dirty.clear();
			});
		});
	}
	
	private void prerenderHolographicSection(HologramRender holoRender, HolographicChunk holoChunk, int sectionIndex) {
		if (holoRender.hologram == null) return; // This hologram is currently being deleted
		
		Optional<HologramChunk> chunk = holoRender.hologram.getChunk(holoChunk.pos);
		HologramSection section = chunk.isPresent() ? chunk.get().getSections().get(sectionIndex) : null;
		
		if (section == null) {
			if (holoChunk.compiled.containsKey(sectionIndex)) {
				holoChunk.compiled.remove(sectionIndex).discardBuffers();
			}
			return;
		}
		
		IRemoteLevelAccessor level = HoloStruct.CLIENT.LEVELBOUND.getAccessor();
		
		CompletableFuture.runAsync(() -> {
			if (holoRender.hologram == null) return; // This hologram is currently being deleted
			
			holoChunk.claimBuffers();
			
			RenderType.chunkBufferLayers().stream().forEach(renderLayer -> {
				renderChunkLayers(holoChunk.bufferContainer, renderLayer, holoRender.hologram, level, holoRender.hologram.position, holoRender.hologram.origin, holoChunk.pos, sectionIndex, section);
			});
			
			List<BlockEntity> sectionBlockEntities = chunk.get().getBlockEntities().entrySet().stream()
					.filter(entry -> entry.getKey().getY() >= sectionIndex << 4 && entry.getKey().getY() < (sectionIndex + 1) << 4)
					.map(Map.Entry::getValue).toList();
			renderBlockEntities(holoChunk.bufferContainer, holoRender.hologram, holoRender.hologram.position, holoRender.hologram.origin, holoChunk.pos, sectionIndex, section, sectionBlockEntities);
			
			List<Entity> sectionEntities = holoRender.hologram.getEntitiesInSection(chunk.get().getPosition(), sectionIndex);
			renderEntities(holoChunk.bufferContainer, holoRender.hologram, holoRender.hologram.position, holoRender.hologram.origin, holoChunk.pos, sectionIndex, section, sectionEntities);
		})
		.exceptionally(e -> { 
			HoloStruct.LOGGER.error("holographic renderer/prerender stage failed!");
			e.printStackTrace();
			return null;
		})
		.thenRunAsync(() -> {
			if (holoRender.hologram == null) return; // This hologram is currently being deleted
			
			HolographicSectionCompiled compiled = new HolographicSectionCompiled();
			compiled.genBuffers(HologramBufferContainer.getAlocatedRenderTypes());
			
			BlockHoloState.renderedStates().forEach(holoState -> {
				HologramBufferContainer.getAlocatedRenderTypes().forEach(renderLayer -> {
					HolographicBufferSource bufferSource = holoChunk.bufferContainer.getBufferSource(holoState);
					if (bufferSource == null) return; // This can happen if a hologram is deleted while a recompile task is executed
					compiled.bindBuffer(holoState, renderLayer).upload(bufferSource.endBatch(renderLayer));
				});
			});
			
			HolographicSectionCompiled old = holoChunk.compiled.put(sectionIndex, compiled);
			if (old != null) old.discardBuffers();
			
			holoChunk.releaseBuffers();
			
		}, HoloStruct.CLIENT.RENDER_EXECUTOR)
		.exceptionally(e -> {
			holoChunk.releaseBuffers();
			HoloStruct.LOGGER.error("holographic renderer/upload stage failed!");
			e.printStackTrace();
			return null;
		});
		
	}
	
	private void renderBlockEntities(HologramBufferContainer bufferContainer, LevelAccessor level, BlockPos hologramPosition, BlockPos origin, ChunkPos chunkPos, int yindex, HologramSection section, List<BlockEntity> blockEntities) {
		
		PoseStack poseStack = new PoseStack();
		BlockEntityRenderDispatcher blockEntityRenderer = BLOCK_ENTITY_RENDERER.get();
		HologramManager hologramManager = HOLOGRAM_MANAGER.get();
		
		blockEntities.forEach(blockEntity -> {
			
			BlockPos holoPos = blockEntity.getBlockPos();
			BlockPos worldPos = hologramPosition.offset(holoPos).subtract(origin);
			
			if (worldPos.getY() != hologramManager.getActiveLayer() && hologramManager.isOneLayerMode()) return;
			
			BlockHoloState holoState = section.getHoloState(holoPos.getX(), holoPos.getY(), holoPos.getZ());
			
			MultiBufferSource bufferSource = bufferContainer.getBufferSource(holoState);
			BlockEntityRenderer<BlockEntity> renderer = blockEntityRenderer.getRenderer(blockEntity);
			
			if (renderer != null) {
				
				int light = LevelRenderer.getLightColor(level, worldPos);
				
				poseStack.pushPose();
				poseStack.translate(-origin.getX(), -origin.getY(), -origin.getZ());
				poseStack.translate(holoPos.getX(), holoPos.getY(), holoPos.getZ());
				renderer.render(blockEntity, 0, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
				poseStack.popPose();
				
			}
			
		});
		
	}
	
	private void renderEntities(HologramBufferContainer bufferContainer, LevelAccessor level, BlockPos hologramPosition, BlockPos origin, ChunkPos chunkPos, int yindex, HologramSection section, List<Entity> entities) {
		
		PoseStack poseStack = new PoseStack();
		EntityRenderDispatcher entityRenderer = ENTITY_RENDERER.get();
		HologramManager hologramManager = HOLOGRAM_MANAGER.get();
		
		entities.forEach(entity -> {
			
			Vec3 holoPos = entity.position();
			Vec3 worldPos = holoPos.add(hologramPosition.getX() - origin.getX(), hologramPosition.getY() - origin.getY(), hologramPosition.getZ() - origin.getZ());
			
			if ((int) Math.floor(worldPos.y) != hologramManager.getActiveLayer() && hologramManager.isOneLayerMode()) return;
			
			MultiBufferSource bufferSource = bufferContainer.getBufferSource(BlockHoloState.NO_BLOCK); // Currently entities do not support detecting of correct placement
			EntityRenderer<? super Entity> renderer = entityRenderer.getRenderer(entity);
			
			if (renderer != null) {

				int light = LevelRenderer.getLightColor(level, UtilHelper.toBlockPos(worldPos));
				poseStack.pushPose();
				poseStack.translate(-origin.getX(), -origin.getY(), -origin.getZ());
				poseStack.translate(holoPos.x, holoPos.y, holoPos.z);
				renderer.render(entity, entity.getYRot(), 0, poseStack, bufferSource, light);
				poseStack.popPose();
				
			}
			
		});
		
	}
	
	private void renderChunkLayers(HologramBufferContainer bufferContainer, RenderType renderLayer, LevelAccessor holoLevel, IRemoteLevelAccessor realLevel, BlockPos hologramPosition, BlockPos origin, ChunkPos chunkPos, int yindex, HologramSection section) {
		
		PoseStack poseStack = new PoseStack();
		BlockRenderDispatcher blockRenderer = BLOCK_RENDERER.get();
		HologramManager hologramManager = HOLOGRAM_MANAGER.get();
		RandomSource random = RandomSource.create();
		
		int posY = yindex << 4;
		
		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					BlockPos holoPos = new BlockPos(chunkPos.getMinBlockX() + x, posY + y, chunkPos.getMinBlockZ() + z);
					BlockPos worldPos = hologramPosition.offset(holoPos).subtract(origin);
					
					if (worldPos.getY() != hologramManager.getActiveLayer() && hologramManager.isOneLayerMode()) continue;
					
					BlockState state = section.getState(x, y, z);
					BlockHoloState holoState = section.getHoloState(x, y, z);
					HolographicBufferSource bufferSource = bufferContainer.getBufferSource(holoState);
					
					VertexConsumer builder = bufferSource.getBuffer(renderLayer);
					
					if (holoState != BlockHoloState.NO_BLOCK) 
						state = realLevel.getBlockState(worldPos);
					
					BakedModel model = blockRenderer.getBlockModel(state);
					ModelData modelData = model.getModelData(holoLevel, hologramPosition, state, ModelData.EMPTY);
					
					if (!state.isAir() && model.getRenderTypes(state, random, modelData).contains(renderLayer)) {
						poseStack.pushPose();
						poseStack.translate(-origin.getX(), -origin.getY(), -origin.getZ());
						poseStack.translate(x, y, z);
						blockRenderer.renderBatched(state, hologramPosition.offset(holoPos).subtract(origin), holoLevel, poseStack, builder, false, random, modelData, renderLayer);
						poseStack.popPose();
					}
					
					FluidState fluidState = state.getFluidState();
					if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderLayer) {
						blockRenderer.renderLiquid(holoPos, holoLevel, builder, state, fluidState);
					}
					
				}
			}
		}
		
	}
	
	protected void translateToWorld(PoseStack poseStack) {
		Vec3 cameraOffset = CAMERA.get().getPosition();
		poseStack.translate(-cameraOffset.x, -cameraOffset.y, -cameraOffset.z);
	}
	
	protected void renderHologramBounds(PoseStack poseStack) {
		
		RenderTarget framebuffer = null;
		if (this.activePostEffect != null) {
			framebuffer = this.activePostEffect.getTempTarget(HOLOGRAPHIC_TARGET.get(BlockHoloState.NO_BLOCK).toString());
			if (framebuffer != null) PostEffectUtil.bindFramebuffer(framebuffer);
		}
		
		hologramRenders.forEach((hid, hologram) -> {
			
			BlockPos position0 = hologram.hologram.holoToWorldPosition(hologram.hologram.getBlockBoundsMin());
			BlockPos position1 = hologram.hologram.holoToWorldPosition(hologram.hologram.getBlockBoundsMax());
			BlockPos origin = hologram.hologram.holoToWorldPosition(hologram.hologram.origin);
			
			poseStack.pushPose();
			
			VertexConsumer buffer = staticSource.getBuffer(RenderType.lineStrip());
			
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
			
			// TODO render origin
			
			staticSource.endBatch(RenderType.lineStrip());
			
			buffer = staticSource.getBuffer(RenderType.lineStrip());
			
			poseStack.pushPose();
			poseStack.translate(origin.getX() + 0.25F, origin.getY() + 0.25F, origin.getZ() + 0.25F);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			
			renderHologramLine(poseStack, buffer, 0, 0, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 0, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 0, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 0, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 0, 0, 1, 0, 1, 1);
			
			renderHologramLine(poseStack, buffer, 0, 1, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 0, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 1, 0, 1, 0, 1, 1);
			
			renderHologramLine(poseStack, buffer, 1, 1, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 0, 0, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 1, 0, 1, 0, 1, 1);
			
			renderHologramLine(poseStack, buffer, 1, 1, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 0, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 1, 1, 1, 1, 0, 1, 1);
			
			renderHologramLine(poseStack, buffer, 0, 1, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 0, 1, 1, 0, 1, 1);
			renderHologramLine(poseStack, buffer, 0, 1, 1, 1, 0, 1, 1);
			
			renderHologramLine(poseStack, buffer, 0, 1, 0, 1, 0, 1, 1);
			
			poseStack.popPose();
			poseStack.popPose();
			
		});

		if (this.activePostEffect != null) {
			if (framebuffer != null) PostEffectUtil.unbindFramebuffer(framebuffer);
		}
		
	}
	
	protected void renderHologramLine(PoseStack poseStack, VertexConsumer buffer, float fx, float fy, float fz, float r, float g, float b, float a) {
		buffer.vertex(poseStack.last().pose(), fx, fy, fz).color(r, g, b, a).normal(poseStack.last().normal(), 1, 1, 1).endVertex();
	}
	
	protected void renderHolograms(PoseStack poseStack, Matrix4f projectionMatrix, RenderType renderLayer) {
		
		renderLayer.setupRenderState();
		ShaderInstance shader = RenderSystem.getShader();
		Uniform chunkOffset = shader.CHUNK_OFFSET;
		Uniform modelViewMatrix = shader.MODEL_VIEW_MATRIX;

		setupSectionShader(projectionMatrix, shader);
		
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1F, -1F);
		
		for (HologramRender hologramRender : hologramRenders.values()) {
			BlockPos origin = hologramRender.hologram.getPosition();
			
			poseStack.pushPose();
			poseStack.translate(origin.getX(), origin.getY(), origin.getZ());
			
			if (modelViewMatrix != null) {
				modelViewMatrix.set(poseStack.last().pose());
				modelViewMatrix.upload();
			}
			
			for (HolographicChunk chunkRender : hologramRender.renderChunks.values()) {
				chunkRender.compiled.forEach((yi, section) -> {
					
					if (chunkRender.compiled.containsKey((int) yi)) {
						if (chunkOffset != null) {
							chunkOffset.set((float) (chunkRender.pos.x << 4), (float) (yi << 4), (float) (chunkRender.pos.z << 4));
							chunkOffset.upload();
						}
						BlockHoloState.renderedStates().forEach((holoState) -> {
							
							if (!section.hasBufferFor(holoState, renderLayer)) return;
							
							RenderTarget framebuffer = null;
							if (this.activePostEffect != null) {
								framebuffer = this.activePostEffect.getTempTarget(HOLOGRAPHIC_TARGET.get(holoState).toString());
								if (framebuffer != null) PostEffectUtil.bindFramebuffer(framebuffer);
							}
							
							section.bindBuffer(holoState, renderLayer).draw();
							
							if (this.activePostEffect != null) {
								if (framebuffer != null) PostEffectUtil.unbindFramebuffer(framebuffer);
							}
							
						});
					}
				});
			}
			poseStack.popPose();
		}
		
		RenderSystem.disablePolygonOffset();
		
		shader.clear();
		renderLayer.clearRenderState();
		
	}
	
	protected void setupSectionShader(Matrix4f pProjectionMatrix, ShaderInstance pShader) {
		for(int i = 0; i < 12; ++i) {
			int j = RenderSystem.getShaderTexture(i);
			pShader.setSampler("Sampler" + i, j);
		}
		
		if (pShader.PROJECTION_MATRIX != null) {
			pShader.PROJECTION_MATRIX.set(pProjectionMatrix);
		}

		if (pShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
			pShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		}

		if (pShader.COLOR_MODULATOR != null) {
			pShader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		}

		if (pShader.GLINT_ALPHA != null) {
			pShader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
		}

		if (pShader.FOG_START != null) {
			pShader.FOG_START.set(RenderSystem.getShaderFogStart());
		}

		if (pShader.FOG_END != null) {
			pShader.FOG_END.set(RenderSystem.getShaderFogEnd());
		}

		if (pShader.FOG_COLOR != null) {
			pShader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		}

		if (pShader.FOG_SHAPE != null) {
			pShader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		}

		if (pShader.TEXTURE_MATRIX != null) {
			pShader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
		}

		if (pShader.GAME_TIME != null) {
			pShader.GAME_TIME.set(RenderSystem.getShaderGameTime());
		}

		if (pShader.SCREEN_SIZE != null) {
			Window window = Minecraft.getInstance().getWindow();
			pShader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
		}

		if (pShader.LINE_WIDTH != null) {
			pShader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
		}

		RenderSystem.setupShaderLights(pShader);
		pShader.apply();
	}
	
	@SuppressWarnings("resource")
	protected void setupPostEffectShader(EffectInstance shader) {
		shader.safeGetUniform("GameTime").set((float) Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime());
	}
	
}
