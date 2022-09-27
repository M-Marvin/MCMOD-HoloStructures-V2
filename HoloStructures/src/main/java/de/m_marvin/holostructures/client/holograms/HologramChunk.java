package de.m_marvin.holostructures.client.holograms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HologramChunk {
	
	protected Map<BlockPos, BlockEntity> blockentities;
	protected Int2ObjectMap<HologramSection> sections;
	protected ChunkPos position;
	
	public HologramChunk(ChunkPos position) {
		this.blockentities = new HashMap<>();
		this.sections = new Int2ObjectArrayMap<>();
		this.position = position;
	}

//	public int getHighestBlockX() {
//		return Stream.of(getSections()).mapToInt((s) -> s.getHighest(BlockPos::getX)).max().getAsInt();
//	}
//
//	public int getHighestBlockZ() {
//		return Stream.of(getSections()).mapToInt((s) -> s.getHighest(BlockPos::getZ)).max().getAsInt();
//	}
//
//	public int getHighestBlockY() {
//		HologramSection highestSection = null;
//		for (HologramSection section : getSections()) if (!section.isEmpty()) highestSection = section;
//		return highestSection == null ? 0 : highestSection.getHighest(BlockPos::getY);
//	}
	
	public ChunkPos getPosition() {
		return position;
	}
	
	public boolean isEmpty() {
		return this.sections.values().stream().map(HologramSection::isEmpty).reduce((a, b) -> a && b).get();
	}
	
	public Int2ObjectMap<HologramSection> getSections() {
		return sections;
	}
	
	public HologramSection getOrCreateSection(int y) {
		HologramSection section = this.sections.get(y >> 4);
		if (section == null) {
			section = new HologramSection();
			this.sections.put(y >> 4, section);
		}
		return section;
	}
	
	public Optional<HologramSection> getAvailableSection(int y) {
		HologramSection section = this.sections.get(y >> 4);
		if (section != null && section.isEmpty()) {
			this.sections.remove(y >> 4);
			return Optional.empty();
		}
		return Optional.ofNullable(section);
	}
	
	public BlockState getBlock(BlockPos position) {
		Optional<HologramSection> section = getAvailableSection(position.getY());
		if (!section.isPresent()) return Blocks.AIR.defaultBlockState();
		return section.get().getState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
	}
	
	public void setBlock(BlockPos position, BlockState state) {
		HologramSection section = getOrCreateSection(position.getY());
		if (section == null) return;
		section.setState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, state);
	}
	
	public Optional<BlockEntity> getBlockEntity(BlockPos position) {
		return Optional.ofNullable(this.blockentities.get(new BlockPos(position.getX() & 15, position.getY() & 15, position.getZ() & 15)));
	}
	
	public void setBlockEntity(BlockPos position, BlockEntity blockentity) {
		this.blockentities.put(new BlockPos(position.getX() & 15, position.getY() & 15, position.getZ() & 15), blockentity);
	}
	
}
