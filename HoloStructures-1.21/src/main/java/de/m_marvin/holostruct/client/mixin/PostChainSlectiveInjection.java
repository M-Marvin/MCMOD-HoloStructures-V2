package de.m_marvin.holostruct.client.mixin;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

@Mixin(PostPass.class)
public abstract class PostChainSlectiveInjection {

	@Shadow
	private String name;
	@Shadow
	private CompiledShaderProgram shader;
	@Shadow
	private ResourceLocation outputTargetId;
	@Shadow
	private List<PostChainConfig.Uniform> uniforms;
	@Shadow
	private List<PostPass.Input> inputs;

	@Shadow
	private void restoreDefaultUniforms() { }
	
	// TODO improve post chain mixin
	public void addToFrame(FrameGraphBuilder p_361477_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_364596_, Matrix4f p_365068_) {
		FramePass framepass = p_361477_.addPass(this.name);

		for (PostPass.Input postpass$input : this.inputs) {
			postpass$input.addToPass(framepass, p_364596_);
		}

		ResourceHandle<RenderTarget> resourcehandle = p_364596_.computeIfPresent(
			this.outputTargetId, (p_362663_, p_363989_) -> framepass.readsAndWrites((ResourceHandle<RenderTarget>)p_363989_)
		);
		if (resourcehandle == null) {
			throw new IllegalStateException("Missing handle for target " + this.outputTargetId);
		} else {
			framepass.executes(() -> {
				RenderTarget rendertarget = resourcehandle.get();
				RenderSystem.viewport(0, 0, rendertarget.width, rendertarget.height);

				for (PostPass.Input postpass$input1 : this.inputs) {
					postpass$input1.bindTo(this.shader, p_364596_);
				}

				this.shader.safeGetUniform("OutSize").set((float)rendertarget.width, (float)rendertarget.height);

				for (PostChainConfig.Uniform postchainconfig$uniform : this.uniforms) {
					Uniform uniform = this.shader.getUniform(postchainconfig$uniform.name());
					if (uniform != null) {
						uniform.setFromConfig(postchainconfig$uniform.values(), postchainconfig$uniform.values().size());
					}
				}

				rendertarget.setClearColor(0.0F, 1.0F, 0.0F, 1.0F);

				/* HS2 Modification: Don't clear main render target, this would override everything */
				if (!(rendertarget instanceof MainTarget))
					rendertarget.clear();
				
				rendertarget.bindWrite(false);
				
				/* HS2 Modification: Don't use GL_ALWAS, we want depth testing to work when adding stuff */
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_LESS);
				RenderSystem.depthMask(true);
				
				RenderSystem.setShader(this.shader);
				RenderSystem.backupProjectionMatrix();
				RenderSystem.setProjectionMatrix(p_365068_, ProjectionType.ORTHOGRAPHIC);
				BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferbuilder.addVertex(0.0F, 0.0F, 500.0F);
				bufferbuilder.addVertex((float)rendertarget.width, 0.0F, 500.0F);
				bufferbuilder.addVertex((float)rendertarget.width, (float)rendertarget.height, 500.0F);
				bufferbuilder.addVertex(0.0F, (float)rendertarget.height, 500.0F);
				BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
				RenderSystem.depthFunc(515);
				RenderSystem.restoreProjectionMatrix();
				rendertarget.unbindWrite();

				for (PostPass.Input postpass$input2 : this.inputs) {
					postpass$input2.cleanup(p_364596_);
				}

				this.restoreDefaultUniforms();
			});
		}
	}
	
}
