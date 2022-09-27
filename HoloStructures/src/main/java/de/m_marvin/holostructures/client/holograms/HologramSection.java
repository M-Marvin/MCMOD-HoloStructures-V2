
package de.m_marvin.holostructures.client.holograms;

import java.util.OptionalInt;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HologramSection {
	
	protected int nonEmptyBlockCount = 0;
	protected BlockPos highestPosition = BlockPos.ZERO;
	protected final Long2ObjectMap<BlockState> states;
	
	public HologramSection() {
		this.states = new Long2ObjectArrayMap<>();
	}
	
	public int getHighest(ToIntFunction<Long> coordReader) {
		OptionalInt highest = Stream.of(this.states.keySet().toArray((l) -> new Long[l])).mapToInt(coordReader).max();
		return highest.isPresent() ? highest.getAsInt() : 0;
	}
	
	public BlockState getState(int x, int y, int z) {
		return this.states.getOrDefault(BlockPos.asLong(x, y, z), Blocks.AIR.defaultBlockState());
	}
	
	public void setState(int x, int y, int z, BlockState state) {
		BlockState replaced = this.states.put(BlockPos.asLong(x, y, z),  state);
		if (replaced == null || replaced.isAir() != state.isAir()) {
			if (state.isAir() && replaced != null) {
				nonEmptyBlockCount--;
			} else {
				nonEmptyBlockCount++;
			}
		}
	}
	
	public int getNonEmptyBlockCount() {
		return nonEmptyBlockCount;
	}
	
	public boolean isEmpty() {
		return this.nonEmptyBlockCount == 0;
	}
	
}
