package de.m_marvin.blueprints.api;

import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

/**
 * Interface that defines the methods used to interact with structures represented by the {@link BlockStateData}, {@link BlockEntityData} and {@link EntityData} classes.
 * @author Marvin Koehler
 */
public interface IStructAccessor {
	
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
	
}
