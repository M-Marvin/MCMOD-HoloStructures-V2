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
	
	public CommandSourceStack getChatTarget();
	public BlockAndTintGetter getLevelGetter();
	
	public boolean hasServerAccess();
	public boolean hasOPAccess();
	public boolean isDoneAccessing();
	public void abbortWaiting();
	
	public void setBlock(BlockPos pos, BlockState state);
	public BlockState getBlock(BlockPos pos);
	public boolean checkBlock(BlockPos pos, BlockState state);
	
	public Optional<Blueprint.EntityData> getBlockEntity(BlockPos pos);
	public void setBlockEntity(BlockPos pos, Blueprint.EntityData data);
	
	public Map<Vec3, EntityData> getEntities(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper);
	public void addEntity(Vec3 pos, Blueprint.EntityData entity);
	
}
