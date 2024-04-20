package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import java.util.Collection;
import java.util.List;

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
	public void setOffset(Vec3i offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vec3i getOffset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBounds(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vec3i getBoundsMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vec3i getBoundsMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBlock(Vec3i positon, BlockStateData state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BlockStateData getBlock(Vec3i position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEntity(EntityData entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<EntityData> getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copyTo(IStructAccessor target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AccessLevel getAccessLevel() {
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
	
}
