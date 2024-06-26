package de.m_marvin.holostruct.client.holograms;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class HologramManager {
	
	public static final Supplier<ClientLevel> CLIENT_LEVEL = () -> Minecraft.getInstance().level;
	
	protected Thread workerThread;
	protected Map<String, Hologram> holograms = new HashMap<>();
	protected int activeLayer = 0;
	protected boolean oneLayerMode = false;
	
	public int getActiveLayer() {
		return activeLayer;
	}
	
	public void setActiveLayer(int activeLayer) {
		this.activeLayer = activeLayer;
		redrawAllHolograms();
	}
	
	public boolean isOneLayerMode() {
		return oneLayerMode;
	}
	
	public void setOneLayerMode(boolean oneLayerMode) {
		this.oneLayerMode = oneLayerMode;
		redrawAllHolograms();
	}
	
	public Hologram createHologram(@Nullable Blueprint blueprint, BlockPos position, String name) {
		if (holograms.containsKey(name)) return null;
		Hologram hologram = new Hologram(CLIENT_LEVEL.get(), position);
		if (blueprint != null) {
			blueprint.copyTo(hologram);
		} else {
			hologram.setBlock(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
		}
		addHologram(name, hologram);
		return hologram;
	}
	
	public void addHologram(String name, Hologram hologram) {
		this.holograms.put(name, hologram);
		HoloStruct.CLIENT.HOLORENDERER.addHologram(hologram);
		hologram.markDirtyAllChunks();
	}
	
	public boolean removeHologram(String name) {
		Hologram hologram = this.holograms.remove(name);
		if (hologram != null) {
			HoloStruct.CLIENT.HOLORENDERER.discardHologram(hologram);
			return true;
		}
		return false;
	}
	
	public Hologram getHologram(String name) {
		return this.holograms.get(name);
	}
	
	public Map<String, Hologram> getHolograms() {
		return holograms;
	}
	
	public void updateHoloSectionAt(BlockPos position) {
		if (!HoloStruct.CLIENT.LEVELBOUND.getAccessLevel().hasRead()) return;
		getHolograms().values().forEach(hologram -> {
			Vec3i holoPosition = Vec3i.fromVec(position.subtract(hologram.getPosition().subtract(hologram.getOrigin())));
			if (!hologram.isInBounds(holoPosition)) return;
			hologram.updateHoloStateAt(HoloStruct.CLIENT.LEVELBOUND.getAccessor(), holoPosition);
		});
	}
	
	public void redrawAllHolograms() {
		getHolograms().values().forEach(Hologram::markDirtyAllChunks);
	}
	
}
