package de.m_marvin.holostruct.levelbound.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendAccessPermissons(String permissonConfig) implements CustomPacketPayload {
	
	public static final ResourceLocation ID = new ResourceLocation("holostruct:send_access_permissons");
	
	public SendAccessPermissons(FriendlyByteBuf buffer) {
		this(buffer.readUtf());
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeUtf(permissonConfig);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

}
