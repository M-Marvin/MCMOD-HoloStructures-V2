
package de.m_marvin.holostruct.client.holograms;

import java.util.OptionalInt;
import java.util.function.ToIntFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An hologram section is the smallest unit of an hologram.
 * @author Marvin Koehler
 */
public class HologramSection {
	
	protected int nonEmptyBlockCount = 0;
	protected final Long2ObjectMap<BlockState> states;
	protected final Long2ObjectMap<BlockHoloState> holoState;
	
	public HologramSection() {
		this.states = new Long2ObjectArrayMap<BlockState>();
		this.holoState = new Long2ObjectArrayMap<BlockHoloState>();
	}
	
	public int getHighest(ToIntFunction<Long> coordReader) {
		OptionalInt highest = Stream.of(this.states.keySet().toArray((l) -> new Long[l])).mapToInt(coordReader).max();
		return highest.isPresent() ? highest.getAsInt() : 0;
	}
	
	public BlockState getState(int x, int y, int z) {
		return this.states.getOrDefault(BlockPos.asLong(x & 15, y & 15, z & 15), Blocks.AIR.defaultBlockState());
	}
	
	public void setState(int x, int y, int z, BlockState state) {
		BlockState replaced = this.states.put(BlockPos.asLong(x & 15, y & 15, z & 15),  state);
		boolean ra = replaced == null || replaced.isAir();
		boolean na = state.isAir();
		if (ra != na) {
			if (na) {
				this.nonEmptyBlockCount--;
				this.holoState.remove(BlockPos.asLong(x & 15, y & 15, z & 15));
			} else {
				this.nonEmptyBlockCount++;
			}
		}
	}

	public BlockHoloState getHoloState(int x, int y, int z) {
		BlockHoloState state = this.holoState.get(BlockPos.asLong(x & 15, y & 15, z & 15));
		return state == null ? BlockHoloState.NO_BLOCK : state;
	}

	public void setHoloState(int x, int y, int z, BlockHoloState state) {
		this.holoState.put(BlockPos.asLong(x & 15, y & 15, z & 15), state);
	}

	public Long2ObjectMap<BlockState> getStates() {
		return this.states;
	}
	
	public Long2ObjectMap<BlockHoloState> getHoloStates() {
		return this.holoState;
	}
	
	public int getNonEmptyBlockCount() {
		return nonEmptyBlockCount;
	}
	
	public boolean isEmpty() {
		return this.nonEmptyBlockCount == 0;
	}

	public int getLowestAxis(Axis axis) {
		return LongStream.of(this.states.keySet().toLongArray()).filter(k -> !this.states.get(k).isAir()).mapToInt((p) -> BlockPos.of(p).get(axis)).min().orElseGet(() -> -1);
	}

	public int getHighestAxis(Axis axis) {
		return LongStream.of(this.states.keySet().toLongArray()).filter(k -> !this.states.get(k).isAir()).mapToInt((p) -> BlockPos.of(p).get(axis)).max().orElseGet(() -> -1);
	}
	
}
