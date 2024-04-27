package de.m_marvin.holostruct.levelbound.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GetAccessPermissions(String defaultConfig) implements CustomPacketPayload {
	
	public static final ResourceLocation ID = new ResourceLocation("holostruct:querry_access_permissions");
	
	public GetAccessPermissions(FriendlyByteBuf pBuffer) {
		this(pBuffer.readUtf());
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeUtf(this.defaultConfig);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

}
