package de.m_marvin.holostruct.levelbound.network;

import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class AddEntityPackage implements ILevelboundPackage<Boolean> {
	
	public static final Type<AddEntityPackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "add_entity_data"));
	public static final StreamCodec<FriendlyByteBuf, AddEntityPackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), AddEntityPackage::new);
	
	private final EntityData data;
	private final int taskId;
	private boolean state;
	
	public AddEntityPackage(int id, EntityData state) {
		this.taskId = id;
		this.data = state;
		this.state = false;
	}
	
	public EntityData getData() {
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
		AddEntityPackage pkg = new AddEntityPackage(this.taskId, this.data);
		pkg.state = response;
		return pkg;
	}
	
	public AddEntityPackage(FriendlyByteBuf pBuffer) {
		Vec3d position = new Vec3d(
				pBuffer.readDouble(),
				pBuffer.readDouble(),
				pBuffer.readDouble());
		this.data = new EntityData(position, TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
		if (pBuffer.readBoolean()) {
			this.data.setData(TypeConverter.nbt2data(pBuffer.readNbt()));
		}
		this.state = pBuffer.readBoolean();
		this.taskId = pBuffer.readInt();
	}
	
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeDouble(this.data.getPosition().x);
		pBuffer.writeDouble(this.data.getPosition().y);
		pBuffer.writeDouble(this.data.getPosition().z);
		pBuffer.writeResourceLocation(TypeConverter.data2resLoc(this.data.getEntityName()));
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
