package de.m_marvin.holostructures.client.holograms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HologramChunk implements LevelHeightAccessor {
	
	protected LevelHeightAccessor levelHeightAccessor;
	protected Map<BlockPos, Blueprint.EntityData> blockentities;
	protected HologramSection[] sections;
	protected ChunkPos position;
	
	public HologramChunk(ChunkPos position, LevelHeightAccessor levelHeightAccessor) {
		this.levelHeightAccessor = levelHeightAccessor;
		this.blockentities = new HashMap<>();
		this.sections = new HologramSection[getSectionsCount()];
		this.position = position;
	}
	
	@Override
	public int getHeight() {
		return this.levelHeightAccessor.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return this.levelHeightAccessor.getMinBuildHeight();
	}
	
	public ChunkPos getPosition() {
		return position;
	}
	
	public HologramSection[] getSections() {
		return sections;
	}
	
	public HologramSection getSection(int y) {
		int index = getSectionIndex(y);
		if (index >= 0 && index < this.sections.length) {
			return this.sections[index];
		} else {
			return null;
		}
	}
	
	public BlockState getBlock(BlockPos position) {
		HologramSection section = getSection(position.getY());
		if (section == null) return Blocks.AIR.defaultBlockState();
		return section.getState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
	}
	
	public void setBlock(BlockPos position, BlockState state) {
		HologramSection section = getSection(position.getY());
		if (section == null) return;
		section.setState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, state);
	}
	
	public Optional<Blueprint.EntityData> getBlockEntitie(BlockPos position) {
		return Optional.ofNullable(this.blockentities.get(new BlockPos(position.getX() & 15, position.getY() & 15, position.getZ() & 15)));
	}
	
	public void setBlockEntity(BlockPos position, Blueprint.EntityData blockentitie) {
		this.blockentities.put(new BlockPos(position.getX() & 15, position.getY() & 15, position.getZ() & 15), blockentitie);
	}
	
}
