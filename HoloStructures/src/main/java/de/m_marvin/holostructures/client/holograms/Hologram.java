package de.m_marvin.holostructures.client.holograms;

import java.util.function.BiFunction;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import net.minecraft.core.BlockPos;

public class Hologram {
	
	public static enum Corner {
		LOW_CORNER((pos, blueprint) -> pos),
		HIGH_CORNER((pos, blueprint) -> pos.offset(blueprint.getSize())),
		ORIGIN((pos, blueprint) -> pos.offset(blueprint.getOrigin()));
		
		private final BiFunction<BlockPos, Blueprint, BlockPos> posMapper;
		Corner(final BiFunction<BlockPos, Blueprint, BlockPos> posMapper) {
			this.posMapper = posMapper;
		}
		public BlockPos mapPosition(BlockPos pos, Blueprint blueprint) {
			return this.posMapper.apply(pos, blueprint);
		}
	}
	
	public Blueprint blueprint;
	public BlockPos position;
	public String name;
	
	public Hologram(Blueprint blueprint, BlockPos position, String name) {
		this.blueprint = blueprint;
		this.position = position;
		this.name = name;
	}
	
	public Blueprint getBlueprint() {
		return blueprint;
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public String getName() {
		return name;
	}
	
	public BlockPos getInternPosition(Corner corner, BlockPos pos) {
		return corner.mapPosition(pos, this.blueprint);
	}

	public void setPosition(BlockPos position) {
		this.position = position;
	}
	
}
