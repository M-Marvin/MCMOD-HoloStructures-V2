package de.m_marvin.holostruct.levelbound.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ILevelboundPackage<T> extends CustomPacketPayload {
	
	public int getTaskId();
	public T getResponse();
	
	public ILevelboundPackage<T> makeResponse(T response);
	
}
