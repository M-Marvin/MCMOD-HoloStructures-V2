
package de.m_marvin.holostructures.client.holograms;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

public class HologramSection {
	
	protected int nonEmptyBlockCount = 0;
	protected final PalettedContainer<BlockState> states;
		
	@SuppressWarnings("deprecation")
	public HologramSection() {
		this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
	}
	
	public BlockState getState(int x, int y, int z) {
		return this.states.get(x, y, z);
	}
	
	public void setState(int x, int y, int z, BlockState state) {
		BlockState replaced = this.states.getAndSet(x, y, z, state);
		if (replaced.isAir() != state.isAir()) {
			if (state.isAir()) {
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
