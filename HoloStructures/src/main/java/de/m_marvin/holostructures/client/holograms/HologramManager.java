package de.m_marvin.holostructures.client.holograms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class HologramManager {
	
	public static final Supplier<ClientLevel> CLIENT_LEVEL = () -> Minecraft.getInstance().level;
	
	Map<String, Hologram> holograms = new HashMap<>();
	
	public Hologram createHologram(@Nullable Blueprint blueprint, BlockPos position, String name, boolean includeEntities) {
		if (holograms.containsKey(name)) return null;
		Hologram hologram = new Hologram(CLIENT_LEVEL.get(), position, name);
		if (blueprint != null) {
			ClientHandler.getInstance().getBlueprints().pasteClipboard(hologram, BlockPos.ZERO, includeEntities, null);
		} else {
			hologram.setBlock(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
		}
		addHologram(name, hologram);
		return hologram;
	}
	
	public void addHologram(String name, Hologram hologram) {
		this.holograms.put(name, hologram);
		HolographicRenderer.addHologram(hologram);
		hologram.refreshAllChunks();
	}
	
	public boolean removeHologram(String name) {
		Hologram hologram = this.holograms.remove(name);
		if (hologram != null) {
			HolographicRenderer.discardHologram(hologram);
			return true;
		}
		return false;
	}
	
	public Hologram getHologram(String name) {
		return this.holograms.get(name);
	}

	public String[] getHologramNames() {
		return this.holograms.keySet().toArray((l) -> new String[l]);
	}
	
	public Collection<Hologram> getHolograms() {
		return holograms.values();
	}
		
}
