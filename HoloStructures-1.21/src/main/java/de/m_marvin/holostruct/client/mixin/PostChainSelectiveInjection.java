package de.m_marvin.holostruct.client.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

import de.m_marvin.holostruct.client.holograms.rendering.posteffect.PostChainHandler;
import net.minecraft.client.renderer.PostPass;

//@Debug(export = true)
@Mixin(PostPass.class)
public abstract class PostChainSelectiveInjection {

	@WrapOperation(
			method = "lambda$addToFrame$1",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthFunc(I)V")
	)
	private void enableDepthTesting(int originalGlMode, Operation<Void> original) {
		if (PostChainHandler.isInjectSelectiveOverrideEnabled() && originalGlMode == 519) {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LESS);
			RenderSystem.depthMask(true);
		} else {
			RenderSystem.disableDepthTest();
			original.call(originalGlMode);
			RenderSystem.depthMask(false);
		}
	}

	@WrapWithCondition(
			method = "lambda$addToFrame$1",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;clear()V")
	)
	private boolean bypassMainFramebufferClear(RenderTarget instance) {
		if (PostChainHandler.isInjectSelectiveOverrideEnabled() && instance instanceof MainTarget) return false;
		return true;
	}
	
// For developement only, override allows quick modification and testing, does the same as the above modifiers
//	@Shadow
//	private String name;
//	@Shadow
//	private CompiledShaderProgram shader;
//	@Shadow
//	private ResourceLocation outputTargetId;
//	@Shadow
//	private List<PostChainConfig.Uniform> uniforms;
//	@Shadow
//	private List<PostPass.Input> inputs;
//
//	@Shadow
//	private void restoreDefaultUniforms() { }
//	
//	public void addToFrame(FrameGraphBuilder graphBuilder, Map<ResourceLocation, ResourceHandle<RenderTarget>> targetHandle, Matrix4f projectionMatrix) {
//		FramePass framepass = graphBuilder.addPass(this.name);
//
//		for (PostPass.Input postpass$input : this.inputs) {
//			postpass$input.addToPass(framepass, targetHandle);
//		}
//
//		ResourceHandle<RenderTarget> resourcehandle = targetHandle.computeIfPresent(
//			this.outputTargetId, (p_362663_, p_363989_) -> framepass.readsAndWrites((ResourceHandle<RenderTarget>)p_363989_)
//		);
//		if (resourcehandle == null) {
//			throw new IllegalStateException("Missing handle for target " + this.outputTargetId);
//		} else {
//			framepass.executes(() -> {
//				RenderTarget rendertarget = resourcehandle.get();
//				RenderSystem.viewport(0, 0, rendertarget.width, rendertarget.height);
//
//				for (PostPass.Input postpass$input1 : this.inputs) {
//					postpass$input1.bindTo(this.shader, targetHandle);
//				}
//
//				this.shader.safeGetUniform("OutSize").set((float)rendertarget.width, (float)rendertarget.height);
//
//				for (PostChainConfig.Uniform postchainconfig$uniform : this.uniforms) {
//					Uniform uniform = this.shader.getUniform(postchainconfig$uniform.name());
//					if (uniform != null) {
//						uniform.setFromConfig(postchainconfig$uniform.values(), postchainconfig$uniform.values().size());
//					}
//				}
//
//				rendertarget.setClearColor(0.0F, 1.0F, 0.0F, 1.0F);
//
//				/* HS2 Modification: Don't clear main render target, this would override everything */
//				if (!(rendertarget instanceof MainTarget))
//					rendertarget.clear();
//				
//				rendertarget.bindWrite(false);
//				
//				/* HS2 Modification: Don't use GL_ALWAS, we want depth testing to work when adding stuff */
//				RenderSystem.enableDepthTest();
//				RenderSystem.depthFunc(GL11.GL_LESS);
//				RenderSystem.depthMask(true);
//				
//				RenderSystem.setShader(this.shader);
//				RenderSystem.backupProjectionMatrix();
//				RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.ORTHOGRAPHIC);
//				BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
//				bufferbuilder.addVertex(0.0F, 0.0F, 500.0F);
//				bufferbuilder.addVertex((float)rendertarget.width, 0.0F, 500.0F);
//				bufferbuilder.addVertex((float)rendertarget.width, (float)rendertarget.height, 500.0F);
//				bufferbuilder.addVertex(0.0F, (float)rendertarget.height, 500.0F);
//				BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
//
//				/* HS2 Modification: Undo modifications */
//				RenderSystem.disableDepthTest();
//				RenderSystem.depthFunc(515);
//				RenderSystem.depthMask(false);
//				
//				RenderSystem.restoreProjectionMatrix();
//				rendertarget.unbindWrite();
//
//				for (PostPass.Input postpass$input2 : this.inputs) {
//					postpass$input2.cleanup(targetHandle);
//				}
//
//				this.restoreDefaultUniforms();
//			});
//		}
//	}
	
}
