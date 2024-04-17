package de.m_marvin.holostruct.client.levelbound.access;

import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

public class NoAccessAccessor implements ILevelAccessor {

	@Override
	public void setBlock(Vec3i position, BlockStateData state) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public BlockStateData getBlock(Vec3i position) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public void addEntity(EntityData entity) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public Collection<EntityData> getEntities() {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		throw new AccessDeniedException("no access to game world!");
	}
	
}
