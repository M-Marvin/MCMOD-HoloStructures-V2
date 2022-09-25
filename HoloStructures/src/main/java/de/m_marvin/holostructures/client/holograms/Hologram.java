package de.m_marvin.holostructures.client.holograms;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class Hologram {
	
	public BlockPos position;
	public String name;
	public List<HologramChunk> chunks;
	public Level level;
	
	public Hologram(Level level, BlockPos position, String name) {
		this.level = level;
		this.position = position;
		this.name = name;
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}
	
	
	
}
