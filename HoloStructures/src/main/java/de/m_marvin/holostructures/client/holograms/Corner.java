package de.m_marvin.holostructures.client.holograms;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public enum Corner {
	
	highest_corner(Hologram::getBoundingSize),lowest_corner((h) -> BlockPos.ZERO),origin(Hologram::getOrigin);
	
	private Function<Hologram, Vec3i> offsetMapper;
	
	Corner(Function<Hologram, Vec3i> offsetMapper) {
		this.offsetMapper = offsetMapper;
	}
	
	public Function<Hologram, Vec3i> getOffsetMapper() {
		return offsetMapper;
	}
	
	public Vec3i map(Hologram hologram) {
		return this.offsetMapper.apply(hologram);
	}
	
}
