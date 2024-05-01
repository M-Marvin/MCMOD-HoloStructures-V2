package de.m_marvin.holostruct.levelbound.network;

import de.m_marvin.holostruct.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Queries the permission config {@link ServerConfig} from the server.
 * If send from client to server, supplies the default config which the server save to an file is none exists yet.
 * If send from server to client, supplies the requested config from the server.
 * @author Marvin Koehler
 */
public record QueryAccessPermissions(String config) implements CustomPacketPayload {
	
	public static final ResourceLocation ID = new ResourceLocation("holostruct:query_access_permissions");
	
	public QueryAccessPermissions(FriendlyByteBuf pBuffer) {
		this(pBuffer.readUtf());
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeUtf(this.config);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

}
