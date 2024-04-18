package de.m_marvin.holostruct.client.rendering;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.holograms.BlockHoloState;
import de.m_marvin.holostruct.client.holograms.Hologram;
import de.m_marvin.holostruct.client.holograms.HologramChunk;
import de.m_marvin.holostruct.client.holograms.HologramSection;
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
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

@Mod.EventBusSubscriber(modid=HoloStruct.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HolographicRenderer {
	
	public static final Supplier<MultiBufferSource> BUFFER_SOURCE = () -> Minecraft.getInstance().renderBuffers().bufferSource();
	public static final Supplier<Camera> CAMERA = () -> Minecraft.getInstance().gameRenderer.getMainCamera();
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDERER = () -> Minecraft.getInstance().getBlockRenderer();
	public static final Supplier<BlockEntityRenderDispatcher> BLOCK_ENTITY_RENDERER = () -> Minecraft.getInstance().getBlockEntityRenderDispatcher();
	public static final Supplier<EntityRenderDispatcher> ENTITY_RENDERER = () -> Minecraft.getInstance().getEntityRenderDispatcher();
	
	protected Int2ObjectMap<HologramRender> hologramRenders = new Int2ObjectArrayMap<>();
	
	public SelectivePostChain postEffect;
	
    public void loadEffect(ResourceLocation pResourceLocation) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        try {
            this.postEffect = new SelectivePostChain(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getMainRenderTarget(), pResourceLocation);
            this.postEffect.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        } catch (IOException ioexception) {
           HoloStruct.LOGGER.warn("Failed to load shader: {}", pResourceLocation, ioexception);
           this.postEffect = null;
        } catch (JsonSyntaxException jsonsyntaxexception) {
        	HoloStruct.LOGGER.warn("Failed to parse shader: {}", pResourceLocation, jsonsyntaxexception);
            this.postEffect = null;
        }
    }
	
//    @SubscribeEvent
//    public static void onReload(AddReloadListenerEvent event) {
//    	event.addListener((pPreparationBarrier, pResourceManager, pPreparationsProfiler, pReloadProfiler, pBackgroundExecutor, pGameExecutor) -> {
//    		return CompletableFuture.runAsync(() -> {
//    			HolographicRenderer renderer = HoloStruct.CLIENT.HOLORENDERER;
//        		renderer.postEffect = null;
//    		}).exceptionally(e -> {
//    			e.printStackTrace();
//    			return null;
//    		});
//    	});
//    }
    
	@SubscribeEvent
	public static void onRenderLevelLast(RenderLevelStageEvent event) {

		HolographicRenderer renderer = HoloStruct.CLIENT.HOLORENDERER;
		
		if (event.getStage() == Stage.AFTER_SKY) {
			renderer.recompileDirtyChunks();
			
//			renderer.postEffect = null;
			if (renderer.postEffect == null) {
				renderer.loadEffect(new ResourceLocation("holostruct:shaders/post/creeper.json"));
			}
			
			RenderTarget framebuffer = renderer.postEffect.getTempTarget("holostruct:holographic");
			PostEffectUtil.preparePostEffect(renderer.postEffect);
			PostEffectUtil.clearFramebuffer(framebuffer);
			PostEffectUtil.unbinFramebuffer(framebuffer);
			
		} else if (event.getStage() == Stage.AFTER_WEATHER) {

			
			renderer.postEffect.process(0);
			
		} else {
			
			RenderTarget framebuffer = renderer.postEffect.getTempTarget("holostruct:holographic");
			PostEffectUtil.bindFramebuffer(framebuffer);
			
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
			} else 
				if (event.getStage() == Stage.AFTER_PARTICLES) {
				renderer.renderHologramBounds(poseStack, BUFFER_SOURCE.get());
			}
			
			poseStack.popPose();
			
			PostEffectUtil.unbinFramebuffer(framebuffer);
			
		}
	}
	
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
	
	public void discardHologram(Hologram hologram) {
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = hologramRenders.remove(hologram.hashCode());
			if (holoRender != null) {
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
		
		Optional<HologramChunk> chunk = holoRender.hologram.getChunk(holoChunk.pos);
		HologramSection section = chunk.isPresent() ? chunk.get().getSections().get(sectionIndex) : null;
		
		if (section == null && holoChunk.compiled.containsKey(sectionIndex)) {
			
			holoChunk.compiled.remove(sectionIndex).discardBuffers();;
			return;
			
		}
		
		CompletableFuture.runAsync(() -> {
			holoChunk.claimBuffers();
			
			RenderType.chunkBufferLayers().stream().forEach(renderLayer ->
				renderChunkLayers(holoChunk.bufferContainer, renderLayer, holoRender.hologram, holoRender.hologram.position, holoChunk.pos, sectionIndex, section)
			);
			
			List<BlockEntity> sectionBlockEntities = chunk.get().getBlockEntities().entrySet().stream()
					.filter(entry -> entry.getKey().getY() >= sectionIndex << 4 && entry.getKey().getY() < (sectionIndex + 1) << 4)
					.map(Map.Entry::getValue).toList();
			renderBlockEntities(holoChunk.bufferContainer, holoRender.hologram, holoRender.hologram.position, holoChunk.pos, sectionIndex, section, sectionBlockEntities);
			
			List<Entity> sectionEntities = holoRender.hologram.getEntitiesInSection(chunk.get().getPosition(), sectionIndex);
			renderEntities(holoChunk.bufferContainer, holoRender.hologram, holoRender.hologram.position, holoChunk.pos, sectionIndex, section, sectionEntities);
		})
		.exceptionally(e -> { 
			HoloStruct.LOGGER.error("holographic renderer/prerender stage failed!");
			e.printStackTrace();
			return null;
		})
		.thenRunAsync(() -> {
			
			HolographicSectionCompiled compiled = new HolographicSectionCompiled();
			compiled.genBuffers(HologramBufferContainer.getAlocatedRenderTypes());
			
			BlockHoloState.renderedStates().forEach(holoState -> {
				HologramBufferContainer.getAlocatedRenderTypes().forEach(renderLayer -> {
					HolographicBufferSource bufferSource = holoChunk.bufferContainer.getBufferSource(holoState);
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
	
	private void renderBlockEntities(HologramBufferContainer bufferContainer, LevelAccessor level, BlockPos hologramPosition, ChunkPos chunkPos, int yindex, HologramSection section, List<BlockEntity> blockEntities) {
		
		PoseStack poseStack = new PoseStack();
		BlockEntityRenderDispatcher blockEntityRenderer = BLOCK_ENTITY_RENDERER.get();
		
		blockEntities.forEach(blockEntity -> {
			
			BlockPos holoPos = blockEntity.getBlockPos();
			BlockPos worldPos = hologramPosition.offset(holoPos);
			BlockHoloState holoState = section.getHoloState(holoPos.getX(), holoPos.getY(), holoPos.getZ());
			
			MultiBufferSource bufferSource = bufferContainer.getBufferSource(holoState);
			BlockEntityRenderer<BlockEntity> renderer = blockEntityRenderer.getRenderer(blockEntity);
			
			if (renderer != null) {
				
				int light = LevelRenderer.getLightColor(level, worldPos);
				
				poseStack.pushPose();
				poseStack.translate(holoPos.getX(), holoPos.getY(), holoPos.getZ());
				renderer.render(blockEntity, 0, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
				poseStack.popPose();
				
			}
			
		});
		
	}
	
	private void renderEntities(HologramBufferContainer bufferContainer, LevelAccessor level, BlockPos hologramPosition, ChunkPos chunkPos, int yindex, HologramSection section, List<Entity> entities) {
		
		PoseStack poseStack = new PoseStack();
		EntityRenderDispatcher entityRenderer = ENTITY_RENDERER.get();
		
		entities.forEach(entity -> {
			
			Vec3 holoPos = entity.position();
			Vec3 worldPos = holoPos.add(hologramPosition.getX(), hologramPosition.getY(), hologramPosition.getZ());
			
			MultiBufferSource bufferSource = bufferContainer.getBufferSource(BlockHoloState.NO_BLOCK); // Currently entities do not support detecting of correct placement
			EntityRenderer<? super Entity> renderer = entityRenderer.getRenderer(entity);
			
			if (renderer != null) {

				int light = LevelRenderer.getLightColor(level, UtilHelper.toBlockPos(worldPos));
				
				poseStack.pushPose();
				poseStack.translate(holoPos.x, holoPos.y, holoPos.z);
				renderer.render(entity, entity.getYRot(), 0, poseStack, bufferSource, light);
				poseStack.popPose();
				
			}
			
		});
		
	}
	
	private void renderChunkLayers(HologramBufferContainer bufferContainer, RenderType renderLayer, LevelAccessor level, BlockPos hologramPosition, ChunkPos chunkPos, int yindex, HologramSection section) {
		
		PoseStack poseStack = new PoseStack();
		BlockRenderDispatcher blockRenderer = BLOCK_RENDERER.get();
		RandomSource random = RandomSource.create();
		
		int posY = yindex << 4;

		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					BlockPos holoPos = new BlockPos(chunkPos.getMinBlockX() + x, posY + y, chunkPos.getMinBlockX() + z);
					BlockPos worldPos = hologramPosition.offset(holoPos);
					BlockState state = section.getState(x, y, z);
					BlockHoloState holoState = section.getHoloState(x, y, z);

					VertexConsumer builder = bufferContainer.getBufferSource(holoState).getBuffer(renderLayer);
					
					if (holoState != BlockHoloState.NO_BLOCK) state = level.getBlockState(worldPos);

					BakedModel model = blockRenderer.getBlockModel(state);
					ModelData modelData = model.getModelData(level, hologramPosition, state, ModelData.EMPTY);
					
					if (!state.isAir() && model.getRenderTypes(state, random, modelData).contains(renderLayer)) {
						poseStack.pushPose();
						poseStack.translate(x + 0.5F, y + 0.5F, z + 0.5F);
						if (holoState != BlockHoloState.NO_BLOCK) poseStack.scale(1.01F, 1.01F, 1.01F);
						poseStack.translate(-0.5F, -0.5F, -0.5F);
						blockRenderer.renderBatched(state, hologramPosition.offset(holoPos), level, poseStack, builder, false, random, modelData, renderLayer);
						poseStack.popPose();
					}
					
					FluidState fluidState = state.getFluidState();
					if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderLayer) {
						blockRenderer.renderLiquid(holoPos, level, builder, state, fluidState);
					}
				}
			}
		}
		
	}
	
	protected void translateToWorld(PoseStack poseStack) {
		Vec3 cameraOffset = CAMERA.get().getPosition();
		poseStack.translate(-cameraOffset.x, -cameraOffset.y, -cameraOffset.z);
	}
	
	protected void renderHologramBounds(PoseStack poseStack, MultiBufferSource source) {
		
		hologramRenders.forEach((hid, hologram) -> {
			
			BlockPos position0 = hologram.hologram.holoToWorldPosition(hologram.hologram.getBlockBoundsMin());
			BlockPos position1 = hologram.hologram.holoToWorldPosition(hologram.hologram.getBlockBoundsMax());
			
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
	
	protected void renderHologramLine(PoseStack poseStack, VertexConsumer buffer, float fx, float fy, float fz, float r, float g, float b, float a) {
		buffer.vertex(poseStack.last().pose(), fx, fy, fz).color(r, g, b, a).normal(poseStack.last().normal(), 1, 1, 1).endVertex();
	}
	
	protected void renderHolograms(PoseStack poseStack, Matrix4f projectionMatrix, RenderType renderLayer) {
		
		renderLayer.setupRenderState();
		ShaderInstance shader = RenderSystem.getShader();
		Uniform chunkOffset = shader.CHUNK_OFFSET;
		Uniform holoStateColor = shader.COLOR_MODULATOR;
		Uniform modelViewMatrix = shader.MODEL_VIEW_MATRIX;

		setupSectionShader(projectionMatrix, shader);
		
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
							if (holoStateColor != null) {
								holoStateColor.set(holoState.colorRed, holoState.colorGreen, holoState.colorBlue, holoState.colorAlpha);
								holoStateColor.upload();
							}
							section.bindBuffer(holoState, renderLayer).draw();
						});
					}
				});
			}
			poseStack.popPose();
		}
		
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
	
}
