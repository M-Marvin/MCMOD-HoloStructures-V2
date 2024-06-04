package de.m_marvin.holostruct.levelbound.network;

import java.util.ArrayList;
import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class GetEntitiesPackage implements ILevelboundPackage<Collection<EntityData>> {

	public static final Type<GetEntitiesPackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "get_entities_data"));
	public static final StreamCodec<FriendlyByteBuf, GetEntitiesPackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), GetEntitiesPackage::new);
	
	private final Vec3d min;
	private final Vec3d max;
	private final int taskId;
	private Collection<EntityData> state;
	
	public GetEntitiesPackage(int id, Vec3d min, Vec3d max) {
		this.min = min;
		this.max = max;
		this.taskId = id;
	}
	
	public Vec3d getMin() {
		return min;
	}
	
	public Vec3d getMax() {
		return max;
	}
	
	@Override
	public int getTaskId() {
		return taskId;
	}

	@Override
	public Collection<EntityData> getResponse() {
		return state;
	}

	@Override
	public ILevelboundPackage<Collection<EntityData>> makeResponse(Collection<EntityData> response) {
		GetEntitiesPackage pkg = new GetEntitiesPackage(this.taskId, this.min, this.max);
		pkg.state = response;
		return pkg;
	}
	
	public GetEntitiesPackage(FriendlyByteBuf pBuffer) {
		this.min = new Vec3d(
				pBuffer.readDouble(),
				pBuffer.readDouble(),
				pBuffer.readDouble());
		this.max = new Vec3d(
				pBuffer.readDouble(),
				pBuffer.readDouble(),
				pBuffer.readDouble());
		if (pBuffer.readBoolean()) {
			this.state = new ArrayList<>();
			int count = pBuffer.readInt();
			for (int i = 0; i < count; i++) {
				Vec3d position = new Vec3d(
						pBuffer.readDouble(),
						pBuffer.readDouble(),
						pBuffer.readDouble());
				EntityData data = new EntityData(position, TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
				if (pBuffer.readBoolean()) {
					data.setData(TypeConverter.nbt2data(pBuffer.readNbt()));
				}
				this.state.add(data);
			}
		}
		this.taskId = pBuffer.readInt();
	}
	
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeDouble(this.min.x);
		pBuffer.writeDouble(this.min.y);
		pBuffer.writeDouble(this.min.z);
		pBuffer.writeDouble(this.max.x);
		pBuffer.writeDouble(this.max.y);
		pBuffer.writeDouble(this.max.z);
		pBuffer.writeBoolean(this.state != null);
		if (this.state != null) {
			pBuffer.writeInt(this.state.size());
			for (EntityData data : this.state) {
				pBuffer.writeDouble(data.getPosition().x);
				pBuffer.writeDouble(data.getPosition().y);
				pBuffer.writeDouble(data.getPosition().z);
				pBuffer.writeResourceLocation(TypeConverter.data2resLoc(data.getEntityName()));
				pBuffer.writeBoolean(data.getData() != null);
				if (data.getData() != null) {
					pBuffer.writeNbt(TypeConverter.data2nbt(data.getData()));
				}
			}
		}
		pBuffer.writeInt(this.taskId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	
}
