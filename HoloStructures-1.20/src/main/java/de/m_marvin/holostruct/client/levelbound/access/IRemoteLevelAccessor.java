package de.m_marvin.holostruct.client.levelbound.access;

import de.m_marvin.holostruct.client.holograms.IFakeLevelAccess;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;

public interface IRemoteLevelAccessor extends IAsyncStructAccessor, IFakeLevelAccess {
	
	public AccessLevel getAccessLevel();
	
}
