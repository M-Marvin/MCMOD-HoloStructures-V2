package de.m_marvin.holostruct.client.levelbound.access.clientlevel;

import java.util.Collection;
import java.util.List;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ClientLevelAccessorImpl implements IRemoteLevelAccessor {

	private Minecraft minecraft;
	
	public ClientLevelAccessorImpl(Minecraft minecraft) {
		this.minecraft = minecraft;
	}
	
	@Override
	public AccessLevel getAccessLevel() {
		return AccessLevel.READ_CLIENT;
	}
	
	@Override
	public void setBlock(Vec3i positon, BlockStateData state) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public BlockStateData getBlock(Vec3i position) {
		BlockState state = minecraft.level.getBlockState(new BlockPos(position.x, position.y, position.z));
		return TypeConverter.blockState2data(state);
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		// TODO incomplete, no data
		BlockEntity blockEntity = minecraft.level.getBlockEntity(new BlockPos(position.x, position.y, position.z));
		return blockEntity == null ? null : TypeConverter.blockEntity2data(blockEntity);
	}

	@Override
	public void addEntity(EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntities() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
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
		return minecraft.level.getBlockState(pos);
	}

	@Override
	public LevelAccessor getLevel() {
		// TODO Auto-generated method stub
		return minecraft.level;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		// TODO
		return minecraft.level.getBlockEntity(pPos);
	}
	
}
