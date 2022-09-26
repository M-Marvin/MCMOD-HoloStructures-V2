package de.m_marvin.holostructures.client.worldaccess;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Function;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface ILevelAccessor {
	
	public BlockAndTintGetter getLevelGetter();
	
	public boolean isMutable();
	public boolean hasWriteAccess();
	public boolean isDoneAccessing();
	public void abbortAccessing();
	
	public void setBlock(BlockPos pos, BlockState state);
	public BlockState getBlock(BlockPos pos);
	public boolean checkBlock(BlockPos pos, BlockState state);
	
	public Optional<Blueprint.EntityData> getBlockEntityData(BlockPos pos);
	public void setBlockEntityData(BlockPos pos, Blueprint.EntityData data);
	
	public Map<Vec3, EntityData> getEntitiesData(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper);
	public void addEntityData(Vec3 pos, Blueprint.EntityData entity);
	
}
