package de.m_marvin.holostruct.levelbound.network;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class SetBlockEntityPackage implements ILevelboundPackage<Boolean> {
	
	public static final Type<SetBlockEntityPackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "set_block_entity_data"));
	public static final StreamCodec<FriendlyByteBuf, SetBlockEntityPackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), SetBlockEntityPackage::new);
	
	private final BlockEntityData data;
	private final int taskId;
	private boolean state;
	
	public SetBlockEntityPackage(int id, BlockEntityData state) {
		this.taskId = id;
		this.data = state;
		this.state = false;
	}
	
	public BlockEntityData getData() {
		return data;
	}
	
	@Override
	public Boolean getResponse() {
		return this.state;
	}
	
	@Override
	public int getTaskId() {
		return this.taskId;
	}
	
	@Override
	public ILevelboundPackage<Boolean> makeResponse(Boolean response) {
		SetBlockEntityPackage pkg = new SetBlockEntityPackage(this.taskId, this.data);
		pkg.state = response;
		return pkg;
	}
	
	public SetBlockEntityPackage(FriendlyByteBuf pBuffer) {
		Vec3i position = new Vec3i(
				pBuffer.readInt(),
				pBuffer.readInt(),
				pBuffer.readInt());
		this.data = new BlockEntityData(position, TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
		if (pBuffer.readBoolean()) {
			this.data.setData(TypeConverter.nbt2data(pBuffer.readNbt()));
		}
		this.state = pBuffer.readBoolean();
		this.taskId = pBuffer.readInt();
	}
	
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeInt(this.data.getPosition().x);
		pBuffer.writeInt(this.data.getPosition().y);
		pBuffer.writeInt(this.data.getPosition().z);
		pBuffer.writeResourceLocation(TypeConverter.data2resLoc(this.data.getTypeName()));
		pBuffer.writeBoolean(this.data.getData() != null);
		if (this.data.getData() != null) {
			pBuffer.writeNbt(TypeConverter.data2nbt(this.data.getData()));
		}
		pBuffer.writeBoolean(this.state);
		pBuffer.writeInt(this.taskId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
