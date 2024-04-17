package de.m_marvin.holostruct.client.levelbound;

import de.m_marvin.holostruct.client.levelbound.access.ILevelAccessor;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;

public class Levelbound {
	
	public static enum AccessLevel {
		NO_ACCESS(),
		READ_CLIENT(),
		FULL_CLIENT(),
		READ_SERVER(),
		FULL_SERVER();
	}
	
	private AccessLevel accessLevel;
	private ILevelAccessor accessor;
	
	public Levelbound() {
		this.accessLevel = AccessLevel.NO_ACCESS;
		this.accessor = new NoAccessAccessor();
	}
	
	public void setAccess(ILevelAccessor accessor, AccessLevel accessLevel) {
		this.accessor = accessor;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}
	
	public ILevelAccessor getAccessor() {
		return accessor;
	}
	
}
