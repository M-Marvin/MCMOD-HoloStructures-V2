package de.m_marvin.blueprints.api;

import de.m_marvin.univec.impl.Vec3i;

/**
 * Extends {@link IStructAccessor} for setting bounds and log errors when parsing or loading from an file
 * @author Marvin Koehler
 *
 */
public interface IBlueprintAcessor extends IStructAccessor {

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
	
	/**
	 * Pastes all blocks, blockentities and entities of this structure to the target structure. The target structure is not cleared before the pasting is started, so duplications might occur in the result.
	 * @param target The target structure to which the data of this structure should be copied
	 */
	public void copyTo(IBlueprintAcessor target);

	/**
	 * Method internally used by the parser to clear the errors from the previous read or write operation
	 */
	public void clearParseLogs();
	
	/**
	 * Method internally used by the parser to store errors that occurred while reading or writing an schematic file
	 */
	public void logParseWarn(String errorMessage);
	
}
