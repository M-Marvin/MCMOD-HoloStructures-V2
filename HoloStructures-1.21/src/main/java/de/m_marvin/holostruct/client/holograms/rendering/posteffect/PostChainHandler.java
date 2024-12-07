package de.m_marvin.holostruct.client.holograms.rendering.posteffect;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class PostChainHandler {

	private static boolean injectSelectiveOverride = false;
	
	public static boolean isInjectSelectiveOverrideEnabled() {
		return injectSelectiveOverride;
	}
	
	private PostChain activePostEffect;
    private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
	private FrameGraphBuilder frameGraph;
	private Map<ResourceLocation, ResourceHandle<RenderTarget>> targets = new HashMap<>();
    
	public void setPostChain(PostChain chain) {
		this.activePostEffect = chain;
	}
	
	public boolean hasPostChain() {
		return this.activePostEffect != null;
	}
	
	public PostChain getPostChain() {
		return activePostEffect;
	}
	
	public void registerTarget(ResourceLocation name, RenderTargetDescriptor descriptor) {
		this.targets.put(name, this.frameGraph.createInternal(name.getPath(), descriptor));
	}

	public void registerExternalTarget(ResourceLocation name, RenderTarget target) {
		this.targets.put(name, this.frameGraph.importExternal(name.getPath(), target));
	}
    
	public RenderTarget getTarget(ResourceLocation name) {
		if (this.targets.containsKey(name)) {
			return this.targets.get(name).get();
		}
		return null;
	}
	
	public void prepareForFrame() {
      this.targets.clear();
		this.frameGraph = new FrameGraphBuilder();
	}
	
	public void applyToFrame(RenderTarget target) {
        // Enable PostChainSelectiveInjection
		injectSelectiveOverride = true;
		
		MapTargetBundle targets = new MapTargetBundle();
		for (var entry : this.targets.entrySet()) {
			targets.addTarget(entry.getKey(), entry.getValue());
		}
		
        this.activePostEffect.addToFrame(this.frameGraph, target.width, target.height, targets);
        this.frameGraph.execute(this.resourcePool);
        
        // Disable PostChainSelectiveInjection
		injectSelectiveOverride = false;
	}
	
}
