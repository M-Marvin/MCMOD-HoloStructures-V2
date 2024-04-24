package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ServerLevelAccessorImpl implements IRemoteLevelAccessor {

	@Override
	public CompletableFuture<Boolean> setBlock(Vec3i position, BlockStateData state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<BlockStateData> getBlock(Vec3i position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<BlockEntityData> getBlockEntity(Vec3i position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addEntity(EntityData entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addEntities(Collection<EntityData> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesOnBlock(Vec3i pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesWithin(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> getEntitiesInBounds(AABB bounds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBlock(BlockPos pos, BlockState sate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BlockState getBlock(BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LevelAccessor getLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccessLevel getAccessLevel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
