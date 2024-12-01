package de.m_marvin.holostruct.client.holograms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An hologram chunk consists of {@link HologramSection}'s.
 * @author Marvin Koehler
 */
public class HologramChunk {
	
	protected Map<BlockPos, BlockEntity> blockentities;
	protected Int2ObjectMap<HologramSection> sections;
	protected ChunkPos position;
	
	public HologramChunk(ChunkPos position) {
		this.blockentities = new HashMap<>();
		this.sections = new Int2ObjectArrayMap<>();
		this.position = position;
	}
	
	public int getLowestAxis(Axis axis) {
		if (axis == Axis.Y) {
			int minSection = IntStream.of(this.sections.keySet().toIntArray()).filter(h -> !this.sections.get(h).isEmpty()).min().getAsInt();
			return getSections().get(minSection).getLowestAxis(axis) | minSection << 4;
		} else {
			return Stream.of(getSections().values().toArray((l) -> new HologramSection[l])).filter(h -> !h.isEmpty()).mapToInt((h) -> h.getLowestAxis(axis)).min().getAsInt() | (axis == Axis.X ? position.x : position.z) << 4;
		}
	}
	
	public int getHighestAxis(Axis axis) {
		if (axis == Axis.Y) {
			int maxSection = IntStream.of(this.sections.keySet().toIntArray()).filter(h -> !this.sections.get(h).isEmpty()).max().getAsInt();
			return getSections().get(maxSection).getHighestAxis(axis) | maxSection << 4;
		} else {
			return Stream.of(getSections().values().toArray((l) -> new HologramSection[l])).filter(h -> !h.isEmpty()).mapToInt((h) -> h.getHighestAxis(axis)).max().getAsInt() | (axis == Axis.X ? position.x : position.z) << 4;
		}
	}
	
	public ChunkPos getPosition() {
		return position;
	}
	
	public boolean isEmpty() {
		return this.sections.values().stream().map(HologramSection::isEmpty).reduce((a, b) -> a && b).orElseGet(() -> true);
	}
	
	public Int2ObjectMap<HologramSection> getSections() {
		return sections;
	}
	
	public Map<BlockPos, BlockEntity> getBlockEntities() {
		return blockentities;
	}
	
	public void removeSection(int y) {
		this.sections.remove(y >> 4);
	}
	
	public Optional<HologramSection> getOrCreateSection(int y, boolean createIfEmpty) {
		HologramSection section = this.sections.get(y >> 4);
		if (section == null && createIfEmpty) {
			section = new HologramSection();
			this.sections.put(y >> 4, section);
		}
		return Optional.ofNullable(section);
	}
	
	public Optional<HologramSection> getAvailableSection(int y) {
		HologramSection section = this.sections.get(y >> 4);
		if (section != null && section.isEmpty()) {
			removeSection(y);
			return Optional.empty();
		}
		return Optional.ofNullable(section);
	}

	public void setHoloState(BlockPos position, BlockHoloState state) {
		Optional<HologramSection> section = getAvailableSection(position.getY());
		if (section.isPresent()) section.get().setHoloState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, state);
	}
	
	public BlockHoloState getHoloState(BlockPos position) {
		Optional<HologramSection> section = getAvailableSection(position.getY());
		if (!section.isPresent()) return BlockHoloState.NO_BLOCK;
		return section.get().getHoloState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
	}
	
	public BlockState getBlock(BlockPos position) {
		Optional<HologramSection> section = getAvailableSection(position.getY());
		if (!section.isPresent()) return Blocks.AIR.defaultBlockState();
		return section.get().getState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
	}
	
	public void setBlock(BlockPos position, BlockState state) {
		Optional<HologramSection> section = getOrCreateSection(position.getY(), !state.isAir());
		if (!section.isPresent()) return;
		section.get().setState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, state);
		if (section.get().isEmpty()) removeSection(position.getY());
	}
	
	public Optional<BlockEntity> getBlockEntity(BlockPos position) {
		return Optional.ofNullable(this.blockentities.get(new BlockPos(position.getX() & 15, position.getY() & 15, position.getZ() & 15)));
	}
	
	public void setBlockEntity(BlockPos position, BlockEntity blockentity) {
		this.blockentities.put(new BlockPos(position.getX() & 15, position.getY(), position.getZ() & 15), blockentity);
	}
	
}
