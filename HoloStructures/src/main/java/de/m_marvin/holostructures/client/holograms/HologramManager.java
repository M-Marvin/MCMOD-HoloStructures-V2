package de.m_marvin.holostructures.client.holograms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import net.minecraft.core.BlockPos;

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
	
}
