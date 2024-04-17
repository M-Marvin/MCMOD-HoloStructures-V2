package de.m_marvin.holostruct.client.levelbound.access;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.univec.impl.Vec3i;

public interface ILevelAccessor extends IStructAccessor {

	@Override
	public default void setOffset(Vec3i offset) {}

	@Override
	public default Vec3i getOffset() {
		return new Vec3i(0, 0, 0);
	}
	
	@Override
	public default void setBounds(Vec3i min, Vec3i max) {}

	@Override
	public default Vec3i getBoundsMin() {
		return new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	@Override
	public default Vec3i getBoundsMax() {
		return new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	@Override
	default void copyTo(IStructAccessor target) {
		throw new UnsupportedOperationException("cant copy entire world!");
	}
	
	@Override
	default void logParseWarn(String errorMessage) {}
	
	@Override
	default void clearParseLogs() {}
	
}
