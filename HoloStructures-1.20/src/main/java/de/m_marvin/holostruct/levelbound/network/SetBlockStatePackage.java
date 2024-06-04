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

public class SetBlockStatePackage implements ILevelboundPackage<Boolean> {
	
	public static final Type<SetBlockStatePackage> TYPE = new Type<>(new ResourceLocation(HoloStruct.MODID, "set_block_state_data"));
	public static final StreamCodec<FriendlyByteBuf, SetBlockStatePackage> CODEC = StreamCodec.of((buf, val) -> val.write(buf), SetBlockStatePackage::new);
	
	private final Vec3i position;
	private final BlockStateData data;
	private final int taskId;
	private boolean state;
	
	public SetBlockStatePackage(int id, Vec3i pos, BlockStateData state) {
		this.taskId = id;
		this.position = pos;
		this.data = state;
		this.state = false;
	}
	
	public Vec3i getPosition() {
		return position;
	}
	
	public BlockStateData getData() {
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
		SetBlockStatePackage pkg = new SetBlockStatePackage(this.taskId, this.position, this.data);
		pkg.state = response;
		return pkg;
	}
	
	public SetBlockStatePackage(FriendlyByteBuf pBuffer) {
		this.position = new Vec3i(
				pBuffer.readInt(),
				pBuffer.readInt(),
				pBuffer.readInt());
		this.data = new BlockStateData(TypeConverter.resLoc2data(pBuffer.readResourceLocation()));
		int propCount = pBuffer.readInt();
		for (int i = 0; i < propCount; i++) {
			this.data.setValue(pBuffer.readUtf(), pBuffer.readUtf());
		}
		this.state = pBuffer.readBoolean();
		this.taskId = pBuffer.readInt();
	}
	
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeInt(this.position.x);
		pBuffer.writeInt(this.position.y);
		pBuffer.writeInt(this.position.z);
		pBuffer.writeResourceLocation(TypeConverter.data2resLoc(this.data.getBlockName()));
		pBuffer.writeInt(this.data.getProperties().size());
		for (Entry<String, String> property : this.data.getProperties().entrySet()) {
			pBuffer.writeUtf(property.getKey());
			pBuffer.writeUtf(property.getValue());
		}
		pBuffer.writeBoolean(this.state);
		pBuffer.writeInt(this.taskId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
