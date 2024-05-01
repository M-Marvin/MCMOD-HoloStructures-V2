package de.m_marvin.holostruct.levelbound.network;

import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ServerLevelAccessorImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Base of all levelbound packages used by the {@link ServerLevelAccessorImpl}
 * @author Marvin Koehler
 *
 * @param <T> Type of the response of the package
 */
public interface ILevelboundPackage<T> extends CustomPacketPayload {
	
	public int getTaskId();
	public T getResponse();
	
	public ILevelboundPackage<T> makeResponse(T response);
	
}
