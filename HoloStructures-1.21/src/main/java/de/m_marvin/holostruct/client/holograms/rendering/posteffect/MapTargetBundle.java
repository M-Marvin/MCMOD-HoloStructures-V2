package de.m_marvin.holostruct.client.holograms.rendering.posteffect;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class MapTargetBundle implements PostChain.TargetBundle {

	protected final Map<ResourceLocation, ResourceHandle<RenderTarget>> handels = new HashMap<>();
	
	public void addTarget(ResourceLocation name, ResourceHandle<RenderTarget> handle) {
		this.handels.put(name, handle);
	}
	
	@Override
	public void replace(ResourceLocation p_362165_, ResourceHandle<RenderTarget> p_362344_) {
		if (!this.handels.containsKey(p_362165_)) {
            throw new IllegalArgumentException("No target with id " + p_362165_);
		}
		this.handels.put(p_362165_, p_362344_);
	}

	@Override
	public ResourceHandle<RenderTarget> get(ResourceLocation p_364001_) {
		return this.handels.get(p_364001_);
	}

}
