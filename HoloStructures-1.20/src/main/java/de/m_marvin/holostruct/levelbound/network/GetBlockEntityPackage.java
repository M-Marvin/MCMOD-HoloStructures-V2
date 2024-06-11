package de.m_marvin.holostruct.levelbound.network;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class GetBlockEntityPackage implements ILevelboundPackage<BlockEntityData> {

	public static final Type<GetBlockEntityPackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "get_block_entity_data"));
	public static final StreamCodec<FriendlyByteBuf, GetBlockEntityPackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), GetBlockEntityPackage::new);
	
	private final Vec3i position;
	private final int taskId;
	private BlockEntityData state;
	
	public GetBlockEntityPackage(int id, Vec3i pos) {
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
	public BlockEntityData getResponse() {
		return state;
	}

	@Override
	public ILevelboundPackage<BlockEntityData> makeResponse(BlockEntityData response) {
		GetBlockEntityPackage pkg = new GetBlockEntityPackage(this.taskId, this.position);
		pkg.state = response;
		return pkg;
	}
	
	public GetBlockEntityPackage(FriendlyByteBuf pBuffer) {
		this.position = new Vec3i(
				pBuffer.readInt(),
				pBuffer.readInt(),
				pBuffer.readInt());
		if (pBuffer.readBoolean()) {
			this.state = new BlockEntityData(position, TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
			if (pBuffer.readBoolean()) {
				this.state.setData(TypeConverter.nbt2data(pBuffer.readNbt()));
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
			pBuffer.writeResourceLocation(TypeConverter.data2resLoc(this.state.getTypeName()));
			pBuffer.writeBoolean(this.state.getData() != null);
			if (this.state.getData() != null) {
				pBuffer.writeNbt(TypeConverter.data2nbt(this.state.getData()));
			}
		}
		pBuffer.writeInt(this.taskId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	
}
