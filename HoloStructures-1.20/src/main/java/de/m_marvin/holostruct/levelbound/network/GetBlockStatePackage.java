package de.m_marvin.holostruct.levelbound.network;

import java.util.Map.Entry;

import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class GetBlockStatePackage implements ILevelboundPackage<BlockStateData> {

	public static final Type<GetBlockStatePackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "get_block_state_data"));
	public static final StreamCodec<FriendlyByteBuf, GetBlockStatePackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), GetBlockStatePackage::new);
	
	private final Vec3i position;
	private final int taskId;
	private BlockStateData state;
	
	public GetBlockStatePackage(int id, Vec3i pos) {
		this.position = pos;
		this.taskId = id;
	}
	
	public Vec3i getPosition() {
		return position;
	}
	
	@Override
	public int getTaskId() {
		return taskId;
	}

	@Override
	public BlockStateData getResponse() {
		return state;
	}

	@Override
	public ILevelboundPackage<BlockStateData> makeResponse(BlockStateData response) {
		GetBlockStatePackage pkg = new GetBlockStatePackage(this.taskId, this.position);
		pkg.state = response;
		return pkg;
	}
	
	public GetBlockStatePackage(FriendlyByteBuf pBuffer) {
		this.position = new Vec3i(
				pBuffer.readInt(),
				pBuffer.readInt(),
				pBuffer.readInt());
		if (pBuffer.readBoolean()) {
			this.state = new BlockStateData(TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
			int propCount = pBuffer.readInt();
			for (int i = 0; i < propCount; i++) {
				this.state.setValue(pBuffer.readUtf(), pBuffer.readUtf());
			}
		}
		this.taskId = pBuffer.readInt();
	}
	
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeInt(this.position.x);
		pBuffer.writeInt(this.position.y);
		pBuffer.writeInt(this.position.z);
		pBuffer.writeBoolean(this.state != null);
		if (this.state != null) {
			pBuffer.writeResourceLocation(TypeConverter.data2resLoc(this.state.getBlockName()));
			pBuffer.writeInt(this.state.getProperties().size());
			for (Entry<String, String> property : this.state.getProperties().entrySet()) {
				pBuffer.writeUtf(property.getKey());
				pBuffer.writeUtf(property.getValue());
			}
		}
		pBuffer.writeInt(this.taskId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	
}
