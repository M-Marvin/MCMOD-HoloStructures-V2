package de.m_marvin.blueprints.api;

import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

/**
 * Interface that defines the methods used to interact with structures represented by the {@link BlockStateData}, {@link BlockEntityData} and {@link EntityData} classes.
 * 
 * @author Marvin Koehler
 */
public interface IStructAccessor {
	
	/**
	 * Sets the offset of this structure. The offset basically determines which position within the structure is considered the "center". This position can be outside of the structures bounds.
	 * @param offset The position within the structure that should be considered the center
	 */
	public void setOffset(Vec3i offset);
	
	/**
	 * Gets the offset of this structure. The offset basically determines which position within the structure is considered the "center". This position can be outside of the structures bounds.
	 * @returns offset The position within the structure that should be considered the center
	 */
	public Vec3i getOffset();

	/**
	 * Sets the bounds (dimensions) of this blueprint, normally only used by the parser.<br>
	 * <b>WARNING</b>: Setting the bounds smaller than it really is, so that blocks, blockentities or entities end up outside of the bounds will cause undefined behavior.<br>
	 * <b>NOTE</b>: The implementation of this method does not neccesserly check if min is actually smaller than max, this must be done by the caller.
	 */
	public void setBounds(Vec3i min, Vec3i max);
	/**
	 * @return The min corner of this structures bounds.
	 */
	public Vec3i getBoundsMin();
	/**
	 * @return The max corner of this structures bounds.
	 */
	public Vec3i getBoundsMax();
	
	/**
	 * Checks if the position is within the bounds of this structure
	 * @param position The position to check
	 * @return true if the position is within the bounds
	 */
	public default boolean isInBounds(Vec3i position) {
		Vec3i min = getBoundsMin();
		Vec3i max = getBoundsMax().sub(new Vec3i(1, 1, 1));
		return	min.min(position).equals(min) &&
				max.max(position).equals(max);
	}
	
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
	
	/**
	 * Pastes all blocks, blockentities and entities of this structure to the target structure. The target structure is not cleared before the pasting is started, so duplications might occur in the result.
	 * @param target The target structure to which the data of this structure should be copied
	 */
	public void copyTo(IStructAccessor target);

	/**
	 * Method internally used by the parser to clear the errors from the previous read or write operation
	 */
	public void clearParseLogs();
	
	/**
	 * Method internally used by the parser to store errors that occurred while reading or writing an schematic file
	 */
	public void logParseWarn(String errorMessage);
	
}
