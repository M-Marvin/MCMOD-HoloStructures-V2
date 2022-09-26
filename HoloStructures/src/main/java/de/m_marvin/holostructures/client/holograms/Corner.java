package de.m_marvin.holostructures.client.holograms;

import java.util.function.Function;

import net.minecraft.core.BlockPos;

public enum Corner {
	
	private Function<Hologram, BlockPos> offsetMapper;
	
	highest_corner((hologram) -> hologram.),lowest_corner(),origin();
	
	Corner(Function<Hologram, BlockPos> offsetMapper) {
		this.offsetMapper = offsetMapper;
	}
	
	public Function<Hologram, BlockPos> getOffsetMapper() {
		return offsetMapper;
	}
	
}
