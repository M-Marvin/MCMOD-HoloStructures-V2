package de.m_marvin.holostruct.client.holograms;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum BlockHoloState {
	
	CORRECT_BLOCK(1, 1, 1, 1),NO_BLOCK(0.7F, 0.7F, 1, 1F),WRONG_BLOCK(1, 0, 0, 1),WRONG_STATE(1, 1, 0, 1),WRONG_DATA(0, 1, 0, 1);
	
	public final float colorRed;
	public final float colorGreen;
	public final float colorBlue;
	public final float colorAlpha;
	
	BlockHoloState(float r, float g, float b, float a) {
		this.colorRed = r;
		this.colorGreen = g;
		this.colorBlue = b;
		this.colorAlpha = a;
	}
	
	public static BlockHoloState getHoloState(BlockStateData targetState, BlockStateData holoState, BlockEntityData targetBlockEntity, BlockEntityData holoBlockEntity) {
		if (holoState.isAir() && targetState.isAir()) {
			return BlockHoloState.CORRECT_BLOCK;
		} else if (!targetState.getBlockName().equals(holoState.getBlockName())) {
			if (targetState.isAir()) {
				return BlockHoloState.NO_BLOCK;
			} else {
				return BlockHoloState.WRONG_BLOCK;
			}
		} else if (!targetState.equals(holoState)) {
			return BlockHoloState.WRONG_STATE;
		} else {
			if (Objects.equal(targetBlockEntity, holoBlockEntity)) {
				return BlockHoloState.CORRECT_BLOCK;
			} else {
				return BlockHoloState.WRONG_DATA;
			}
		}
	}
	
	public static BlockHoloState getHoloState(BlockState holoState, Optional<BlockEntity> holoBlockEntity, BlockState realState, Optional<BlockEntity> realBlockEntity) {
		if (realState.isAir()) {
			return NO_BLOCK;
		} else if (holoState.getBlock() != realState.getBlock()) {
			return WRONG_BLOCK;
		} else if (!holoState.equals(realState)) {
			return WRONG_STATE;
		} else if (holoState.hasBlockEntity()) {
			CompoundTag holoNbt = holoBlockEntity.isPresent() ? holoBlockEntity.get().serializeNBT() : new CompoundTag();
			CompoundTag realNbt = realBlockEntity.isPresent() ? realBlockEntity.get().serializeNBT() : new CompoundTag();
			if (!holoNbt.equals(realNbt)) return WRONG_DATA;
		}
		return CORRECT_BLOCK;
	}
	
	public static List<BlockHoloState> renderedStates() {
		return ImmutableList.of(NO_BLOCK, WRONG_BLOCK, WRONG_STATE, WRONG_DATA);
	}
	
}
