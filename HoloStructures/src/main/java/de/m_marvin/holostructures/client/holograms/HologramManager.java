package de.m_marvin.holostructures.client.holograms;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HologramManager {
	
	Map<String, Hologram> holograms = new HashMap<>();
	
	public Hologram createHologram(Blueprint blueprint, BlockPos position, String name) {
		if (holograms.containsKey(name)) return null;
		Hologram hologram = new Hologram(blueprint, position, name);
		this.holograms.put(name, hologram);
		return hologram;
	}
	
	public boolean removeHologram(String name) {
		return this.holograms.remove(name) != null;
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
	
	public Collection<Hologram> getHologramsWithBlockAt(BlockPos pos) {
		return this.holograms.values().stream().filter((hologram) -> !hologram.getBlueprint().getBlock(hologram.getBlueprintPositionFromWorld(pos)).isAir()).toList();
	}
	
	public List<BlockState> getHologramBlocksAt(BlockPos pos) {
		return getHologramsWithBlockAt(pos).stream().map((hologram) -> hologram.getBlueprint().getBlock(hologram.getBlueprintPositionFromWorld(pos))).toList();
	}

	public List<Optional<EntityData>> getHologramBlockentitesAt(BlockPos pos) {
		return getHologramsWithBlockAt(pos).stream().map((hologram) -> hologram.getBlueprint().getBlockEntityData(hologram.getBlueprintPositionFromWorld(pos))).toList();
	}
	
}
