package de.m_marvin.holostructures.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.holograms.BlockHoloState;
import de.m_marvin.holostructures.client.holograms.Hologram;
import de.m_marvin.holostructures.client.holograms.HologramChunk;
import de.m_marvin.holostructures.client.rendering.HologramBufferContainer.HolographicBufferSource;
import de.m_marvin.holostructures.client.rendering.HologramRender.HolographicChunk;
import de.m_marvin.holostructures.client.rendering.HologramRender.HolographicChunk.HolographicChunkCompiled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.model.data.ModelData;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HolographicRenderer {
	
	public static final int BUFFER_BUILDER_CAPACITY = 786432; // copied this number from the RenderBuffers class
	
	public static final Supplier<MultiBufferSource> BUFFER_SOURCE = () -> Minecraft.getInstance().renderBuffers().bufferSource();
	public static final Supplier<Camera> CAMERA = () -> Minecraft.getInstance().gameRenderer.getMainCamera();
	public static final Supplier<BlockRenderDispatcher> BLOCK_RENDERER = () -> Minecraft.getInstance().getBlockRenderer();
	public static final Supplier<BlockEntityRenderDispatcher> BLOCK_ENTITY_RENDERER = () -> Minecraft.getInstance().getBlockEntityRenderDispatcher();
	
	protected Int2ObjectMap<HologramRender> hologramRenders = new Int2ObjectArrayMap<>();
	protected HologramBufferContainer bufferContainer = new HologramBufferContainer(() -> {
		Map<RenderType, BufferBuilder> builders = new HashMap<>();
		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			builders.put(renderType, new BufferBuilder(BUFFER_BUILDER_CAPACITY));
		}
		return new HologramBufferContainer.HolographicBufferSource(builders, builders.get(RenderType.solid()));
	});
	
	@SubscribeEvent
	public static void onRenderLevelLast(RenderLevelStageEvent event) {
		if (event.getStage() == Stage.AFTER_SKY) {
			HolographicRenderer renderer = HoloStructures.CLIENT.HOLORENDERER;
			renderer.recompileDirtyChunks();
		} else {
			HolographicRenderer renderer = HoloStructures.CLIENT.HOLORENDERER;
			
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
			} else if (event.getStage() == Stage.AFTER_PARTICLES) {
				renderer.renderHologramBounds(poseStack, BUFFER_SOURCE.get());
			}
			
			poseStack.popPose();
		}
	}
	
	public void markDirty(Hologram hologram, ChunkPos pos) {
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = hologramRenders.get(hologram.hashCode());
			if (holoRender != null) {
				HolographicChunk chunk = holoRender.renderChunks.get(pos.toLong());
				if (chunk == null) {
					chunk = new HolographicChunk(new ChunkPos(pos.toLong()));
					holoRender.renderChunks.put(pos.toLong(), chunk);
				} else {
					chunk.dirty = true;
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
				} else {
					chunk.dirty = true;
				}
			});
		});
	}
	
	public void discardHologram(Hologram hologram) {
		RenderSystem.recordRenderCall(() -> {
			HologramRender holoRender = hologramRenders.remove(hologram.hashCode());
			if (holoRender != null) {
				holoRender.renderChunks.forEach((lp, chunk) -> {
					chunk.compiled.get().discardBuffers();
				});
			}
		});
	}
	
	private void recompileDirtyChunks() {
		hologramRenders.forEach((hid, hologram) -> {
			hologram.renderChunks.forEach((pos, chunk) -> {
				if (chunk.dirty) {
					chunk.dirty = false;
					compileHolographicChunk(hologram, chunk);
					return;
				}
			});
		});
	}
	
	private void compileHolographicChunk(HologramRender hologram, HolographicChunk chunk) {
		
		Optional<HologramChunk> holoChunk = hologram.hologram.getChunk(chunk.pos);
		
		if (holoChunk.isEmpty()) {

			chunk.compiled.get().discardBuffers();
			chunk.compiled.set(HolographicChunkCompiled.UNCOMPILED);
			return;
			
		}
		
		HolographicChunkCompiled compiled = new HolographicChunkCompiled();
		compiled.genBuffers();
		
		CompletableFuture.allOf(
				
				BlockHoloState.renderedStates().stream().flatMap(holoState -> {
					
					HolographicBufferSource bufferSource = this.bufferContainer.getBufferSource(holoState);
					
					return RenderType.chunkBufferLayers().stream().map(renderLayer -> {
						
						return CompletableFuture.runAsync(() -> {
							
							PoseStack poseStack = new PoseStack();
							BlockRenderDispatcher blockRenderer = BLOCK_RENDERER.get();
							BlockEntityRenderDispatcher blockEntityRenderer = BLOCK_ENTITY_RENDERER.get();
							RandomSource random = RandomSource.create();
							
							VertexConsumer builder = bufferSource.getBuffer(renderLayer);
							
							holoChunk.get().getSections().forEach((yi, section) -> {
								
								int posY = yi << 4;
	
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										for (int x = 0; x < 16; x++) {
											BlockPos holoPos = new BlockPos((chunk.pos.x << 4) + x, posY + y, (chunk.pos.z << 4) + z);
											BlockPos worldPos = hologram.hologram.getPosition().offset(holoPos);
											BlockState state = section.getState(x, y, z);
											
											if (holoState != BlockHoloState.NO_BLOCK) state = hologram.hologram.level.getBlockState(worldPos);
											if (holoState == section.getHoloState(x, y, z)) {
												
												BakedModel model = blockRenderer.getBlockModel(state);
												if (!state.isAir() && model.getRenderTypes(state, random, ModelData.EMPTY).contains(renderLayer)) {
													poseStack.pushPose();
													poseStack.translate(x + 0.5F, posY + y + 0.5F, z + 0.5F);
													if (holoState != BlockHoloState.NO_BLOCK) poseStack.scale(1.01F, 1.01F, 1.01F);
													poseStack.translate(-0.5F, -0.5F, -0.5F);
													blockRenderer.renderBatched(state, hologram.hologram.getPosition().offset(holoPos), hologram.hologram.level, poseStack, builder, false, random);
													poseStack.popPose();
												}
												
											}
											
										}
									}
								}
								
							});
							
							holoChunk.get().getBlockEntities().forEach((position, blockEntity) -> {
								
								BlockPos holoPos = position;
								BlockPos worldPos = hologram.hologram.getPosition().offset(holoPos);
								
								// TODO fake world for rendering
								blockEntityRenderer.render(blockEntity, 0, poseStack, bufferSource);
								
							});
							
						}).thenRunAsync(() -> {
							
							System.out.println("upload VAO"); // TODO DEBUG
							compiled.bindBuffer(holoState, renderLayer).upload(bufferSource.endBatch(renderLayer));
							
						}, HoloStructures.CLIENT.RENDER_EXECUTOR);
						
					});
					
				}).toArray(i -> new CompletableFuture[i])
				
		).thenRun(() -> {
			
			chunk.compiled.get().discardBuffers();
			chunk.compiled.set(compiled);
			
		});
		
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
		
		ModRenderType.hologramBlocks().setupRenderState();
		ShaderInstance shader = RenderSystem.getShader();
		initChunkRenderShader(poseStack.last().pose(), projectionMatrix, shader);
		Uniform chunkOffset = shader.CHUNK_OFFSET;
		Uniform holoStateColor = shader.COLOR_MODULATOR;
		
		for (HologramRender hologramRender : hologramRenders.values()) {
			BlockPos origin = hologramRender.hologram.getPosition();
			
			poseStack.pushPose();
			poseStack.translate(origin.getX(), origin.getY(), origin.getZ());
			for (HolographicChunk chunkRender : hologramRender.renderChunks.values()) {
				if (chunkRender.compiled.get() != HolographicChunkCompiled.UNCOMPILED) {
					if (chunkOffset != null) {
						chunkOffset.set((float)(origin.getX() + chunkRender.pos.x * 16), (float)(origin.getY()), (float)(origin.getZ() + chunkRender.pos.z * 16));
						chunkOffset.upload();
					}
					BlockHoloState.renderedStates().forEach((holoState) -> {
						if (holoStateColor != null) {
							holoStateColor.set(holoState.colorRed, holoState.colorGreen, holoState.colorBlue, holoState.colorAlpha);
							holoStateColor.upload();
						}
						chunkRender.compiled.get().bindBuffer(holoState, renderLayer).draw();
					});
				}
			}
			poseStack.popPose();
		}
		
		shader.clear();
		
	}
	
	protected void initChunkRenderShader(Matrix4f pModelViewMatrix, Matrix4f pProjectionMatrix, ShaderInstance pShader) {
		for(int i = 0; i < 12; ++i) {
			int j = RenderSystem.getShaderTexture(i);
			pShader.setSampler("Sampler" + i, j);
		}

		if (pShader.MODEL_VIEW_MATRIX != null) {
			pShader.MODEL_VIEW_MATRIX.set(pModelViewMatrix);
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
