package de.m_marvin.holostruct.client.struktedit;

import java.util.HashMap;
import java.util.Map;

import com.mojang.math.OctahedralGroup;

import de.m_marvin.holostruct.utility.UtilHelper;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StruktOrientator {
	
	public static void rotate(LevelAccessor source, BlockPos origin, BlockPos boundsMin, BlockPos boundsMax, Rotation rotation) {
		
		Map<BlockPos, BlockState> newStates = new HashMap<>();
		Map<BlockPos, CompoundTag> newBlockEntities = new HashMap<>();
		
		for (int y = boundsMin.getY(); y <= boundsMax.getY(); y++) {
			for (int z = boundsMin.getZ(); z <= boundsMax.getZ(); z++) {
				for (int x = boundsMin.getX(); x <= boundsMax.getX(); x++) {
					BlockPos scanPos = new BlockPos(x, y, z);
					
					BlockState state = source.getBlockState(scanPos);
					BlockEntity blockEntity = state.hasBlockEntity() ? source.getBlockEntity(scanPos) : null;
					
					BlockPos newPos = origin.offset(scanPos.subtract(origin).rotate(rotation));
					state = state.rotate(source, scanPos, rotation);
					
					newStates.put(newPos, state);
					newBlockEntities.put(newPos, blockEntity == null ? null : blockEntity.serializeNBT());
					
					source.removeBlock(scanPos, false);
				}
			}
		}
		
		for (BlockPos scanPos : newStates.keySet()) {
			
			BlockState state = newStates.get(scanPos);
			CompoundTag blockEntity = newBlockEntities.get(scanPos);
			
			source.setBlock(scanPos, state, 2);
			if (blockEntity != null) {
				BlockEntity newBlockEntity = source.getBlockEntity(scanPos);
				if (newBlockEntity != null) {
					newBlockEntity.deserializeNBT(blockEntity);
				}
			}
			
		}

		Vec3d originv = Vec3d.fromVec(origin).add(0.5, 0.5, 0.5);
		
		for (Entity entity : source.getEntities(null, AABB.encapsulatingFullBlocks(boundsMin, boundsMax))) {
			
			Vec3d pos = Vec3d.fromVec(entity.position());
			Vec3d originRelative = pos.sub(originv);
			Vec3d newPos = UtilHelper.rotate(originRelative, rotation).add(originv);
			
			entity.setPos(newPos.writeTo(new Vec3(0, 0, 0)));
			
		}
		
	}

	public static void mirror(LevelAccessor source, BlockPos origin, BlockPos boundsMin, BlockPos boundsMax, Mirror mirror) {

		Map<BlockPos, BlockState> newStates = new HashMap<>();
		Map<BlockPos, CompoundTag> newBlockEntities = new HashMap<>();
		
		for (int y = boundsMin.getY(); y <= boundsMax.getY(); y++) {
			for (int z = boundsMin.getZ(); z <= boundsMax.getZ(); z++) {
				for (int x = boundsMin.getX(); x <= boundsMax.getX(); x++) {
					BlockPos scanPos = new BlockPos(x, y, z);
					
					BlockState state = source.getBlockState(scanPos);
					BlockEntity blockEntity = state.hasBlockEntity() ? source.getBlockEntity(scanPos) : null;
					
					BlockPos originRelative = scanPos.subtract(origin);
					if (mirror.rotation() == OctahedralGroup.INVERT_X) {
						originRelative = new BlockPos(-originRelative.getX(), originRelative.getY(), originRelative.getZ());
					} else if (mirror.rotation() == OctahedralGroup.INVERT_Z) {
						originRelative = new BlockPos(originRelative.getX(), originRelative.getY(), -originRelative.getZ());
					}
					BlockPos newPos = origin.offset(originRelative);
					state = state.mirror(mirror);
					
					newStates.put(newPos, state);
					newBlockEntities.put(newPos, blockEntity == null ? null : blockEntity.serializeNBT());
					
					source.removeBlock(scanPos, false);
				}
			}
		}
		
		for (BlockPos scanPos : newStates.keySet()) {
			
			BlockState state = newStates.get(scanPos);
			CompoundTag blockEntity = newBlockEntities.get(scanPos);
			
			source.setBlock(scanPos, state, 2);
			if (blockEntity != null) {
				BlockEntity newBlockEntity = source.getBlockEntity(scanPos);
				if (newBlockEntity != null) {
					newBlockEntity.deserializeNBT(blockEntity);
				}
			}
			
		}

		Vec3d originv = Vec3d.fromVec(origin).add(0.5, 0.5, 0.5);
		
		for (Entity entity : source.getEntities(null, AABB.encapsulatingFullBlocks(boundsMin, boundsMax))) {
			
			Vec3d pos = Vec3d.fromVec(entity.position());
			Vec3d originRelative = pos.sub(originv);
			if (mirror.rotation() == OctahedralGroup.INVERT_X) {
				originRelative = new Vec3d(-originRelative.getX(), originRelative.getY(), originRelative.getZ());
			} else if (mirror.rotation() == OctahedralGroup.INVERT_Z) {
				originRelative = new Vec3d(originRelative.getX(), originRelative.getY(), -originRelative.getZ());
			}
			Vec3d newPos = originRelative.add(originv);
			
			entity.setPos(newPos.writeTo(new Vec3(0, 0, 0)));
			
		}
		
	}
	
}
