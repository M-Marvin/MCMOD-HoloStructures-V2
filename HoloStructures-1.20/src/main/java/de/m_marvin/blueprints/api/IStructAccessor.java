package de.m_marvin.blueprints.api;

import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

public interface IStructAccessor {
	
	public void setOffset(Vec3i offset);
	public Vec3i getOffset();
	
	public void setBounds(Vec3i min, Vec3i max);
	public Vec3i getBoundsMin();
	public Vec3i getBoundsMax();
	
	public void setBlock(Vec3i position, BlockStateData state);
	public BlockStateData getBlock(Vec3i position);
	
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity);
	public BlockEntityData getBlockEntity(Vec3i position);
	
	public void addEntity(EntityData entity);
	public void addEntity(Vec3i blockPos, EntityData entity);
	public void addEntities(Collection<EntityData> entities);
	public Collection<EntityData> getEntities(); 
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos);
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max);
	
	public void copyTo(IStructAccessor target);
	public void clearParseLogs();
	public void logParseWarn(String errorMessage);
	
}
