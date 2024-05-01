package de.m_marvin.holostruct.client.levelbound.access;

import de.m_marvin.holostruct.client.holograms.IFakeLevelAccess;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientLevelAccessorImpl;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ServerLevelAccessorImpl;

/**
 * An interface implemented by {@link ServerLevelAccessorImpl} and {@link ClientLevelAccessorImpl} used for accessing the level from the client
 * @author Marvin Koehler
 *
 */
public interface IRemoteLevelAccessor extends IAsyncStructAccessor, IFakeLevelAccess {
	
	/**
	 * Gets the access level currently available.
	 */
	public AccessLevel getAccessLevel();
	
}
