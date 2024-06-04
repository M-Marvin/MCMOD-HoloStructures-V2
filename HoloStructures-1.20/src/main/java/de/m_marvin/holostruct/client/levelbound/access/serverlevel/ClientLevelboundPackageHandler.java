package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.levelbound.network.ILevelboundPackage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handles the levelbound packages received from the server
 * @author Marvin Koehler
 */
public class ClientLevelboundPackageHandler {
	
	public void handlerTaskResponse(ILevelboundPackage<?> pkg, IPayloadContext context) {
		IRemoteLevelAccessor accessor = HoloStruct.CLIENT.LEVELBOUND.getAccessor();
		if (accessor instanceof ServerLevelAccessorImpl serverImpl) {
			serverImpl.handleResponse(pkg);
		}
	}
	
}
