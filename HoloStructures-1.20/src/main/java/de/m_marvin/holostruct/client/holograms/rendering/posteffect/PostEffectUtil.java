package de.m_marvin.holostruct.client.holograms.rendering.posteffect;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;

public class PostEffectUtil {
	
	public static void preparePostEffect(SelectivePostChain postEffect) {
		postEffect.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}
	
	public static void clearFramebuffer(RenderTarget framebuffer) {	
		framebuffer.bindWrite(true);
		framebuffer.clear(Minecraft.ON_OSX);
	}
	
	public static void bindFramebuffer(RenderTarget framebuffer) {
		framebuffer.bindWrite(true);
	}
	
	public static void unbindFramebuffer(RenderTarget framebuffer) {
		framebuffer.unbindWrite();
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}

	@SuppressWarnings("resource")
	public static void forceRunOnCurrentFramebuffer() {
		Minecraft.getInstance().gameRenderer.setPanoramicMode(true);
	}

	@SuppressWarnings("resource")
	public static void resetRunOnCurrentFramebuffer() {
		Minecraft.getInstance().gameRenderer.setPanoramicMode(false);
	}
	
}
