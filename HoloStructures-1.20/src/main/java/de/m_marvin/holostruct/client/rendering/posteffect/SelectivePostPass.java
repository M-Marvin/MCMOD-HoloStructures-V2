package de.m_marvin.holostruct.client.rendering.posteffect;

import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SelectivePostPass implements AutoCloseable {
	private final EffectInstance effect;
	public final RenderTarget inTarget;
	public final RenderTarget outTarget;
	private final List<IntSupplier> auxAssets = Lists.newArrayList();
	private final List<String> auxNames = Lists.newArrayList();
	private final List<Integer> auxWidths = Lists.newArrayList();
	private final List<Integer> auxHeights = Lists.newArrayList();
	private Matrix4f shaderOrthoMatrix;

	public SelectivePostPass(ResourceManager pResourceManager, String pName, RenderTarget pInTarget, RenderTarget pOutTarget) throws IOException {
		this.effect = new EffectInstance(pResourceManager, pName);
		this.inTarget = pInTarget;
		this.outTarget = pOutTarget;
	}

	@Override
	public void close() {
		this.effect.close();
	}

	public final String getName() {
		return this.effect.getName();
	}

	public void addAuxAsset(String pAuxName, IntSupplier pAuxFramebuffer, int pWidth, int pHeight) {
		this.auxNames.add(this.auxNames.size(), pAuxName);
		this.auxAssets.add(this.auxAssets.size(), pAuxFramebuffer);
		this.auxWidths.add(this.auxWidths.size(), pWidth);
		this.auxHeights.add(this.auxHeights.size(), pHeight);
	}

	public void setOrthoMatrix(Matrix4f pShaderOrthoMatrix) {
		this.shaderOrthoMatrix = pShaderOrthoMatrix;
	}

	public void process(float pPartialTicks) {
		this.inTarget.unbindWrite();
		float f = (float)this.outTarget.width;
		float f1 = (float)this.outTarget.height;
		RenderSystem.viewport(0, 0, (int)f, (int)f1);
		this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);
		
		/* HS2 Modification: Pass depth buffer to post effect shader */
		this.effect.setSampler("DepthSampler", this.inTarget::getDepthTextureId);
		
		for(int i = 0; i < this.auxAssets.size(); ++i) {
			this.effect.setSampler(this.auxNames.get(i), this.auxAssets.get(i));
			this.effect.safeGetUniform("AuxSize" + i).set((float)this.auxWidths.get(i).intValue(), (float)this.auxHeights.get(i).intValue());
		}

		this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
		this.effect.safeGetUniform("InSize").set((float)this.inTarget.width, (float)this.inTarget.height);
		this.effect.safeGetUniform("OutSize").set(f, f1);
		this.effect.safeGetUniform("Time").set(pPartialTicks);
		Minecraft minecraft = Minecraft.getInstance();
		this.effect.safeGetUniform("ScreenSize").set((float)minecraft.getWindow().getWidth(), (float)minecraft.getWindow().getHeight());
		
		/* HS2 Modification: Reset BlendMode#lastApplied since it for some reason gets not reset automatically */
		new BlendMode().apply();
		
		this.effect.apply();
		
		/* HS2 Modification: Don't clear main framebuffer, we are going to ADD stuff, not replace */
		if (!(this.outTarget instanceof MainTarget))
			this.outTarget.clear(Minecraft.ON_OSX);
		
		this.outTarget.bindWrite(false);
		
		/* HS2 Modification: Don't use GL_ALWAS, we want depth testing to work when adding stuff */
		RenderSystem.depthFunc(GL30.GL_LEQUAL);
		
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		bufferbuilder.vertex(0.0, 0.0, 500.0).endVertex();
		bufferbuilder.vertex((double)f, 0.0, 500.0).endVertex();
		bufferbuilder.vertex((double)f, (double)f1, 500.0).endVertex();
		bufferbuilder.vertex(0.0, (double)f1, 500.0).endVertex();
		BufferUploader.draw(bufferbuilder.end());
		RenderSystem.depthFunc(515);
		this.effect.clear();
		
		/* HS2 Modification: Copy over the depth data to temp framebuffers */
		/* For some reason the depth data gets lost when using a temp buffer as target */
		if (!(this.outTarget instanceof MainTarget))
			this.outTarget.copyDepthFrom(inTarget);
		
		this.outTarget.unbindWrite();
		this.inTarget.unbindRead();

		for(Object object : this.auxAssets) {
			if (object instanceof RenderTarget) {
				((RenderTarget)object).unbindRead();
			}
		}
	}

	public EffectInstance getEffect() {
		return this.effect;
	}
}
